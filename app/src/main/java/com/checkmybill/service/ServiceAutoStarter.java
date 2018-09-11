package com.checkmybill.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petrus A, on 29/11/2016.
 */

public class ServiceAutoStarter {
    private String LOG_TAG;
    private Context activity;

    // Lista de serviocos a serem inicializados
    /**
     * @Imporant: ISSUE137, houve uma alteração, onde, agora, além de iniciar o serviço de obtenção
     * de dados iniciais no AutoStarter, agora, ao requisitar (e receber) a permissão
     * o serviço de leitura de dados iniciais é disparada, passando como argumento, qual dados a ser
     * lido, usando como base a permissão concedida.
     */
    private List<Class> serviceClassList = new ArrayList<Class>(){{
        add( ServicePhoneStateListener.class );
        add( ServiceSMSOutgoingMonitor.class );
        add( ServiceCallMonitor.class );
        add( ServiceConfMob.class );
        add( ServiceWifiMonitor.class );
        add ( ServiceSaveMyPosition.class );
    }};

    public ServiceAutoStarter(Context activity) {
        this.activity = activity;
        this.LOG_TAG = getClass().getName();
        Log.d(LOG_TAG, "Instance initialized");
    }

    /*public void initializeAllServiceAndAlarms() {
        this.initializeCheckbillServices();
        this.initializeCheckbillAlarms();
    }*/

    public void stopAllServices() {
        for ( int i = 0; i < this.serviceClassList.size(); i++ )
            activity.stopService(new Intent(activity, this.serviceClassList.get(i)));
    }
    /**
     * Este metodo garante a inicialização dos Servicos de monitoracao
     * Petrus Augusto (28-09-2016)
     */
    public void initializeCheckbillServices() {
        Log.i(LOG_TAG, "Initializaing " + serviceClassList.size() + " services");
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(activity);
        sharedPrefsUtil.setConfMob2g(-105);
        sharedPrefsUtil.setConfMob4g(-125);
        sharedPrefsUtil.setConfMobTimeUnavailability(10000);

        /* Lembre-se, a lista de serviços a serem inicializados (e consequenteemnte, parados),
           se encontra como 'public static', e é inicializada no metodos onCreate desta Activity */
        for ( int i = 0; i < serviceClassList.size(); i++ ) {
            if ( i == 0 ) activity.startService(new Intent(activity, serviceClassList.get(i)));
            else if ( ! Util.serviceIsRunning(activity, serviceClassList.get(i)) ) {
                Log.i(LOG_TAG, "Starting service: " + serviceClassList.get(i).getName());
                activity.startService(new Intent(activity, serviceClassList.get(i)));
            }
        }
    }

    /**
     * Este metodo inicializa os alarms caso nao existam...
     */
    public void initializeCheckbillAlarms() {
        Log.d(LOG_TAG, "Initializing Alarms");
        Util.defineAlarmMeasureSignalStrength(activity);
        Util.defineUnavailability(activity);
        //Util.defineAlarmGetConfMob(activity);
        //Util.definirAlarmSaveMyPosition(activity);
        new SharedPrefsUtil(activity).setAlarmSignalStrengthLoad(true);
    }
}
