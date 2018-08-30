package com.checkmybill.presentation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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
import com.checkmybill.util.FBUtil;
import com.checkmybill.util.IntentMap;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.checkmybill.views.PhoneEditText;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
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

import java.util.ArrayList;
import java.util.Arrays;

@EActivity(R.layout.activity_create_account)
public class CreateAccountActivity extends AppCompatActivity {
    @ViewById(R.id.password_container_layout) LinearLayout password_container_layout;
    @ViewById(R.id.nome_usuario) EditText nome_usuario;
    @ViewById(R.id.login_email) EditText login_email;
    @ViewById(R.id.num_telefone) PhoneEditText num_telefone;
    @ViewById(R.id.senha) EditText senha;
    @ViewById(R.id.confirm_senha) EditText confirm_senha;
    @ViewById(R.id.hidden_fabebookLoginButton) protected LoginButton hidden_fabebookLoginButton;

    private String fbUserToken;
    private String fbUserId;
    protected CallbackManager fbCallbackManager;
    private String LOG_TAG;
    private RequestQueue requestQueue;
    private Context mContext;
    private ProgressDialog loadingWindow;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores da Acitivty/Layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOG_TAG = getClass().getName();
        mContext = this;

        requestQueue = Volley.newRequestQueue(this);
        this.loadingWindow = NotifyWindow.CreateLoadingWindow(this, "Criar Conta", "");
    }

    private void populateEmailAndNameFields(String name, String mail, boolean hiddenPass) {
        nome_usuario.setText( name );
        login_email.setText( mail );
        password_container_layout.setVisibility(View.GONE);
        num_telefone.requestFocus();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Garantindo o logout do facebook
        LoginManager.getInstance().logOut();
    }
    @Override
    protected void onStart() {
        super.onStart();

        // Inicializando SDK do Facebook
        FacebookSdk.sdkInitialize(this);
        fbCallbackManager = CallbackManager.Factory.create();
        this.hidden_fabebookLoginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email"));
        this.hidden_fabebookLoginButton.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                fbUserToken = loginResult.getAccessToken().getToken();
                fbUserId = loginResult.getAccessToken().getUserId();

                Log.d(LOG_TAG, "FBLogin.onSuccess TOKEN:" + loginResult.getAccessToken());
                // Populando os dados e ocultando a informação de senha...
                FBUtil.getFBMe(loginResult.getAccessToken(), new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject user = FBUtil.getMe();
                            populateEmailAndNameFields(user.getString("name"), user.getString("email"), true);
                        } catch (JSONException e) {
                            new NotifyWindow(mContext).showErrorMessage("Criar Conta", "Erro ao carregar os dados do Facebook", false);
                        }
                    }
                });
            }

            @Override
            public void onCancel() { Log.d(LOG_TAG, "FBLogin.onCancel"); }

            @Override
            public void onError(FacebookException error) {
                new NotifyWindow(mContext).showErrorMessage("FALTAL ERROR", Util.getMessageErrorFromExcepetion(error), false);             }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /* ------------------------------------------------------------------------------------------ */
    // Eventos dos elementos/Views
    @EditorAction(R.id.confirm_senha)
    public boolean confirm_senhaEditorAction(TextView view, int actionId, KeyEvent event) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            handled = true;
            Log.d(LOG_TAG, "All Done -> With Password");

            // Disparando evento de criacao da conta (button Click)
            createAccountBtn();
        }

        return handled;
    }

    @EditorAction(R.id.num_telefone)
    public boolean num_telefoneEditorAction(TextView view, int actionId, KeyEvent event) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            handled = true;
            Log.d(LOG_TAG, "All Done");

            // Disparando evento de criacao da conta (button Click)
            createAccountBtn();
        }

        return handled;
    }

    @Click(R.id.createAccountBtn)
    public void createAccountBtn() {
        // Validando os argumentos obrigatorios
        final String sName = nome_usuario.getText().toString();
        final String sEmail = login_email.getText().toString();
        final String sPhone = num_telefone.getText().toString();
        if ( sName.length() <= 0 ) {
            nome_usuario.setError(getString(R.string.campo_obrigatorio));
            nome_usuario.requestFocus();
            return;
        }
        else if ( Util.isEmailValid(sEmail) == false ) {
            login_email.setError(getString(R.string.campo_obrigatorio));
            login_email.requestFocus();
            return;
        } else if ( sPhone.length() < 11 ) {
            num_telefone.setError(getString(R.string.campo_obrigatorio));
            num_telefone.requestFocus();
            return;
        }

        Account account = new Account(sName, sEmail, sPhone, getCurrentOperatorName(), "", "", null, null);

        // Definindo accessToken do FaceBook (se existir)
        if ( fbUserId != null ) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            Log.d(LOG_TAG, "Token:" + accessToken + " -> " + fbUserToken);
            account.setFbUserId( fbUserId );
            account.setFbUserToken( fbUserToken );
        } else {
            // Checando se a senha esta definida
            String sPassword = senha.getText().toString();
            String sPasswordConfirm = confirm_senha.getText().toString();
            if ( sPassword.length() <= 0 || sPasswordConfirm.equals(sPassword) == false ) {
                senha.setText("");
                confirm_senha.setText("");
                senha.setError(getString(R.string.campo_obrigatorio));
                senha.requestFocus();
                return;
            }
            account.setSenha(sPassword);
            account.setSenha(sPasswordConfirm);
        }

        // Enviando requisicao de cadastro
        this.loadingWindow.setMessage("Aguarde, criando a conta...");
        this.loadingWindow.show();
        JsonObjectRequest jsonObjectRequest = AccountRequester.prepareCreateAccountRequest(successResponseListener, errorResponseListener, account, this);
        jsonObjectRequest.setTag(getClass().getName());
        requestQueue.add( jsonObjectRequest );
    }

    @Click(R.id.facebook_login_button)
    public void facebookLoginButton() {
        this.hidden_fabebookLoginButton.performClick();
    }

    private String getCurrentOperatorName() {
        String operator = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            operator = telephonyManager.getNetworkOperatorName();
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "The error was: " + Util.getMessageErrorFromExcepetion(e));
        }
        return operator;
    }

    private Response.ErrorListener errorResponseListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            loadingWindow.dismiss();
            try {
                Log.e(LOG_TAG, "The erro was: " + error.toString() + " | data: " + new String(error.networkResponse.data));
                if (Util.isConnectionTrue(mContext)) {
                    new NotifyWindow(mContext).showErrorMessage("Criar Conta", getString(R.string.error_conexao), false);
                } else {
                    new NotifyWindow(mContext).showErrorMessage("Criar Conta", getString(R.string.sem_conexao), false);
                }

            } catch (Exception e) {
                new NotifyWindow(mContext).showErrorMessage("FATAL ERRO", Util.getMessageErrorFromExcepetion(e), false);
            }
        }
    };

    private Response.Listener successResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            loadingWindow.dismiss();
            try {
                // Checando resposta
                if ( response == null ) {
                    new NotifyWindow(mContext).showErrorMessage("FATAL ERRO", "Full Empty Response[ÑULL]", false);
                    return;
                }

                final String status = response.getString("status").toLowerCase();
                switch (status) {
                    case "success":
                        afterLoginSavePrefsAndGoHome(response);
                        break;
                    case "validating":
                        new NotifyWindow(mContext).showMessage("Criar Conta", "Entre no seu e-mail para ativar o cadastro.", false, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Mudando o Activity para o de login...
                                dialogInterface.dismiss();
                                Intent it = new Intent( IntentMap.LOGIN );
                                startActivity(it);
                            }
                        });
                        break;
                    default:
                        new NotifyWindow(mContext).showErrorMessage("Criar Conta", response.getString("message"), false);
                        break;
                }

                // Verificando a resposta
                Log.d(LOG_TAG, "JSON-Response:" + response.toString());
            } catch ( JSONException e ) {
                new NotifyWindow(mContext).showErrorMessage("FATAL ERRO", Util.getMessageErrorFromExcepetion(e), false);
            }
        }
    };

    private void afterLoginSavePrefsAndGoHome(JSONObject response) throws JSONException {
        final SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(mContext);
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
                    Toast.makeText(mContext, getString(R.string.seja_bem_vindo), Toast.LENGTH_SHORT).show();
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

    public void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                LoginManager.getInstance().logOut();

            }
        }).executeAsync();
    }
}
