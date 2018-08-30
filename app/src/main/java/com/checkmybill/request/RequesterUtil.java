package com.checkmybill.request;

import android.content.res.AssetFileDescriptor;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.checkmybill.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Victor Guerra on 09/06/2016.
 */
public class RequesterUtil {
    // Variavel para definir se deve ou não utilizar compressão dos dados
    public static boolean USE_GZIP_COMPRESSION = false;

    private static final String TAG = "RequesterUtil";
    private static final String STATUS = "status";

    private static final String RESPONSE_STATUS_ERROR = "error";
    private static final String RESPONSE_STATUS_SUCCESS = "success";

    public static boolean jsonIsEmpty(JSONObject jsonObject) {
        try {
            if (jsonObject != null && jsonObject.length() > 0) {
                return false;
            } else {
                return true;
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
            return true;
        }
    }

    public static boolean requestWasSuccess(JSONObject jsonObject) {
        try {
            String status = jsonObject.getString(STATUS);
            if (status.equals(RESPONSE_STATUS_SUCCESS)) {
                return true;
            } else if (status.equals(RESPONSE_STATUS_ERROR)) {
                return false;
            } else {
                return false;
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
            return false;
        }
    }

    public static JsonObjectRequest createGenericJsonObjectRequest(int requestMethod, String url, String requestBody, final Response.Listener responseListener, Response.ErrorListener erroListener) {
        JsonObjectRequest jsonRequest;
        String body;
        try {
            // Checando se deve gerar compressão
            if ( RequesterUtil.USE_GZIP_COMPRESSION ) {
                // Gerando os dados compactados e formatados em B64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gzipOut = new GZIPOutputStream( baos );
                gzipOut.write( requestBody.getBytes() );
                gzipOut.close();
                byte[] bytesToSend = baos.toByteArray();

                // Montando JSON com os dados compactados
                final JSONObject jsonObject = new JSONObject();
                jsonObject.put("c_method", "gzip");
                jsonObject.put("c_data", Base64.encodeToString(bytesToSend, Base64.DEFAULT));
                body = jsonObject.toString();
            } else {
                body = requestBody;
            }

            // Montando requisicao, com o valor de resposta interno, usado para verificar se
            // o retorno contem compactação ou não...
            jsonRequest = new JsonObjectRequest(requestMethod, url, body,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) { // Success Response
                            // Checando o tipo de resposta
                            Log.d(TAG, response.toString());
                            JSONObject jsonNewObject = response;
                            try {
                                // Checando se contem compactação/criptorafia
                                Log.d(TAG, "Teste:" + String.valueOf(response.isNull("c_method")));
                                if ( response.isNull("c_method") == false && response.getString("c_method").contains("gzip") ) {

                                    Log.d(TAG, "Dados comprimidos, decodificando-os");
                                    final int BUFFER_SIZE = 32;
                                    byte[] bytes =  Base64.decode(response.getString("c_data"), Base64.DEFAULT);
                                    ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
                                    GZIPInputStream gzipIn = new GZIPInputStream(bais, BUFFER_SIZE);
                                    StringBuilder builder = new StringBuilder();
                                    byte[] data = new byte[BUFFER_SIZE];
                                    int bytesRead;
                                    while ((bytesRead = gzipIn.read(data)) != -1) {
                                        builder.append(new String(data, 0, bytesRead));
                                    }

                                    gzipIn.close();
                                    bais.close();
                                    jsonNewObject = new JSONObject( builder.toString() );
                                } else if ( response.isNull("c_method") == false ) {
                                    Log.d(TAG, "Dados com uma compressão desconhecida");
                                    throw new Exception("Invalid Compression Method");
                                } else {
                                    // Sem compactação
                                    Log.d(TAG, "Sem compactação");
                                }
                            } catch ( Exception ex ) {
                                Log.d(TAG, "FATAL ERROR, não foi possivel decodificar/ler os dados");
                            }

                            responseListener.onResponse(jsonNewObject);
                        }
                    }, erroListener // Error Response
            );
            jsonRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });

            return jsonRequest;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }
    public static JsonObjectRequest createGenericJsonObjectRequest(int requestMethod, String url, final Response.Listener responseListener, Response.ErrorListener erroListener) {
        JsonObjectRequest jsonRequest;
        // Montando requisicao, com o valor de resposta interno, usado para verificar se
        // o retorno contem compactação ou não...
        jsonRequest = new JsonObjectRequest(requestMethod, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { // Success Response
                        // Checando o tipo de resposta
                        try {
                            // Checando se contem compactação/criptorafia
                            if ( response.has("c_method") && response.getString("c_method").contains("gzip") ) {
                                final int BUFFER_SIZE = 32;
                                byte[] bytes =  Base64.decode(response.getString("c_data"), Base64.DEFAULT);
                                ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
                                GZIPInputStream gzipIn = new GZIPInputStream(bais, BUFFER_SIZE);
                                StringBuilder builder = new StringBuilder();
                                byte[] data = new byte[BUFFER_SIZE];
                                int bytesRead;
                                while ((bytesRead = gzipIn.read(data)) != -1) {
                                    builder.append(new String(data, 0, bytesRead));
                                }

                                gzipIn.close();
                                bais.close();
                                JSONObject jsonNewObject = new JSONObject( builder.toString() );
                                responseListener.onResponse(jsonNewObject);
                            } else if ( response.has("c_method") ) {
                                throw new Exception("Invalid Compression Method");
                            } else {
                                // Sem compactação
                                responseListener.onResponse(response);
                            }
                        } catch ( Exception ex ) {
                            Log.d(TAG, "FATAL ERROR, não foi possivel decodificar/ler os dados");
                        }
                    }
                }, erroListener // Error Response
        );
        jsonRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        return jsonRequest;
    }

    public static MultipartRequester createMultipartRequest(int requestMethod, String url, final AssetFileDescriptor fd, final Response.Listener responseListener, Response.ErrorListener erroListener) {
        MultipartRequester multipartRequester = new MultipartRequester(requestMethod, url, responseListener, erroListener) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    FileInputStream fis = fd.createInputStream();
                    int len = (int) fd.getLength();
                    byte[] buffer = new byte[len];
                    fis.read(buffer, 0, len);
                    fis.close();
                    params.put("uploaded_file", new DataPart("5mb.rar", buffer, "compacted-rar"));
                    return params;
                } catch (Exception e) {
                    return null;
                }
            }
        };

        return multipartRequester;
    }
    public static MultipartRequester createMultipartRequest(int requestMethod, String url, final String requestBody, final AssetFileDescriptor fd, final Response.Listener responseListener, Response.ErrorListener erroListener) {
        MultipartRequester multipartRequester = new MultipartRequester(requestMethod, url, responseListener, erroListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("json_body", requestBody);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    FileInputStream fis = fd.createInputStream();
                    int len = (int) fd.getLength();
                    byte[] buffer = new byte[len];
                    fis.read(buffer, 0, len);
                    fis.close();
                    params.put("uploaded_file", new DataPart("5mb.rar", buffer, "compacted-rar"));
                    return params;
                } catch (Exception e) {
                    return null;
                }
            }
        };

        return multipartRequester;
    }
}
