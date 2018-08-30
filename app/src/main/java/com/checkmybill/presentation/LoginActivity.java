package com.checkmybill.presentation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.entity.Account;
import com.checkmybill.request.AccountRequester;
import com.checkmybill.util.IntentMap;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * A login screen that offers login via email/password.
 */
@EActivity(R.layout.activity_login)
public class LoginActivity extends BaseActivity {
    @ViewById(R.id.email_login) EditText email_login;
    @ViewById(R.id.password_login) EditText password_login;
    @ViewById(R.id.hidden_fabebookLoginButton) protected LoginButton hidden_fabebookLoginButton;

    protected CallbackManager fbCallbackManager;
    private Account account;
    private ProgressDialog loadingDialog;
    private RequestQueue requestQueue;
    private Context mContext;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores/Eventos da Acitivty/Layout)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(this);
        mContext = this;

        // Mudando a cor do statusBar
        if (Build.VERSION.SDK_INT >= 21 ) {
            getWindow().setStatusBarColor(ContextCompat.getColor(mContext, R.color.loginScreenPrimaryDark));
        }

        // Criando o loading Window
        this.loadingDialog = NotifyWindow.CreateLoadingWindow(mContext, "", "Aguarde...");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart(){
        super.onStart();

        // Inicializando SDK do Facebook
        FacebookSdk.sdkInitialize(this);
        fbCallbackManager = CallbackManager.Factory.create();
        this.hidden_fabebookLoginButton.setReadPermissions("email");
        this.hidden_fabebookLoginButton.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String fbUserToken = loginResult.getAccessToken().getToken();
                Log.d(LOG_TAG, "FBLogin.onSuccess TOKEN:" + loginResult.getAccessToken());
                account = new Account(null, null, null, null, null, null, fbUserToken, null);

                // Inicializando o login via facebook
                sendDoLoginRequest();
            }

            @Override
            public void onCancel() { Log.d(LOG_TAG, "FBLogin.onCancel"); }

            @Override
            public void onError(FacebookException error) { Log.d(LOG_TAG, "FBLogin.onError:" + Util.getMessageErrorFromExcepetion(error));             }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        requestQueue.cancelAll(getClass().getName());
    }

    /* ------------------------------------------------------------------------------------------ */
    // Eventos dos elementos/Views
    @EditorAction(R.id.password_login)
    public boolean passwordLoginEditorAction(TextView view, int actionId, KeyEvent event) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            handled = true;
            //ActivityUtils.hideKeyboard(this);
            this.loginButtonClick();
        }

        return handled;
    }

    @Click(R.id.login_button)
    public void loginButtonClick() {
        // Validando entradas
        String email = email_login.getText().toString();
        String password = password_login.getText().toString();
        if ( email.length() <= 0 || Util.isEmailValid(email) == false ) {
            email_login.setError(getString(R.string.txt_email_error));
            email_login.requestFocus();
            return;
        } else if ( password.length() <= 0 ) {
            password_login.setError(getString(R.string.campo_obrigatorio));
            password_login.requestFocus();
            return;
        }

        // Exibindo loading
        this.loadingDialog.setTitle("Realizando Login");
        this.loadingDialog.show();

        // Enviando requisicao de login por email
        account = new Account(null, email, null, null, password, null, null, null);
        sendDoLoginRequest();
    }

    @Click(R.id.facebook_login_button)
    public void facebookLoginButtonClick() {
        this.hidden_fabebookLoginButton.performClick();
    }

    @Click(R.id.recoveryPassword)
    public void recoveryPasswordClick() {
        // Exibindo dialog requisitando o email do usuário a ser recuperado a senha...
        final LinearLayout dialogRecoveryPassword = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_recovery_password, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar Senha");
        builder.setView( dialogRecoveryPassword );
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Obtendo os elementos a serem enviados
                EditText email = (EditText) dialogRecoveryPassword.findViewById(R.id.email_login);
                final String email2recovery = email.getText().toString();
                dialogInterface.dismiss();
                sendRecoveryPasswordRequest(email2recovery);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();

    }

    /* ------------------------------------------------------------------------------------------ */
    // Listeners do Volley
    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            loadingDialog.dismiss();
            Log.e(LOG_TAG, "Error:" + error.toString() + "| data:" + new String(error.networkResponse.data));
            try{
                if (Util.isConnectionTrue(LoginActivity.this)) {
                    new NotifyWindow(mContext).showErrorMessage("Login", getString(R.string.error_conexao), false);
                } else {
                    new NotifyWindow(mContext).showErrorMessage("Login", getString(R.string.sem_conexao), false);
                }
            } catch (Exception e) {
                new NotifyWindow(mContext).showErrorMessage("FALTAL ERROR", Util.getMessageErrorFromExcepetion(e), false);
            }
        }
    };

    private Response.Listener recoverySuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            loadingDialog.dismiss();
            String messageToShow;
            try {
                if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                    Log.e(LOG_TAG, "ERROR-JSON-RCV:" + response.toString());
                    messageToShow = response.getString("message");
                } else {
                    messageToShow = "Você recebera um email para iniciar a recuparação da senha";
                }

                // Enviado... Exibindo notificação
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Recuperar Senha");
                builder.setMessage(messageToShow);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            } catch (JSONException e ) {
                new NotifyWindow(mContext).showErrorMessage("FALTAL ERROR", Util.getMessageErrorFromExcepetion(e), false);
            }
        }
    };

    private Response.Listener loginSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            loadingDialog.dismiss(); // Ocultando o progress...

            if ( response == null ) return;
            else Log.d(LOG_TAG, "JSON-RCV:" + response.toString());

            // Checando a resposta do servidor...
            try {
                // Checando o status
                if (response.getString("status").equalsIgnoreCase("success") == false) {
                    if(isLoggedIn()){
                        LoginManager.getInstance().logOut();
                    }
                    new NotifyWindow(mContext).showErrorMessage("Login", response.getString("message"), false);
                    return;
                }

                // Salvando os dados e enviando para a interface inicial
                afterLoginSavePrefsAndGoHome(response);
            } catch ( JSONException ex ) {
                new NotifyWindow(mContext).showErrorMessage("FALTAL ERROR", Util.getMessageErrorFromExcepetion(ex), false);
            }
        }
    };

    // --------------------------------------------------------------------------------------------
    // -> Metodos privados da classe
    private void sendRecoveryPasswordRequest(final String email) {
        //progressDialog = ProgressDialog.show(this, "Recuperar Senha", "Aguarde...", true);
        this.loadingDialog.setTitle("Recuperando a Senha");
        this.loadingDialog.show();

        JsonObjectRequest jsonObjectRequest = AccountRequester.prepareRecoveryPassRequest(recoverySuccessListener, errorListener, email, this);
        jsonObjectRequest.setTag(getClass().getName());
        Log.d(LOG_TAG, "Recuperando a senha");
        requestQueue.add(jsonObjectRequest);
    }

    // -> Codigos originais de Victor,
    private void sendDoLoginRequest() {
        // Exibindo o loading (se necessario)
        if ( this.loadingDialog.isShowing() == false ) {
            this.loadingDialog.setTitle("Realizando Login");
            this.loadingDialog.show();
        }

        // Iniciando requisição...
        JsonObjectRequest jsonObjectRequest = AccountRequester.prepareDoLoginRequest(loginSuccessListener, errorListener, account, this);
        jsonObjectRequest.setTag(getClass().getName());
        requestQueue.add(jsonObjectRequest);
    }

    private void afterLoginSavePrefsAndGoHome(JSONObject response) throws JSONException {
        final SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(LoginActivity.this);
        sharedPrefsUtil.setAccessKey(response.getString("access_key"));
        sharedPrefsUtil.setUserName(response.getString("nome_usuario"));
        sharedPrefsUtil.setUserEmail(response.getString("login_email"));
        String fbUsername = response.getString("facebook_name");

        if ( fbUsername.length() > 0 )
            sharedPrefsUtil.setFBUserName( fbUsername );

        // Checando se tem mais que um telefone, se sim, ira perguntar ao usuario qual deseja escolher
        final JSONArray usuarioTelefoneList = response.getJSONArray("telefones");
        if (usuarioTelefoneList.length() == 1) {
            sharedPrefsUtil.setUserPhone(usuarioTelefoneList.getJSONObject(0).getString("num_telefone"));
        }
        else if (usuarioTelefoneList.length() > 1) {
            // Obtendo o layout e o populando o spinner...
            View dialogView = this.getLayoutInflater().inflate(R.layout.dialog_telefone_escolha, null);
            final Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner_telList);
            final ArrayList<String> spinnerItemList = new ArrayList<>();
            for (int idx = 0; idx < usuarioTelefoneList.length(); idx++) {
                String itemValue = usuarioTelefoneList.getJSONObject(idx).getString("num_telefone").trim();
                spinnerItemList.add(itemValue);
            }
            final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerItemList);
            spinner.setAdapter(spinnerAdapter);
            // Criando dialog box com o ListView dentro
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Escolha o telefone");
            builder.setView(dialogView);
            builder.setCancelable(false);
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    new NotifyWindow(mContext).showErrorMessage("Login", getString(R.string.login_cancelado), false);
                    return;
                }
            });
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int id = (int) spinner.getSelectedItemId();
                    String selectedNumTel = spinnerAdapter.getItem(id);
                    Log.i(LOG_TAG, "Selected Tel Num:" + selectedNumTel);
                    sharedPrefsUtil.setUserPhone(selectedNumTel);
                    sharedPrefsUtil.setSignFinished(true);
                    dialog.dismiss();
                    // Exbindo boas vindas e abrindo Activity
                    Toast.makeText(LoginActivity.this, getString(R.string.seja_bem_vindo), Toast.LENGTH_SHORT).show();
                    Intent it = new Intent(IntentMap.HOME);
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(it);
                }
            });
            // Exibindo dialogbox
            builder.create().show();
            return;
        }

        // Salvando o status como 'logado'
        sharedPrefsUtil.setSignFinished(true);

        // Exibindo mensagem de boas vindas
        Toast.makeText(this, getString(R.string.seja_bem_vindo), Toast.LENGTH_SHORT).show();

        // Abrindo a proxima tela (a home)
        Intent it = new Intent(IntentMap.HOME);
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(it);
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }
}

