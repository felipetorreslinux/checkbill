package com.checkmybill.request;

import android.util.Log;

import com.checkmybill.entity.NetworkQualityAverageApi;
import com.checkmybill.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Victor Guerra on 09/06/2016.
 * Entidade referente ao endpoint: http://54.207.195.4:8282/services/informacao-regiao-blarga
 * <p/>
 * Exemplo de resposta:
 * {
 * "status": "success",
 * "data": {
 * "info_gsm": {
 * "numero_entradas": 135,
 * "nivel_sinal": -87,
 * "indisponibilidade": 160
 * },
 * "info_blarga": {
 * "numero_entradas": 731,
 * "download": "-",
 * "upload": "-",
 * "indisponibilidade": 0
 * }
 * }
 * }
 */
public class ObterMediaOperadorasRequester {

    private static final String TAG = "InfooRegBLargaReq";

    private static final String DATA = "data";
    private static final String INFO_GSM = "info_gsm";
    private static final String INFO_GSM_NUM_ENTRADAS = "numero_entradas";
    private static final String INFO_GSM_NIVEL_SINAL = "nivel_sinal";
    private static final String INFO_GSM_INDISPONIBILIDADE = "indisponibilidade";
    private static final String INFO_BLARGA = "info_blarga";
    private static final String INFO_BLARGA_NUM_ENTRADAS = "numero_entradas";
    private static final String INFO_BLARGA_DOWNLOAD = "download";
    private static final String INFO_BLARGA_UPLOAD = "upload";
    private static final String INFO_BLARGA_INDISPONIBILIDADE = "indisponibilidade";

    public static NetworkQualityAverageApi getGsmInfoFromJsonResponse(JSONObject jsonObject) {
        if (!RequesterUtil.jsonIsEmpty(jsonObject) && RequesterUtil.requestWasSuccess(jsonObject)) {
            JSONObject jsonData;
            try {
                jsonData = jsonObject.getJSONObject(DATA);

                if (jsonData != null) {
                    JSONObject jsonInfoGsm = null;
                    try {
                        jsonInfoGsm = jsonData.getJSONObject(INFO_GSM);
                        if (jsonInfoGsm != null) {
                            int numero_entradas;
                            int nivel_sinal;
                            int indisponibilidade = 0;
                            try {
                                numero_entradas = jsonInfoGsm.getInt(INFO_GSM_NUM_ENTRADAS);
                            } catch (JSONException e) {
                                Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
                                Log.e(TAG, "error in get parameter: " + INFO_GSM_NUM_ENTRADAS);
                                return null;
                            }

                            try {
                                nivel_sinal = jsonInfoGsm.getInt(INFO_GSM_NIVEL_SINAL);
                            } catch (JSONException e) {
                                Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
                                Log.e(TAG, "error in get parameter: " + INFO_GSM_NIVEL_SINAL);
                                return null;
                            }

                            try {
                                indisponibilidade = jsonInfoGsm.getInt(INFO_GSM_INDISPONIBILIDADE);
                            } catch (JSONException e) {
                                Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
                                Log.e(TAG, "error in get parameter: " + INFO_GSM_INDISPONIBILIDADE);
                                return null;
                            }

                            NetworkQualityAverageApi networkQualityAverageApi = new NetworkQualityAverageApi(0, 0, 0, null, null, numero_entradas, indisponibilidade, nivel_sinal);
                            return networkQualityAverageApi;

                        } else {
                            return null;
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
                        Log.e(TAG, "error in get parameter: " + INFO_GSM);
                        return null;
                    }
                } else {
                    return null;
                }

            } catch (JSONException e) {
                Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
                Log.e(TAG, "error in get parameter: " + DATA);
                return null;
            }
        } else {
            return null;
        }

    }

    public static NetworkQualityAverageApi getBLargaInfoFromJsonResponse(JSONObject jsonObject) {
        if (!RequesterUtil.jsonIsEmpty(jsonObject) && RequesterUtil.requestWasSuccess(jsonObject)) {
            JSONObject jsonData;
            try {
                jsonData = jsonObject.getJSONObject(DATA);

                if (jsonData != null) {
                    JSONObject jsonInfoGsm = null;
                    try {

                        jsonInfoGsm = jsonData.getJSONObject(INFO_BLARGA);
                        if (jsonInfoGsm != null) {
                            int numero_entradas;
                            int download;
                            int upload;
                            int indisponibilidade;
                            try {
                                numero_entradas = jsonInfoGsm.getInt(INFO_BLARGA_NUM_ENTRADAS);
                            } catch (JSONException e) {
                                Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
                                Log.e(TAG, "error in get parameter: " + INFO_BLARGA_NUM_ENTRADAS);
                                return null;
                            }

                            try {
                                indisponibilidade = jsonInfoGsm.getInt(INFO_BLARGA_INDISPONIBILIDADE);
                            } catch (JSONException e) {
                                Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
                                Log.e(TAG, "error in get parameter: " + INFO_BLARGA_INDISPONIBILIDADE);
                                return null;
                            }

                            try {
                                download = jsonInfoGsm.getInt(INFO_BLARGA_DOWNLOAD);
                            } catch (JSONException e) {
                                Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
                                Log.e(TAG, "error in get parameter: " + INFO_BLARGA_DOWNLOAD);
                                return null;
                            }

                            try {
                                upload = jsonInfoGsm.getInt(INFO_BLARGA_UPLOAD);
                            } catch (JSONException e) {
                                Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
                                Log.e(TAG, "error in get parameter: " + INFO_BLARGA_UPLOAD);
                                return null;
                            }

                            NetworkQualityAverageApi networkQualityAverageApi = new NetworkQualityAverageApi(0, download, upload, null, null, numero_entradas, indisponibilidade, 0);
                            return networkQualityAverageApi;

                        } else {
                            return null;
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
                        Log.e(TAG, "error in get parameter: " + INFO_GSM);
                        return null;
                    }
                } else {
                    return null;
                }

            } catch (JSONException e) {
                Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
                Log.e(TAG, "error in get parameter: " + DATA);
                return null;
            }
        } else {
            return null;
        }

    }

}
