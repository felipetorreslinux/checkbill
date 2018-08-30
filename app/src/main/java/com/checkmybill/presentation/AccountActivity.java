package com.checkmybill.presentation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.request.AccountRequester;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Downloader;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

@EActivity(R.layout.activity_account)
public class AccountActivity extends BaseActivity {

    @ViewById(R.id.txt_user_name)
    protected EditText txt_user_name;
    @ViewById(R.id.txt_user_email)
    protected EditText txt_user_email;
    @ViewById(R.id.txt_user_phone_ddd)
    protected EditText txt_user_phone_ddd;
    @ViewById(R.id.txt_user_phone_number)
    protected EditText txt_user_phone_number;
    @ViewById(R.id.txt_hide)
    protected EditText txt_hide;
    @ViewById(R.id.btn_salvar)
    protected Button btn_salvar;
    @ViewById(R.id.btn_facebook)
    protected Button btn_facebook;
    @ViewById(R.id.hidden_fblogin_button)
    protected LoginButton hidden_fblogin_button;

    private SharedPrefsUtil sharedPrefsUtil;
    private ProgressDialog loadingDialogBox;
    private CallbackManager callbackManager;
    private RequestQueue requestQueue;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        LOG_TAG = getClass().getName();
        sharedPrefsUtil = new SharedPrefsUtil(this);

//        setContentView(R.layout.activity_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingDialogBox = new ProgressDialog(this);
        loadingDialogBox.setCancelable(false);

        // Facebook
        if ( FacebookSdk.isInitialized() == false )
            FacebookSdk.sdkInitialize(this);

        callbackManager = CallbackManager.Factory.create();
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart(){
        super.onStart();
        String userPhoneNumber = sharedPrefsUtil.getUserPhone().replaceAll("[()]","");

        txt_user_name.setText(sharedPrefsUtil.getUserName());
        txt_user_email.setText(sharedPrefsUtil.getUserEmail());
        txt_user_phone_ddd.setText(userPhoneNumber.substring(0, 2));
        txt_user_phone_number.setText(userPhoneNumber.substring(2));

        Log.d(LOG_TAG, userPhoneNumber);

        // Permitir a integração com o facebook?
        SharedPrefsUtil prefsUtil = new SharedPrefsUtil(AccountActivity.this);
        if ( prefsUtil.getFBUserName() != null ) {
            btn_facebook.setClickable(false);
            btn_facebook.setText("Logado como: " + prefsUtil.getFBUserName());
        } else {
            Log.d(LOG_TAG, "Register facebook callback");
            hidden_fblogin_button.setReadPermissions(Arrays.asList(
                    "public_profile", "email"));
            hidden_fblogin_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    String accessToken = loginResult.getAccessToken().getToken();
                    Log.d(LOG_TAG, "FbToken:" + accessToken);
                    facebookGetAcccessToken(accessToken);
                }

                @Override
                public void onCancel() {
                    Log.d(LOG_TAG, "Cancel FB Login");
                    loadingDialogBox.dismiss();
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(LOG_TAG, "Error FB Login");
                    facebookGetAcccessToken(null);
                }
            });
            LoginManager.getInstance().logOut();
        }
    }

    @Click
    void btn_facebook() {
        loadingDialogBox.setMessage("Realizando Login no Facebook");
        loadingDialogBox.setTitle("Integrando Facebook");
        loadingDialogBox.show();

        // Disparando evento para o login pelo facebook
        hidden_fblogin_button.performClick();
    }

    @Click
    void btn_salvar() {
        String sName = txt_user_name.getEditableText().toString();
        String sEmail = txt_user_email.getEditableText().toString();
        String sPhoneDDD = txt_user_phone_ddd.getEditableText().toString();
        String sPhoneNumber = txt_user_phone_number.getEditableText().toString();

        if (sName.length() > 0) {
            txt_user_name.setError(null);
            if (sEmail.length() > 0 && Util.isEmailValid(sEmail)) {
                txt_user_email.setError(null);
                if (sPhoneDDD.length() >= 2) {
                    txt_user_phone_ddd.setError(null);
                    if (sPhoneNumber.length() >= 9) {
                        txt_user_phone_number.setError(null);

                        sharedPrefsUtil.setUserName(sName);
                        sharedPrefsUtil.setUserPhone(sPhoneDDD + sPhoneNumber);
                        sharedPrefsUtil.setUserEmail(sEmail);

                        txt_hide.requestFocus();
                        closeKeyBoard();

                        // Disparando acao de salvar os dados no sistema...
                        saveAccountInfo();
                    } else {
                        txt_user_phone_number.setError(getString(R.string.txt_phone_number_error));
                        txt_user_phone_number.requestFocus();
                    }
                } else {
                    txt_user_phone_ddd.setError(getString(R.string.txt_phone_ddd_error));
                    txt_user_phone_ddd.requestFocus();
                }
            } else {
                txt_user_email.setError(getString(R.string.txt_email_error));
                txt_user_email.requestFocus();
            }
        } else {
            txt_user_name.setError(getString(R.string.txt_name_error));
            txt_user_name.requestFocus();
        }
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


    private void closeKeyBoard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void facebookGetAcccessToken(final String fbAccessToken) {
        if ( fbAccessToken == null ) {
            loadingDialogBox.dismiss();
            Toast.makeText(AccountActivity.this, "Erro: Não foi possível obter realizar o login pelo facebook", Toast.LENGTH_SHORT).show();
            return;
        }

        // Iniciando requisicao Volley
        JsonObjectRequest jsonObjectRequest = AccountRequester.prepareFacebookAccountIntegrationRequest(
                fbIntegrationSuccessListener,
                errorRequestListener,
                fbAccessToken,
                this);
        jsonObjectRequest.setTag(getClass().getName());
        requestQueue.add(jsonObjectRequest);
    }

    private void saveAccountInfo() {
        this.loadingDialogBox.setMessage("Salvando os dados");
        this.loadingDialogBox.show();

        // Executando acao de salvar a conta
        final String numTel = txt_user_phone_ddd.getText().toString() + " " + txt_user_phone_number.getText().toString();
        JsonObjectRequest jsonObjectRequest = AccountRequester.prepareAccountUpdateInfoRequester(
                accountSaveSuccessListener,
                errorRequestListener,
                txt_user_name.getText().toString(),
                txt_user_email.getText().toString(),
                numTel,
                this);
        jsonObjectRequest.setTag(getClass().getName());
        requestQueue.add(jsonObjectRequest);
    }


    // --------------------------------------------------------------------------------------------
    // Variaveis para o tratamento das requisições do Volley
    private Response.Listener accountSaveSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            loadingDialogBox.dismiss();
            try {
                // Veriricando status da resposta
                if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                    Toast.makeText(mContext, "Erro: Não foi possivel atualizar as informações", Toast.LENGTH_SHORT).show();
                    Log.e(LOG_TAG, response.getString("message"));
                    return;
                }

                // Dados atualizando com sucesso
                Toast.makeText(mContext, "Informações atualizadas", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Log.d(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
                Toast.makeText(mContext, Util.getMessageErrorFromExcepetion(e), Toast.LENGTH_SHORT).show();
            }
        }
    };
    private Response.Listener fbIntegrationSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            loadingDialogBox.dismiss();
            // Checando resposta
            try {
                // Checando resposta
                if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                    // Error
                    Log.e(LOG_TAG, response.getString("message"));
                    Toast.makeText(mContext, "Erro: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Integrado com sucesso... xD
                // Nome do usuario no facebok
                SharedPrefsUtil prefsUtil = new SharedPrefsUtil(mContext);
                String facebookName = response.getString("facebook_name");
                prefsUtil.setFBUserName( facebookName );

                Toast.makeText(mContext, "Facebook anexado com sucesso", Toast.LENGTH_SHORT).show();

                // Modificando o texto do botão...
                btn_facebook.setClickable(false);
                btn_facebook.setText("Logado como: " + facebookName);
            } catch ( JSONException ex ) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(ex));
            }
        }
    };
    private Response.ErrorListener errorRequestListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    };
}
