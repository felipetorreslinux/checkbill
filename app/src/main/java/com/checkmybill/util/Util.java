package com.checkmybill.util;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;

import com.checkmybill.R;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.SignalStrengthAverage;
import com.checkmybill.receiver.ReceiverMain;
import com.checkmybill.service.TrafficMonitor;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Victor Guerra on 25/11/2015.
 */
public class Util {

    public static final int REGISTER_USE_DATA_ALARM_ID = 1;
    public static final int MEASURE_SIGNAL_STRENGTH_ALARM_ID = 2;
    public static final int UNAVAILABILITY_ALARM_ID = 3;
    public static final int CONF_MOB_ALARM_ID = 4;
    public static final int WIFI_MONITOR_ALARM_ID = 5;
    public static final int MY_POSITION_ID = 6;

    private static final long INTERVAL_TIME_ALARM = 1000 * 60 * 60;

    /**
     * Petrus A.
     * Obtem o tempo formatado no padrão HH:MM:SS de um valor de mili-segundos
     *
     * @param msecs
     * @return
     */
    public static String getFormattedTimeFromMSeconds(final long msecs) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return formatter.format(new Date(msecs));
    }

    /**
     * Petrus A.
     * Obtem o tempo formatado no padrão (definodo no argumento) de um valor de mili-segundos
     *
     * @param msecs
     * @param format
     * @return
     */
    public static String getFormattedTimeFromMSeconds(final long msecs, final String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        return formatter.format(new Date(msecs));
    }

    /**
     * Petrus A.
     * Obtem o Date de um dia especfico (YYYY-MM-DD)
     *
     * @param dtStr
     * @return
     * @throws ParseException
     */
    public static Date getDateFromString(final String dtStr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dtStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Petrus A.
     * Obtem o Date de um dia especfico que pode ser formtado (argumento: format)
     *
     * @param dtStr
     * @return
     * @throws ParseException
     */
    public static Date getDateFromString(final String dtStr, final String format) {
        try {
            return new SimpleDateFormat(format).parse(dtStr);
        } catch (ParseException e) {
            return null;
        }
    }
/*
    public static void defineAlarmReceiverMain(Context context) {
        PendingIntent pendingIntent;
        AlarmManager alarmMgr;

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm = new Intent(context, ReceiverMain.class);
        intentAlarm.setAction(context.getString(R.string.intent_action_use_data_alarm));
        pendingIntent = PendingIntent.getBroadcast(context, REGISTER_USE_DATA_ALARM_ID, intentAlarm, PendingIntent.FLAG_ONE_SHOT);
        if ( pendingIntent == null ) { // Alarme ja criado
            Log.d(Util.class.getName(), "Alarm Already Created: defineAlarmReceiverMain");
            return;
        } else {
            Log.d(Util.class.getName(), "Alarm Created: defineAlarmReceiverMain");
        }

        DateTime dateTimeCurrent = new DateTime().plusMinutes(5);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, dateTimeCurrent.getMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);
    }
*/
    public static void defineAlarmMeasureSignalStrength(Context context) {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReceiverMain.class);
        intent.setAction(context.getString(R.string.intent_action_measure_signal_strength_alarm));
        alarmIntent = PendingIntent.getBroadcast(context, MEASURE_SIGNAL_STRENGTH_ALARM_ID, intent, PendingIntent.FLAG_ONE_SHOT);
        if ( alarmIntent == null ) {// Alarme ja criado
            Log.d(Util.class.getName(), "Alarm Already Created: defineAlarmMeasureSignalStrength");
            return;
        } else {
            Log.d(Util.class.getName(), "Alarm Created: defineAlarmMeasureSignalStrength");
        }

        try {
            Dao<SignalStrengthAverage, Integer> signalStrengthAverageDao = OrmLiteHelper.getInstance(context).getSignalStrengthAverageDao();

            List<SignalStrengthAverage> signalStrengthAverageList = signalStrengthAverageDao.queryForAll();
            if (!signalStrengthAverageList.isEmpty()) {
                Log.i("UTIL ALARM", "LIST RESUL NO EMPTY");
                SignalStrengthAverage signalStrengthAverage = signalStrengthAverageList.get(signalStrengthAverageList.size() - 1);

                DateTime signalData = new DateTime(signalStrengthAverage.getDate());
                signalData = signalData.plusHours(1);
                DateTime currentDate = new DateTime();

                Log.i("UTIL ALARM", "LAST SIGNAL DATE: " + signalData.toString());
                Log.i("UTIL ALARM", "CURRENT DATE: " + currentDate.toString());

                int resultCompare = currentDate.compareTo(signalData);
                long diff = signalData.getMillis() - currentDate.getMillis();

                Log.i("UTIL ALARM", "DIFF: " + diff);
                Log.i("UTIL ALARM", "RESULT COMPARE: " + resultCompare);

                if (resultCompare < 0) {
                    Log.i("UTIL ALARM", "DIFF MENOR");
                    //Current
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, signalData.getMillis(),
                            INTERVAL_TIME_ALARM, alarmIntent);
                    Log.i("UTIL ALARM", "SCHENCULE DATE: " + signalData.getMillis());
                } else {
                    Log.i("UTIL ALARM", "DIFF MAIOR");
                    //Current Finished
                    //context.startService(new Intent(context, ServiceSignalStrengthGET.class));
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, new DateTime().getMillis() + 10000,
                            INTERVAL_TIME_ALARM, alarmIntent);
                }


                /*

                if(diff < INTERVAL_TIME_ALARM){
                    Log.i("UTIL ALARM", "DIFF MENOR");
                    //Current
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, 8);
                    calendar.set(Calendar.MINUTE, 30);

                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, signalData.getMillis(),
                            INTERVAL_TIME_ALARM, alarmIntent);
                    Log.i("UTIL ALARM", "SCHENCULE DATE: " + signalData.getMillis());
                }else if(diff == INTERVAL_TIME_ALARM || diff > INTERVAL_TIME_ALARM){
                    Log.i("UTIL ALARM", "DIFF MAIOR");
                    //Current Finished
                    //context.startService(new Intent(context, ServiceSignalStrengthGET.class));
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, new DateTime().getMillis() + 10000,
                            INTERVAL_TIME_ALARM, alarmIntent);
                }*/
            } else {
                Log.i("UTIL ALARM", "LIST EMPTY. NEW");
                //new
                //context.startService(new Intent(context, ServiceSignalStrengthGET.class));
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, new DateTime().getMillis() + 10000,
                        INTERVAL_TIME_ALARM, alarmIntent);
            }

            Log.i("DAOOOOOO LOG", "Content: " + alarmMgr.toString());
        } catch (SQLException e) {
            Log.e(Util.class.getName(), e.getMessage());
        }

        Log.i("ALARM STATUS", "Alarm measure signal strength create");
    }

    /*public static void defineAlarmGetConfMob(Context context) {
        final Intent intentAlarm = new Intent(context, ReceiverMain.class);
        intentAlarm.setAction(context.getString(R.string.intent_action_conf_mob_alarm));

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, CONF_MOB_ALARM_ID, intentAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
        final AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, AlarmManager.INTERVAL_DAY, pendingIntent);
    }*/

    public static void definirAlarmSaveMyPosition(Context context) {
        final AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final Intent intentAlarm = new Intent(context, ReceiverMain.class);
        intentAlarm.setAction(context.getString(R.string.intent_action_save_my_position_alarm));
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MY_POSITION_ID, intentAlarm, PendingIntent.FLAG_CANCEL_CURRENT);

        // Criando o alarm...
        final long startDaley = System.currentTimeMillis() + (1000 * 10);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, startDaley, AlarmManager.INTERVAL_HOUR, pendingIntent);
        Log.d(Util.class.getName(), "Alarm Created: definirAlarmSaveMyPosition");
    }

    public static boolean uploadSignalStrength(List<SignalStrengthAverage> result, Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        Log.i("STATUS NETWORK", "Status: " + isConnected);
        return true;
    }

    public static List<SignalStrengthAverage> insertSignalStrengthAverage(Context context) {
        Log.i("LOG ALARM", "alarm in Util.insert");
        Dao<SignalStrengthAverage, Integer> signalStrengthAverageDao;
        try {
            signalStrengthAverageDao = OrmLiteHelper.getInstance(context).getSignalStrengthAverageDao();

            UpdateBuilder<SignalStrengthAverage, Integer> updateBuilder = signalStrengthAverageDao.updateBuilder();
            updateBuilder.where().eq("SIG_STR_AVE_CUR", true).or()
                    .eq("SIG_STR_AVE_SAVED", false);

            updateBuilder.updateColumnValue("SIG_STR_AVE_CUR", false);
            updateBuilder.update();

            List<SignalStrengthAverage> signalStrengthAverageList = signalStrengthAverageDao.queryBuilder()
                    .where().eq("SIG_STR_AVE_SAVED", false)
                    .query();

            Util.uploadSignalStrength(signalStrengthAverageList, context);

            return signalStrengthAverageList;

        } catch (SQLException e) {
            e.printStackTrace();
            Log.i("LOG ALARM", "alarm in Util.insert EXCEPTION");
        }

        return null;

    }

    public static void defineUnavailability(Context context) {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReceiverMain.class);
        intent.setAction(context.getString(R.string.intent_action_unavailability_alarm));
        alarmIntent = PendingIntent.getBroadcast(context, UNAVAILABILITY_ALARM_ID, intent, PendingIntent.FLAG_ONE_SHOT);
        if ( alarmIntent == null ) { // Alarme ja criado
            Log.d(Util.class.getName(), "Alarm Already Created: defineUnavailability");
            return;
        } else {
            Log.d(Util.class.getName(), "Alarm Created: defineUnavailability");
        }

        DateTime currentDate = new DateTime();

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, currentDate.getMillis() + 10000,
                1000 * 60 * 360, alarmIntent);

        Log.i("ALARM STATUS", "Alarm defineUnavailability create in " + new DateTime(currentDate.getMillis() + 10000).toString());
    }

    public static void defineWifiMonitorAlarm(Context context) {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReceiverMain.class);
        intent.setAction(context.getString(R.string.intent_action_wifi_monitor_alarm));
        alarmIntent = PendingIntent.getBroadcast(context, WIFI_MONITOR_ALARM_ID, intent, 0);

        DateTime currentDate = new DateTime();

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, currentDate.getMillis() + 30000,
                1000 * 60 * 30, alarmIntent);

        Log.i("ALARM STATUS", "Alarm defineWifiMonitorAlarm create in " + new DateTime(currentDate.getMillis() + 30000).toString());
    }

    public static boolean isConnectionTrue(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo activeWifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
        boolean isWifiConnected = activeWifiInfo != null && activeWifiInfo.isConnectedOrConnecting();

        if (isConnected || isWifiConnected) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isWifiConnectionTrue(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeWifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        boolean isWifiConnected = activeWifiInfo != null && activeWifiInfo.isConnectedOrConnecting();

        return isWifiConnected;
    }

    public static String getSuperUrl(Context context) {
        return context.getString(R.string.url_base);
    }

    public static String getSubFoldersURI() {
        return "/services";
    }

    public static String getSuperUrlServiceEnviarColabore(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/enviar-colabore";
    }

    public static String getSuperUrlServiceAddData(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/adicionar-dados-gsm";
    }

    public static String getSuperUrlServiceGetInfoGSM(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/informacao-regiao-gsm";
    }

    public static String getSuperUrlServiceGetInfoBandaLarga(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/informacao-regiao-blarga";
    }

    public static String getSuperUrlServiceObterRecargasPlanoUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-recargas";
    }

    public static String getSuperUrlServiceAdicionarRecargaPlanoUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/adicionar-recarga";
    }

    public static String getSuperUrlServiceRemoverRecargaPlanoUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/remover-recarga";
    }

    public static String getSuperUrlServiceObterPlanoUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-detalhes-plano-usuario";
    }

    public static String getSuperUrlServiceSalvarPlanoUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/adicionar-plano-usuario";
    }

    public static String getSuperUrlServiceCompararPlanoUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/comparar-plano-usuario";
    }

    public static String getSuperUrlServiceObterDetalhesPlano(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-detalhes-plano-operadora";
    }

    public static String getSuperUrlServiceObterListaPlanosSimples(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-lista-planos";
    }

    public static String getSuperUrlServiceObterListaPacotes(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-lista-pacotes";
    }

    public static String getSuperUrlServiceAnexarPacotePlanoUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/anexar-pacote-plano-usuario";
    }

    public static String getSuperUrlServiceRemoverPacotePlanoUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/remover-pacotes-plano-usuario";
    }

    public static String getSuperUrlServiceObterPacotesPlanoUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-pacotes-plano-usuario";
    }

    public static String getSuperUrlServiceObterFiltrosBasePlanos(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-filtros-base-planos";
    }

    public static String getSuperUrlServiceIntegrarFacebookUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/integrar-facebook-usuario";
    }

    public static String getSuperUrlServiceAtualizarInfoUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/atualizar-info-usuario";
    }

    public static String getSuperUrlServiceObterNomePorvedor(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-nome-provedor";
    }

    public static String getSuperUrlServiceAdicionaDadosVelocidade(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/adicionar-dados-blarga";
    }

    public static String getSuperUrlServiceGetConfMob(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-config-mob";
    }

    public static String getSuperUrlServiceObterMediaRegiao(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-media-regiao";
    }

    public static String getSuperUrlServiceObterMediaGeralBLarga(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-media-geral-blarga";
    }

    public static String getSuperUrlServiceAdicionarDadosMonitoramento(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/adicionar-dados-geral-monitoramento";
    }

    public static String getSuperUrlServiceGerarRelatorioConsumo(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/gerar-relatorio-consumo";
    }

    public static String getSuperUrlServiceRealizarTesteUpload(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/realizar-teste-upload";
    }
    public static String getSuperUrlServiceObterRankingGsm(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-ranking-gsm";
    }
    public static String getSuperUrlServiceObterRankingBandaLarga(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-ranking-blarga";
    }

    // -- Not Implemented on Server
    /*public static String getSuperUrlServiceGetInfoAnalyticsList(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/estatisticas-aparelho";
    }
    */
    /*public static String getSuperUrlServiceAvaliarPlanoUsuario(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/avaliar-plano-usuario";
    }*/

    public static String getSuperUrlServiceObterDadosSincronizacao(Context context) {
        return getSuperUrl(context) + getSubFoldersURI() + "/obter-dados-sincronizacao";
    }

    public static String getSuperUrlSpeedTestDownload(Context context) {
        return getSuperUrl(context) + "/speed-test/20MB.zip";
    }

    public static boolean isEmailValid(String email) {
        if ((email == null) || (email.trim().length() == 0))
            return false;

        String emailPattern = "\\b(^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@([A-Za-z0-9-])+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z0-9]{2,})|(\\.[A-Za-z0-9]{2,}\\.[A-Za-z0-9]{2,}))$)\\b";
        Pattern pattern = Pattern.compile(emailPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /*public static void restartDataUseMonitor(Context context){
        try {
            TableUtils.clearTable(OrmLiteHelper.getInstance(context).getConnectionSource(), DataUse.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm = new Intent(context, ReceiverMain.class);
        intentAlarm.setAction(context.getString(R.string.intent_action_use_data_alarm));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Util.REGISTER_USE_DATA_ALARM_ID, intentAlarm, 0);
        alarmMgr.cancel(pendingIntent);

        Util.defineAlarm(context);
    }*/

    public static boolean serviceIsRunning(Context context, Class<?> serviceClass) {
        ActivityManager manger = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manger.getRunningServices(Integer.MAX_VALUE) ) {
            if ( serviceClass.getName().equals( serviceInfo.service.getClassName()) ) {
                return true;
            }
        }

        Log.d(Util.class.getName(), "Service Is Not Running: " + serviceClass.getName());
        return false;
    }

    public static String getMessageErrorFromExcepetion(Exception e){
        String msgError = "";

        try {
            String msn = e.getMessage();
            if(msn != null && msn.length() > 0){
                msgError = msn;
            }else{
                msgError = "Sem mensagem de erro";
            }
        }catch (Exception ex){
            msgError = "Sem mensagem de erro";
        }

        return msgError;
    }



    /*public static void newDataUseInBoot(Context context) {
        long totalUp = TrafficStats.getTotalTxBytes();
        long totalDown = TrafficStats.getTotalRxBytes();

        try {
            if (totalUp != TrafficStats.UNSUPPORTED && totalDown != TrafficStats.UNSUPPORTED) {
                long mobUp = TrafficStats.getMobileTxBytes();
                long mobDown = TrafficStats.getMobileRxBytes();

                long wifiUp = totalUp - mobUp;
                long wifiDown = totalDown - mobDown;
                //long wifiUp = totalUp;
                //long wifiDown = totalDown;

                DataUse dataUse = new DataUse();
                dataUse.setDate(new DateTime().toDate());
                dataUse.setMobileUpStart(mobUp);
                dataUse.setMobileUpEnd(mobUp);

                dataUse.setMobileDownStart(mobDown);
                dataUse.setMobileDownEnd(mobDown);

                dataUse.setWifiUpStart(wifiUp);
                dataUse.setWifiUpEnd(wifiUp);

                dataUse.setWifiDownStart(wifiDown);
                dataUse.setWifiDownEnd(wifiDown);

                try {
                    Dao<DataUse, Integer> dataUseDao = OrmLiteHelper.getInstance(context).getDataUseDao();
                    dataUseDao.create(dataUse);

                    List<DataUse> dataUses = dataUseDao.queryBuilder().where().eq("DAT_USE_DATA", dataUse.getDate()).query();
                    DataUse dataUseDb = dataUses.get(0);

                    new SharedPrefsUtil(context).setSfCurrentDataUseId(dataUseDb.getId());

                    Log.e("Util|iniConfigDataUse", "New dateUse created: " + dataUse.getId() + " | " + dataUse.getDate().toString());
                } catch (SQLException e) {
                    Log.e("Util|iniConfigDataUse", "Error: " + e.getMessage());
                }
            }

        } catch (RuntimeException e) {
            Log.e("Util|iniConfigDataUse", "Error: " + e.getMessage());
        }
    }*/

    /*public static void finishDataUseInShutdown(Context context) {
        long totalUp = TrafficStats.getTotalTxBytes();
        long totalDown = TrafficStats.getTotalRxBytes();

        try {
            if (totalUp != TrafficStats.UNSUPPORTED && totalDown != TrafficStats.UNSUPPORTED) {
                long mobUp = TrafficStats.getMobileTxBytes();
                long mobDown = TrafficStats.getMobileRxBytes();

                long wifiUp = totalUp - mobUp;
                long wifiDown = totalDown - mobDown;

                try {
                    Dao<DataUse, Integer> dataUseDao = OrmLiteHelper.getInstance(context).getDataUseDao();
                    List<DataUse> dataUses = dataUseDao.queryBuilder().where()
                            .eq("DAT_USE_ID", new SharedPrefsUtil(context).getSfCurrentDataUseId())
                            .query();

                    DataUse dataUseDb = dataUses.get(0);
                    dataUseDb.setMobileUpEnd(mobUp);
                    dataUseDb.setMobileDownEnd(mobDown);
                    dataUseDb.setWifiUpEnd(wifiUp);
                    dataUseDb.setWifiDownEnd(wifiDown);

                    dataUseDao.update(dataUseDb);

                    Log.e("Util|iniConfigDataUse", "Finish dateUse: " + dataUseDb.getId() + " | " + dataUseDb.getDate().toString());
                } catch (SQLException e) {
                    Log.e("Util|iniConfigDataUse", "Error: " + e.getMessage());
                }
            }

        } catch (RuntimeException e) {
            Log.e("Util|iniConfigDataUse", "Error: " + e.getMessage());
        }

    }*/

    public static void exportDatabase() {
        File sd = Environment.getExternalStorageDirectory();
        if (sd.canWrite()) {
            String currentDBPath = "/data/data/com.checkmybill.app/databases";
            File currentDB = new File(currentDBPath, "check-my-bill.db");
            File backupDB = new File(sd, "check-my-bill.db" + "_" + new LocalTime().toString() + ".db");

            if (currentDB.exists()) {
                FileChannel src;
                try {
                    src = new FileInputStream(currentDB).getChannel();

                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }/*

    public static void importDatabase() {
        File sd = Environment.getExternalStorageDirectory();
        if (sd.canWrite()) {
            String currentDBPath = "/data/data/com.checkmybill.app/databases";

            File currentDB = new File(sd.getPath(), "check-my-bill.db");

            File backupDB = new File(currentDBPath, "check-my-bill.db");

            if (currentDB.exists()) {
                FileChannel src;
                try {
                    src = new FileInputStream(currentDB).getChannel();

                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isSimSupport(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
        return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
    }*/

    public static float getPercentValue(long total, long used) {
        if ( used >= total ) return 100f;
        else if ( total == 0 ) return 100f;
        else if ( used == 0 ) return 0.1f;
        float base = (float) used / total;
        return (base * 100f);
    }


    public static String convertBytesToStr(Context context, long bytes) {
        return TrafficMonitor.FormatBytes(context, bytes);
    }

    public static class DatesUtil {
        public static Date getCurrentDate() {
            return new Date();
        }

        public static Date getDecrementedDate(int num2dec ) {
            Date currentDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime( currentDate );
            cal.add( Calendar.DATE, (num2dec * -1) );
            return cal.getTime();
        }

        public static Date getDecrementedMonth(int num2dec) {
            Date currentDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime( currentDate );
            cal.add( Calendar.MONTH, (num2dec * -1) );
            return cal.getTime();
        }
    }
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
