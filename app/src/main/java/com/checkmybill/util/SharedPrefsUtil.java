package com.checkmybill.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.checkmybill.R;
import com.checkmybill.entity.Plano;
import com.checkmybill.entity.TipoPlano;

/**
 * Created by Victor Guerra on 08/03/2016.
 */
public class SharedPrefsUtil {
    private static final String SF_NOSIGNED_JUMP_LOGIN = "SF_NOSIGNED_JUMP_LOGIN";
    private static final String SF_SIGN_FINISHED = "SF_SIGN_FINISHED";
    private static final String SF_USER_NAME = "USER_NAME";
    private static final String SF_USER_PHONE = "USER_PHONE";
    private static final String SF_USER_EMAIL = "USER_EMAIL";
    private static final String SHOW_INTRODUCTION = "SHOW_INTRODUCTION";
    private static final String MAP_VIEW_PERSONAL = "MAP_VIEW_PERSONAL";
    private static final String ALARM_SIGNAL_STRENGTH_LOAD = "ALARM_SIGNAL_STRENGTH_LOAD";
    private static final String CONF_MOB_2G = "CONF_MOB_2G";
    private static final String CONF_MOB_4G = "CONF_MOB_4G";
    private static final String CONF_MOB_TIME_UNAVAILABILITY = "CONF_MOB_TIME_UNAVAILABILITY";
    private static final String SF_MONITOR_WIFI = "SF_MONITOR_WIFI";
    private static final String SF_CURRENT_WIFI_ID = "SF_CURRENT_WIFI_ID";
    private static final String SF_CURRENT_DATA_USE_ID = "SF_CURRENT_DATA_USE_ID";
    private static final String ACCESS_KEY = "ACCESS_KEY";
    private static final String FB_USER_NAME = "FACEBOOK_USER_NAME";
    private static final String NOLOGIN_ID_IMEI = "NOLOGIN_ID_IMEI";

    private static final String GET_INITIAL_CALL_DATAS = "GET_INITIAL_CALL_DATAS";
    private static final String GET_INITIAL_SMS_DATAS = "GET_INITIAL_SMS_DATAS";
    private static final String GET_INITIAL_SERVER_USER_DATA = "GET_INITIAL_SERVER_USER_DATA";

    // Constrole para a exibicao dos tutoriais
    private static final String PCONSUMO_IS_FIRST_VISUALIZATION = "PCONSUMO_IS_FIRST_VISUALIZATION";
    private static final String PLANO_IS_FIRST_VISUALIZATION = "PLANO_IS_FIRST_VISUALIZATION";

    private static final String PCONSUMO_SELECTED_POSTION = "PCONSUMO_SELECTED_POSTION";

    private SharedPreferences sharedPref;

    public SharedPrefsUtil(Context context) {
        sharedPref = context.getSharedPreferences(
                context.getString(R.string.prefs), Context.MODE_PRIVATE);
    }

    public void clearAllData() {
        /**
         * @Important: Modificação realizada na issue136, para permitir o funcionamento correto dos
         * serviços, principalmente, o 'ServiceInitialDataReader', que obtem os dados iniciais e SMS
         * e Ligação... Desse modo, ao limpar os dados, tudo será apagado exceto as informações
         * que indica que estes dados iniciais já foram obtidos, evitando assim a repetição
         *  destes... Petrus Augusto, @issue136, 2016-11-29
         */
        // Obtendo status do 'InitialReader', este é o unico que deve ser mantido...
        boolean userCall = this.getInitialUserCallData();
        boolean userSms = this.getInitialUserSMSData();
        boolean userSyncData = this.getInitialServerUserData();

        // Limpando os dados...
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.clear();

        // Definindo os valores armazenados de backup...
        edit.putBoolean(GET_INITIAL_CALL_DATAS, userCall);
        edit.putBoolean(GET_INITIAL_SMS_DATAS, userSms);
        edit.putBoolean(GET_INITIAL_SERVER_USER_DATA, userSyncData);

        // Commit...
        edit.commit();
    }

    public String getUserName() {
        return sharedPref.getString(SF_USER_NAME, null);
    }

    public void setIDImei(int id_imei) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(NOLOGIN_ID_IMEI, id_imei);
        editor.commit();
    }

    public int getIDImei() {
        return sharedPref.getInt(NOLOGIN_ID_IMEI, 0);
    }

    public void setUserName(String user_phone) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SF_USER_NAME, user_phone);
        editor.commit();
    }

    public String getFBUserName() {
        return sharedPref.getString(FB_USER_NAME, null);
    }

    public void setFBUserName(String fb_user_name) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(FB_USER_NAME, fb_user_name);
        editor.commit();
    }

    public String getUserPhone() {
        return sharedPref.getString(SF_USER_PHONE, null);
    }

    public void setUserPhone(String user_phone) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SF_USER_PHONE, user_phone);
        editor.commit();
    }

    public String getUserEmail() {
        return sharedPref.getString(SF_USER_EMAIL, null);
    }

    public void setUserEmail(String user_phone) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SF_USER_EMAIL, user_phone);
        editor.commit();
    }

    public boolean getShowIntroduction() {
        return sharedPref.getBoolean(SHOW_INTRODUCTION, true);
    }

    public void setShowIntroduction(boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SHOW_INTRODUCTION, value);
        editor.commit();
    }

    public boolean getMapViewPersonal() {
        return sharedPref.getBoolean(MAP_VIEW_PERSONAL, false);
    }

    public void setMapViewPersonal(boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(MAP_VIEW_PERSONAL, value);
        editor.commit();
    }

    /*public boolean getAlarmSignalStrengthLoad() {
        return sharedPref.getBoolean(ALARM_SIGNAL_STRENGTH_LOAD, false);
    }*/

    public void setAlarmSignalStrengthLoad(boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(ALARM_SIGNAL_STRENGTH_LOAD, value);
        editor.commit();
    }

    public int getConfMob2g() {
        return sharedPref.getInt(CONF_MOB_2G, -1);
    }

    public void setConfMob2g(int confMob2g) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(CONF_MOB_2G, confMob2g);
        editor.commit();
    }

    public int getConfMob4g() {
        return sharedPref.getInt(CONF_MOB_4G, -1);
    }

    public void setConfMob4g(int confMob4g) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(CONF_MOB_4G, confMob4g);
        editor.commit();
    }

    public int getConfMobTimeUnavailability() {
        return sharedPref.getInt(CONF_MOB_TIME_UNAVAILABILITY, -1);
    }

    public void setConfMobTimeUnavailability(int confMobTimeUnavailability) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(CONF_MOB_TIME_UNAVAILABILITY, confMobTimeUnavailability);
        editor.commit();
    }

    public boolean getSfMonitorWifi() {
        return sharedPref.getBoolean(SF_MONITOR_WIFI, false);
    }

    public void setSfMonitorWifi(boolean sfMonitorWifi) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SF_MONITOR_WIFI, sfMonitorWifi);
        editor.commit();
    }

    public int getSfCurrentWifi() {
        return sharedPref.getInt(SF_CURRENT_WIFI_ID, -1);
    }

    public void setSfCurrentWifi(int idCurrentWifi) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(SF_CURRENT_WIFI_ID, idCurrentWifi);
        editor.commit();
    }

    public int getSfCurrentDataUseId() {
        return sharedPref.getInt(SF_CURRENT_DATA_USE_ID, -1);
    }

    public void setSfCurrentDataUseId(int sfCurrentDataUseId) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(SF_CURRENT_DATA_USE_ID, sfCurrentDataUseId);
        editor.commit();
    }

    public boolean getSignFinished() {
        return sharedPref.getBoolean(SF_SIGN_FINISHED, false);
    }

    public void setSignFinished(boolean signFinished) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SF_SIGN_FINISHED, signFinished);
        editor.commit();
    }

    public String getAccessKey() {
        return sharedPref.getString(ACCESS_KEY, null);
    }

    public void setAccessKey(String accessKey) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ACCESS_KEY, accessKey);
        editor.commit();
    }

    public boolean getInitialUserCallData() {
        return sharedPref.getBoolean(GET_INITIAL_CALL_DATAS, true);
    }

    public void setInitialUserCallData(boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(GET_INITIAL_CALL_DATAS, value);
        editor.commit();
    }

    public boolean getInitialUserSMSData() {
        return sharedPref.getBoolean(GET_INITIAL_SMS_DATAS, true);
    }

    public void setInitialUserSMSData(boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(GET_INITIAL_SMS_DATAS, value);
        editor.commit();
    }

    public boolean getInitialServerUserData() {
        return sharedPref.getBoolean(GET_INITIAL_SERVER_USER_DATA, true);
    }

    public void setInitialServerUserData(boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(GET_INITIAL_SERVER_USER_DATA, value);
        editor.commit();
    }

    public boolean getPConsumoIsFirstVisualization() {
        return sharedPref.getBoolean(PCONSUMO_IS_FIRST_VISUALIZATION, true);
    }

    public void setPConsumoIsFirstVisualization(boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(PCONSUMO_IS_FIRST_VISUALIZATION, value);
        editor.commit();
    }

    public boolean getPlanoIsFirstVisualization() {
        return sharedPref.getBoolean(PLANO_IS_FIRST_VISUALIZATION, true);
    }

    public void setPlanoIsFirstVisualization(boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(PLANO_IS_FIRST_VISUALIZATION, value);
        editor.commit();
    }

    public boolean getJumpLogin() {
        return sharedPref.getBoolean(SF_NOSIGNED_JUMP_LOGIN, false);
    }

    public void setJumpLogin(boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SF_NOSIGNED_JUMP_LOGIN, value);
        editor.commit();
    }

    public int getSelectedPConsumoFilterPosition() {
        return sharedPref.getInt("PCONSUMO_SELECTED_POSTION", 0);
    }

    public void setSelectedPConsumoFilterPosition(int pos) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("PCONSUMO_SELECTED_POSTION", pos);
        editor.commit();
    }

    public void setTipoPlanoList(TipoPlano[] arrTipoPlano) {
        final int size = arrTipoPlano.length;
        final SharedPreferences.Editor editor = sharedPref.edit();
        final String BASEKEY = "DATA_TIPOPLANO_ARRAY";

        // Definindo Size...
        editor.putInt(BASEKEY + "__SIZE", size);
        for ( int i = 0; i < arrTipoPlano.length; i++ ) {
            TipoPlano tp = arrTipoPlano[i];
            editor.putInt(BASEKEY + "__" + i + "_ID", tp.getId());
            editor.putString(BASEKEY + "__" + i + "_DESCRICAO", tp.getDescricaoTipoPlano());
        }
        editor.commit();
    }

    public TipoPlano[] getTipoPlanoList() {
        final String BASEKEY = "DATA_TIPOPLANO_ARRAY";
        final int size = sharedPref.getInt(BASEKEY + "__SIZE", 0);
        if ( size <= 0 ) return null;
        TipoPlano[] arrTipoPlano = new TipoPlano[size];
        for ( int i = 0; i < size; i++ ) {
            TipoPlano tp = new TipoPlano();
            tp.setId(sharedPref.getInt(BASEKEY + "__" + i + "_ID", 0));
            tp.setDescricaoTipoPlano(sharedPref.getString(BASEKEY + "__" + i + "_DESCRICAO", ""));
            arrTipoPlano[i] = tp;
        }

        return arrTipoPlano;
    }

    public Plano getMeuPlanoClass() {
        final Plano meuPlano = new Plano();
        final String BASEKEY = "MEUPLANO_CLASS__";
        meuPlano.setIdPlano(sharedPref.getInt(BASEKEY + "ID_PLANO", 0));
        if ( meuPlano.getIdPlano() <= 0 ) return null;

        // Obtendo o restante dos dados do Plano...
        meuPlano.setIdOperadora(sharedPref.getInt(BASEKEY + "ID_OPERADORA", 0));
        meuPlano.setIdModalidadePlano(sharedPref.getInt(BASEKEY + "ID_MODALIDADE_PLANO", 0));
        meuPlano.setIdTipoPlano(sharedPref.getInt(BASEKEY + "ID_TIPO_PLANO", 0));
        meuPlano.setIdDDD(sharedPref.getInt(BASEKEY + "ID_DDD", 0));
        meuPlano.setMinMO(sharedPref.getInt(BASEKEY + "MIN_MO", 0));
        meuPlano.setMinMO(sharedPref.getInt(BASEKEY + "MIN_OO", 0));
        meuPlano.setMinFixo(sharedPref.getInt(BASEKEY + "MIN_FIXO", 0));
        meuPlano.setMinIU(sharedPref.getInt(BASEKEY + "MIN_IU", 0));
        meuPlano.setSmsInclusos(sharedPref.getInt(BASEKEY + "SMS_INCLUSIVO", 0));
        meuPlano.setDtVencimento(sharedPref.getInt(BASEKEY + "DT_VENCIMENTO", 0));
        meuPlano.setIdPlanoReferencia(sharedPref.getInt(BASEKEY + "ID_PLANO_REFERENCIA", 0));
        meuPlano.setLimiteDadosWeb(sharedPref.getLong(BASEKEY + "LIMITE_WEB", 0));
        meuPlano.setSmsExtras(sharedPref.getFloat(BASEKEY + "SMS_EXTRA", 0));
        meuPlano.setValorPlano(sharedPref.getFloat(BASEKEY + "VALOR_PLANO", 0));
        meuPlano.setBonus(sharedPref.getFloat(BASEKEY + "BONUS", 0));
        meuPlano.setNomePlano(sharedPref.getString(BASEKEY + "NOME_PLANO", ""));
        meuPlano.setObservacao(sharedPref.getString(BASEKEY + "OBSERVACAO", ""));
        meuPlano.setNomeOperadora(sharedPref.getString(BASEKEY + "NOME_OPERADORA", ""));
        meuPlano.setDescricaoTipoPlano(sharedPref.getString(BASEKEY + "DESCRICAO_TIPO_PLANO", ""));
        meuPlano.setNomeOperadora(sharedPref.getString(BASEKEY + "DESCRICAO_MODALIDADE_PLANO", ""));
        meuPlano.setMinMOStr(sharedPref.getString(BASEKEY + "MIN_MO_STR", ""));
        meuPlano.setMinOOStr(sharedPref.getString(BASEKEY + "MIN_OO_STR", ""));
        meuPlano.setMinIUStr(sharedPref.getString(BASEKEY + "MIN_IU_STR", ""));
        meuPlano.setMinFixoStr(sharedPref.getString(BASEKEY + "MIN_FIXO_STR", ""));
        meuPlano.setSmsInclusosStr(sharedPref.getString(BASEKEY + "SMS_INCLUSO_STR", ""));
        meuPlano.setSmsExtrasStr(sharedPref.getString(BASEKEY + "SMS_EXTRA_STR", ""));
        meuPlano.setLimiteDadosWebStr(sharedPref.getString(BASEKEY + "LIMITE_WEB_STR", ""));

        return meuPlano;
    }

    public void setMeuPlanoClass(Plano plano) {
        final SharedPreferences.Editor editor = sharedPref.edit();
        final String BASEKEY = "MEUPLANO_CLASS__";

        editor.putInt(BASEKEY + "ID_PLANO", plano.getIdPlano());
        editor.putInt(BASEKEY + "ID_OPERADORA", plano.getIdOperadora());
        editor.putInt(BASEKEY + "ID_MODALIDADE_PLANO", plano.getIdModalidadePlano());
        editor.putInt(BASEKEY + "ID_TIPO_PLANO", plano.getIdTipoPlano());
        editor.putInt(BASEKEY + "ID_DDD", plano.getIdDDD());
        editor.putInt(BASEKEY + "MIN_MO", plano.getMinMO());
        editor.putInt(BASEKEY + "MIN_OO", plano.getMinOO());
        editor.putInt(BASEKEY + "MIN_IU", plano.getMinIU());
        editor.putInt(BASEKEY + "MIN_FIXO", plano.getMinFixo());
        editor.putInt(BASEKEY + "SMS_INCLUSO", plano.getSmsInclusos());
        editor.putInt(BASEKEY + "DT_VENCIMENTO", plano.getDtVencimento());
        editor.putInt(BASEKEY + "ID_PLANO_REFERENCIA", plano.getIdPlanoReferencia());
        editor.putLong(BASEKEY + "LIMITE_WEB", plano.getLimiteDadosWeb());
        editor.putFloat(BASEKEY + "SMS_EXTRA", plano.getSmsExtras());
        editor.putFloat(BASEKEY + "VALOR_PLANO", plano.getValorPlano());
        editor.putFloat(BASEKEY + "BONUS", plano.getBonus());
        editor.putString(BASEKEY + "NOME_PLANO", plano.getNomePlano());
        editor.putString(BASEKEY + "OBSERVACAO", plano.getObservacao());
        editor.putString(BASEKEY + "NOME_OPERADORA", plano.getNomeOperadora());
        editor.putString(BASEKEY + "DESCRICAO_TIPO_PLANO", plano.getDescricaoTipoPlano());
        editor.putString(BASEKEY + "DESCRICAO_MODALIDADE_PLANO", plano.getDescricaoModalidadePlano());
        editor.putString(BASEKEY + "MIN_MO_STR", plano.getMinMOStr());
        editor.putString(BASEKEY + "MIN_OO_STR", plano.getMinOOStr());
        editor.putString(BASEKEY + "MIN_IU_STR", plano.getMinIUStr());
        editor.putString(BASEKEY + "MIN_FIXO_STR", plano.getMinFixoStr());
        editor.putString(BASEKEY + "SMS_INCLUSO_STR", plano.getSmsInclusosStr());
        editor.putString(BASEKEY + "SMS_EXTRA_STR", plano.getSmsExtrasStr());
        editor.putString(BASEKEY + "LIMITE_WEB_STR", plano.getLimiteDadosWebStr());
        editor.commit();
    }
}
