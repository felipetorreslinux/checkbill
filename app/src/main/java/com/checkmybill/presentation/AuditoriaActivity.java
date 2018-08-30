package com.checkmybill.presentation;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.checkmybill.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

@EActivity(R.layout.activity_auditoria)
public class AuditoriaActivity extends BaseActivity {

    public static final int REQUEST_CODE_CAMERA_PICK = 1;

    @ViewById(R.id.btnDate)
    protected Button btnDate;

    @ViewById(R.id.btnAttachImage)
    protected Button btnAttachImage;

    @ViewById(R.id.btnSend)
    protected Button btnSend;

    @ViewById(R.id.layoutImages)
    protected LinearLayout layoutImages;

    protected Bundle savedState;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedState = savedInstanceState;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        btnDate.setText("Data:  " + day + "/" + month + "/" + year);

        Dialog dialogFuncionalidadeBeta = createDialogFuncionalidadeBeta();
        dialogFuncionalidadeBeta.show();
    }

    @Click
    protected void btnAttachImage(){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, REQUEST_CODE_CAMERA_PICK);
    }

    @Click
    protected void btnSend(){
        Dialog dialogGooglePlay = onCreateDialogGooglePlay(this.savedState);
        dialogGooglePlay.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            AuditoriaActivity activity = (AuditoriaActivity) this.getActivity();
            activity.btnDate.setText("Data:  " + day + "/" + month + "/" + year);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            AuditoriaActivity activity = (AuditoriaActivity) this.getActivity();
            activity.btnDate.setText("Data:  " + day + "/" + month + "/" + year);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA_PICK) {
            if (data != null) {
                Bitmap imgBitmap = (Bitmap) data.getExtras().get("data");

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(140, 140);
                lp.setMargins(10, 10, 10, 10);

                ImageView imageViewAttach = new ImageView(this);
                imageViewAttach.setLayoutParams(lp);
                imageViewAttach.setImageBitmap(imgBitmap);

                layoutImages.addView(imageViewAttach);
            }
        }
    }

    public Dialog onCreateDialogGooglePlay(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AuditoriaActivity.this);
        LayoutInflater inflater = AuditoriaActivity.this.getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_auditoria_buy, null);

        Button btnBuy = (Button) view.findViewById(R.id.btnBuy);
        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AuditoriaActivity.this, "Compra realizada com sucesso!", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        builder.setView(view)
                .setCancelable(true);

        return builder.create();
    }

    public Dialog createDialogFuncionalidadeBeta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AuditoriaActivity.this);
        builder.setMessage("Essa funcionalidade é apenas uma demonstração.")
                .setTitle("Atenção")
                .setPositiveButton("Entendi!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}
