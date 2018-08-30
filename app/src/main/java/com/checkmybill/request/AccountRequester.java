package com.checkmybill.request;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.checkmybill.R;
import com.checkmybill.entity.Account;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Victor Guerra on 04/08/2016.
 */

public class AccountRequester {
    private static final String URL_FACEBOOK_INTEGRATION = Util.getSubFoldersURI() + "/integrar-facebook-usuario";
    private static final String URL_RECOVERY_PASSWORD = Util.getSubFoldersURI() + "/recuperar-senha";
    private static final String URL_CREATE_ACCOUNT = Util.getSubFoldersURI() + "/criar-nova-conta";
    private static final String URL_DO_LOGIN = Util.getSubFoldersURI() + "/efetuar-login";
    private static final String URL_CREATE_ANONYNOUS_ACCOUNT = Util.getSubFoldersURI() + "/criar-nova-conta-anonima";
    private static final String TAG = "AccountRequester";

    private static final String PARAMETER_ACCESS_KEY = "access_key";
    private static final String PARAMETER_NOME_USUARIO = "nome_usuario";
    private static final String PARAMETER_LOGIN_EMAIL = "login_email";
    private static final String PARAMETER_EMAIL = "email";
    private static final String PARAMETER_TELEFONES = "telefones";
    private static final String PARAMETER_TELEFONES_NUM_TELEFONE = "num_telefone";
    private static final String PARAMETER_TELEFONES_OPERADORA = "operadora";
    private static final String PARAMETER_SENHA = "senha";
    private static final String PARAMETER_SENHA_2 = "senha2";
    private static final String PARAMETER_FB_USER_TOKEN = "fb_token_user";
    private static final String PARAMETER_FB_USER_ID = "fb_user_id";
    private static final String PARAMETER_TELEPHONE_IMEI = "phone_imei";
    private static final String PARAMETER_ID_IMEI = "id_imei";

    public static JsonObjectRequest prepareCreateAccountRequest(Response.Listener responseListener, Response.ErrorListener erroListener, Account account, Context context) {
        String url = context.getString(R.string.url_base) + URL_CREATE_ACCOUNT;
        JSONObject jsonObject = new JSONObject();
        try {
            final int idIMEI = new SharedPrefsUtil(context).getIDImei();
            if ( idIMEI > 0 ) jsonObject.put(PARAMETER_ID_IMEI, idIMEI);
            jsonObject.put(PARAMETER_NOME_USUARIO, account.getNomeUsuario());
            jsonObject.put(PARAMETER_LOGIN_EMAIL, account.getLoginEmail());
            jsonObject.put("email", account.getLoginEmail());

            JSONArray jsonArrayTelefones = new JSONArray();
            JSONObject jsonObjectTelefone = new JSONObject();
            jsonObjectTelefone.put(PARAMETER_TELEFONES_NUM_TELEFONE, account.getTelefoneNum());
            jsonObjectTelefone.put(PARAMETER_TELEFONES_OPERADORA, account.getTelefoneOperadora());
            jsonArrayTelefones.put(jsonObjectTelefone);

            jsonObject.put(PARAMETER_TELEFONES, jsonArrayTelefones);

            if (account.getFbUserToken() != null && account.getFbUserToken().length() > 0) {
                jsonObject.put(PARAMETER_FB_USER_TOKEN, account.getFbUserToken());
                jsonObject.put(PARAMETER_FB_USER_ID, account.getFbUserId());
                // jsonObject.put(PARAMETER_SENHA, "NULLLLLLL");
                // jsonObject.put(PARAMETER_SENHA_2, "NULLLLLLL");
                // Log.d(TAG, "setting null password because of facebook signup");
            } else {
                jsonObject.put(PARAMETER_SENHA, account.getSenha());
                jsonObject.put(PARAMETER_SENHA_2, account.getSenha2());
                // Log.d(TAG, "setting default password");
            }

        } catch (JSONException e) {
            Log.e(TAG, "prepareLoginRequest | error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + URL_CREATE_ACCOUNT);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareDoLoginRequest(Response.Listener responseListener, Response.ErrorListener erroListener, Account account, Context context) {
        String url = context.getString(R.string.url_base) + URL_DO_LOGIN;
        JSONObject jsonObject = new JSONObject();
        try {
            if (account.getFbUserToken() != null && account.getFbUserToken().length() > 0) {
                jsonObject.put(PARAMETER_FB_USER_TOKEN, account.getFbUserToken());
            } else {
                jsonObject.put(PARAMETER_LOGIN_EMAIL, account.getLoginEmail());
                jsonObject.put(PARAMETER_SENHA, account.getSenha());
                jsonObject.put(PARAMETER_SENHA_2, account.getSenha2());
            }
        } catch (JSONException e) {
            Log.e(TAG, "prepareLoginRequest | error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + URL_DO_LOGIN);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareRecoveryPassRequest(Response.Listener responseListener, Response.ErrorListener erroListener, String email, Context context) {
        String url = context.getString(R.string.url_base) + URL_RECOVERY_PASSWORD;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMETER_EMAIL, email);
        } catch (JSONException e) {
            Log.e(TAG, "prepareLoginRequest | error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareFacebookAccountIntegrationRequest(Response.Listener responseListener, Response.ErrorListener erroListener, String fb_user_token, Context context) {
        String url = context.getString(R.string.url_base) +URL_FACEBOOK_INTEGRATION;
        JSONObject jsonObject = new JSONObject();
        final String accessKey = new SharedPrefsUtil(context).getAccessKey();
        try {
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMETER_FB_USER_TOKEN, fb_user_token);
        } catch ( JSONException e) {
            Log.e(TAG, "prepareLoginRequest | error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareAccountUpdateInfoRequester(Response.Listener responseListener, Response.ErrorListener erroListener, String nomeUsuario, String loginEmail, String numTelefone, Context context) {
        String url = Util.getSuperUrlServiceAtualizarInfoUsuario(context);
        JSONObject jsonObject = new JSONObject();
        final String accessKey = new SharedPrefsUtil(context).getAccessKey();

        try {
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMETER_NOME_USUARIO, nomeUsuario);
            jsonObject.put(PARAMETER_LOGIN_EMAIL, loginEmail);
            jsonObject.put(PARAMETER_TELEFONES_NUM_TELEFONE, numTelefone);
        } catch (JSONException e) {
            Log.e(TAG, "prepareLoginRequest | error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareAnonymousAccountRequest(Response.Listener responseListener, Response.ErrorListener errorListener, final String imei, Context context) {
        String url = context.getString(R.string.url_base) + URL_CREATE_ANONYNOUS_ACCOUNT;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMETER_TELEPHONE_IMEI, imei);
        } catch (JSONException e) {
            Log.e(TAG, "prepareLoginRequest | error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, errorListener);
        return genericJsonObjectRequest;
    }
}
