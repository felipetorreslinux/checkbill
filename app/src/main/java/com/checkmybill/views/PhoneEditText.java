package com.checkmybill.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.widget.EditText;

import com.checkmybill.R;

/**
 * Created by Petrus A. (R@G3), ESPE... On 05/05/2017.
 */

public class PhoneEditText extends android.support.v7.widget.AppCompatEditText implements TextWatcher {
    public enum NumDigits {
        DIGITS_BOTH, DIGITS_8, DIGITS_9;
    };

    public class Fields {
        final static public int PHONE = 0x01;
        final static public int LOCAL = 0x02;
        final static public int COUNTRY = 0x04;
    }

    final protected String TAG = getClass().getName();
    private boolean withPhoneInfo;
    private boolean withLocalInfo;
    private boolean withCountryInfo;
    private int numDigitsCode;

    private int autoChangeTextLimit;
    protected String currentMask = "";
    protected String maskErrorText;

    public PhoneEditText(Context context) {
        super(context);
        this.withCountryInfo = false;
        this.withLocalInfo = true;
        this.numDigitsCode = 0;
        this.maskErrorText = getContext().getString(R.string.phoneedittext_invalid_mask_err_text);

        // Setting Listeners...
        this.initClassListeners();

        // Completing Initialization
        this.initElement();
    }

    public PhoneEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Sertting Attributes
        this.initAttributes(attrs);
        // Setting Listeners...
        this.initClassListeners();

        // Completing Initialization
        this.initElement();
    }

    public PhoneEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Sertting Attributes
        this.initAttributes(attrs);

        // Setting Listeners...
        this.initClassListeners();

        // Completing Initialization
        this.initElement();
    }

    /**
     * Generate Phone mask (8 or 9 digits) with or without DDD value
     * @param nineDigits -> Boolean indicating if generate with 9 digits or 8
     * @return -> String Mask
     */
    protected String generatePhoneMask(boolean nineDigits) {
        final StringBuilder sp = new StringBuilder();
        if ( withCountryInfo ) sp.append("+99").append((!withLocalInfo && !withPhoneInfo) ? "":" ");
        if ( withLocalInfo ) sp.append("(99)").append((!withPhoneInfo) ? "":" ");
        if ( withPhoneInfo && nineDigits ) sp.append("99999-9999");
        else if ( withPhoneInfo ) sp.append("9999-9999");
        return sp.toString();
    }

    protected void initAttributes(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.PhoneEditText, 0, 0);
        try {
            // Type of digits
            this.numDigitsCode = a.getInteger(R.styleable.PhoneEditText_digitsType, 0);

            // Error text
            this.maskErrorText = a.getString(R.styleable.PhoneEditText_maskErrorText);
            if ( this.maskErrorText == null || this.maskErrorText.length() <= 0 )
                this.maskErrorText = getContext().getString(R.string.phoneedittext_invalid_mask_err_text);

            // Setting Field Flags (default: LOCAL|PHONE)
            final int phoneFieldsFlags = a.getInt(R.styleable.PhoneEditText_fields, 0x03);
            withCountryInfo = ((Fields.COUNTRY & phoneFieldsFlags) == Fields.COUNTRY);
            withLocalInfo = ((Fields.LOCAL & phoneFieldsFlags) == Fields.LOCAL);
            withPhoneInfo = ((Fields.PHONE & phoneFieldsFlags) == Fields.PHONE);
        } finally {
            a.recycle();
        }
    }

    protected void initClassListeners() {
        this.addTextChangedListener(this);
        this.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
    }

    protected void initElement() {
        // Setting attributes to Work with Phone Number
        this.currentMask = this.generatePhoneMask((this.numDigitsCode == 2));
        this.setInputType(InputType.TYPE_CLASS_PHONE);

        // Setting limits...
        this.setControlLimits();
    }

    protected void setControlLimits() {
        this.autoChangeTextLimit = (withPhoneInfo) ? 9 : 0;
        if ( this.withLocalInfo ) this.autoChangeTextLimit += 5;
        if ( this.withCountryInfo ) this.autoChangeTextLimit += 4;

        // Setting max text lenght limit
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(this.currentMask.length() + 1)});
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Checking is mask is disabled
        if ( this.currentMask == null || this.currentMask.length() <= 0 )
            return;

        if ( (start >= this.autoChangeTextLimit && after == 1) && this.currentMask.length() <= this.autoChangeTextLimit ) {
            this.currentMask = this.generatePhoneMask((this.numDigitsCode != 1));
        } else if ( after <= this.autoChangeTextLimit && this.currentMask.length() != this.autoChangeTextLimit ){
            this.currentMask = this.generatePhoneMask((this.numDigitsCode == 2));
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if ( s == null || s.length() <= 0 )
            return; // Nothing to do...

        final String currentTextValue = this.getUnmaskedText().replaceAll("\\s+", "");
        final int currentTextSize = currentTextValue.length();
        if ( this.currentMask == null || this.currentMask.length() <= 0 ) {
            this.setSelection(getText().length()); // move cursor to end
            return; // Nothing to do
        }

        // Applying mask
        String newTextValue = "";
        int mask_idx, text_idx;
        for ( mask_idx = 0, text_idx = 0; mask_idx < currentMask.length(); mask_idx++ ) {
            if ( text_idx >= currentTextSize ) {
                break; // Stopping loop
            }

            char ch = currentTextValue.charAt(text_idx);
            char msk = this.currentMask.charAt(mask_idx);

            // Checando type of mask char (9 == number, a == Letters, # == Any or Mask Fix Char)
            if ( msk == '9' ) { // Mask to digit/number,
                if ( !Character.isDigit(ch) ) break; // Invalid char
                else { newTextValue += ch; text_idx++; }
            } else if ( msk == 'a' ) { // Mask to letter
                if ( !Character.isLetter(ch) ) break; // Invalid char
                else { newTextValue += ch; text_idx++; }
            } else if ( msk == '#' ) { // Mask to anywhere
                newTextValue += ch; text_idx++;
            } else { // Fixed mask value...
                newTextValue += msk;
            }
        }

        // Setting new text value
        this.removeTextChangedListener(this); // Remove this listener to prevent freezy...
        this.setText(newTextValue);
        this.setSelection(newTextValue.length()); // move cursor to end
        this.addTextChangedListener(this); // Add this listener again xD
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if ( !focused && (this.currentMask != null && this.currentMask.length() > 0) ) {
            // Focus Leaving... validating mask (using lenght)
            int maskLen = this.currentMask.length();
            int textLen = getText().toString().length();
            if ( textLen > 0 && maskLen != textLen ) {
                // Cleaning text and setting error massage
                this.setText("");
                this.setError(maskErrorText);
            }
        }

        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    public void setPhoneFields(final int phoneFieldsValue) {
        withCountryInfo = ((Fields.COUNTRY & phoneFieldsValue) == Fields.COUNTRY);
        withLocalInfo = ((Fields.LOCAL & phoneFieldsValue) == Fields.LOCAL);
        withPhoneInfo = ((Fields.PHONE & phoneFieldsValue) == Fields.PHONE);

        this.initElement();
        this.setText(this.getUnmaskedText().replaceAll("\\s+", ""));
    }

    /**
     * Get current text (UNMASKED)
     * @return -> Unmasked current Text value
     */
    public String getUnmaskedText() {
        return getText().toString().replaceAll("[.]", "").replaceAll("[+]", "")
                .replaceAll("[-]", "").replaceAll("[/]", "")
                .replaceAll("[(]", "").replaceAll("[)]", "");
    }

    /**
     * Get current text (MASKED) (same as getText)
     * @return -> Masked current Text value
     */
    public String getMaskedText() {
        return getText().toString();
    }

    /**
     * Set number of phone digits (8,9 or both with automatic change)
     * @param nd -> Enum with number digits value
     */
    public void setNumDigits(NumDigits nd) {
        this.numDigitsCode = nd.ordinal();
        this.currentMask = this.generatePhoneMask((numDigitsCode == 2));
        this.setControlLimits();
        this.setText(this.getUnmaskedText().replaceAll("\\s+", ""));
    }
}
