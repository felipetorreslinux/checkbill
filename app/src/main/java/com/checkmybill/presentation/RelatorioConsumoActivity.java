package com.checkmybill.presentation;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.CallMonitor;
import com.checkmybill.entity.MyLocationMonitor;
import com.checkmybill.entity.Plano;
import com.checkmybill.entity.SmsMonitor;
import com.checkmybill.entity.Unavailability;
import com.checkmybill.request.MedicoesRequester;
import com.checkmybill.service.TrafficMonitor;
import com.checkmybill.util.DatePickers;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@EActivity(R.layout.activity_relatorio_consumo)
public class RelatorioConsumoActivity extends AppCompatActivity {
    private static String LOG_TAG;
    public static String PLANO_EXTRA = "PLANO_EXTRA";

    @ViewById(R.id.reportHtmlPage) protected WebView reportHtmlPage;
    @ViewById(R.id.layoutProgress) protected LinearLayout layoutProgress;
    @ViewById(R.id.baseContentLayout) protected LinearLayout baseContentLayout;
    @ViewById(R.id.cardMenuDots) protected ImageView cardMenuDots;
    @ViewById(R.id.header_periodoText) protected TextView header_periodoText;

    private Plano meuPlano;
    private Date periodoDate;
    private Context mContext;

    private double[] coordenates;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    private RequestQueue requestQueue;

    @Override
    public void onStop() {
        super.onStop();
        requestQueue.cancelAll(getClass().getName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.LOG_TAG = getClass().getName();
        this.mContext = this;

        this.myLocationListener = new MyLocationListener();

        // Obtendo o plano...
        Intent it = getIntent();
        meuPlano = (Plano) it.getSerializableExtra(PLANO_EXTRA);

        // Inicializando restante das variaveis...
        this.periodoDate = new Date();
        this.requestQueue = Volley.newRequestQueue(this);

        // Definindo valores do WebView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Obtendo a posicao/coordenada
        // Nota -> Tentando obter a última posicao obtida/salva no aplicativo...
        this.getMyLastPosition();
    }

    private void getMyLastPosition() {
        showLoadingLayout(true);
        try {
            Dao<MyLocationMonitor, Integer> myLocationMonitorDao = OrmLiteHelper.getInstance(this).getMyLocationMonitorDao();
            List<MyLocationMonitor> myLastLocation = myLocationMonitorDao.queryBuilder().limit(1).orderBy("LOCATION_ID", false).query();
            if ( myLastLocation.size() <= 0 ) {
                getMyREALPosition();
            } else {
                MyLocationMonitor lastLocation = myLastLocation.get(0);
                coordenates = new double[] {lastLocation.getLat(), lastLocation.getLng()};
                loadReportData();
            }
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error -> " + e.getMessage());
            getMyREALPosition();
        }
    }

    private void getMyREALPosition() {
        // Checando a permissão de obtenção da posição
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
            Log.d(LOG_TAG, "Requested REAL position");
            showLoadingLayout(true);
        } else {
            // Requisitando a permissão...
            new NotifyWindow(this).showErrorMessage("Relatório", "Não há permissão para obter as coordenadas, dê permissão ao aplicativo de acessar o GPS e tente novamente", false, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Eventos dos elementos visuais (Click, Change, etc...)
    @Click(R.id.cardMenuDots)
    public void cardMenuDotsClick() {
        PopupMenu popup = new PopupMenu(this, cardMenuDots);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_relatorio, popup.getMenu());
        popup.setOnMenuItemClickListener(this.onMenuItemClick);
        popup.show();
    }

    PopupMenu.OnMenuItemClickListener onMenuItemClick = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_modificar_periodo:
                    showPeriodoFilterWindow();
                    break;
                case R.id.compartilhar:
                    shareHtmlImage();
                    break;
            }

            return false;
        }
    };

    // ---------------------------------------------------------------------------------------------
    private Response.Listener successRequestListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(LOG_TAG, "Response:" + response.toString());

            try {
                if (response.getString("status").equalsIgnoreCase("success") == false) {
                    Log.e(LOG_TAG, response.getString("message"));
                    new NotifyWindow(mContext).showErrorMessage("Relatório", response.getString("message"), false);
                    return;
                }

                // Adicionando conteudo ao webview
                reportHtmlPage.getSettings().setJavaScriptEnabled(true);
                reportHtmlPage.loadData(response.getString("html_data"), "text/html; charset=utf-8", "UTF-8");
                showLoadingLayout(false);
            } catch (JSONException e) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }
    };

    private Response.ErrorListener errorRequestListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            showLoadingLayout(false);
            Log.e(LOG_TAG, error.getMessage());
            new NotifyWindow(mContext).showErrorMessage("Erro", error.getMessage(), false);
        }
    };

    // ---------------------------------------------------------------------------------------------
    // Metodos privados da classe
    private void showPeriodoFilterWindow() {
        DatePickers.MonthAndYearDatePicker monthAndYearDatePicker = new DatePickers.MonthAndYearDatePicker(this);
        monthAndYearDatePicker.setTitle("Período");
        monthAndYearDatePicker.setInitialDateRangeValue(this.periodoDate);
        monthAndYearDatePicker.setPositiveEvent(new DatePickers.MonthAndYearDatePickerInterface.OnPositiveEvent() {
            @Override
            public void onEvent(Date date, DialogInterface dialogInterface, int i) {
                // Definindo o novo periodo e recarregando os dados...
                periodoDate = date;
                dialogInterface.dismiss();
                loadReportData();
            }
        });
        monthAndYearDatePicker.showDialogWindow();
    }

    private void loadReportData() {
        // Definindo o texto do periodo...
        SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy");
        header_periodoText.setText(sdf.format(periodoDate));

        // Loading...
        showLoadingLayout(true);

        // Obtendo os dados não sincronizados...
        LoadStaticInfos loadLocalStaticInfo = new LoadStaticInfos();
        loadLocalStaticInfo.execute(periodoDate);
    }

    private void requestReportPage(int incSMS, long incCall, double incOfflineMinutes, long incBytesWeb) {
        JsonObjectRequest request = MedicoesRequester.prepareGerarRelatorioConsumo(successRequestListener, errorRequestListener, periodoDate, incSMS, incCall, incOfflineMinutes, incBytesWeb, coordenates[0], coordenates[1], mContext);
        request.setTag(getClass().getName());
        requestQueue.add(request);
    }

    private boolean loadingIsShowing() {
        return (layoutProgress.getVisibility() == View.VISIBLE);
    }

    private void showLoadingLayout(boolean show) {
        if (show) {
            baseContentLayout.setVisibility(View.GONE);
            layoutProgress.setVisibility(View.VISIBLE);
        } else {
            layoutProgress.setVisibility(View.GONE);
            baseContentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void shareHtmlImage() {
        try {
            Log.d(LOG_TAG, "Definindo o arquivo a ser gerado (screenshot)");
            final File destinyTmpFile = new File(getCacheDir(), "Checkbill_Report_Buff.jpg");
            if ( !destinyTmpFile.exists() ) {
                destinyTmpFile.getParentFile().mkdirs();
                destinyTmpFile.createNewFile();
            } else if ( destinyTmpFile.isDirectory() ) {
                destinyTmpFile.delete();
                destinyTmpFile.createNewFile();
            }

            // Craindo Bitmap com o conteudo da pagina
            Log.d(LOG_TAG, "Tirando o screenshot do conteudo do WebView");
            reportHtmlPage.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            reportHtmlPage.layout(0, 0, reportHtmlPage.getMeasuredWidth(), reportHtmlPage.getMeasuredHeight());
            reportHtmlPage.setDrawingCacheEnabled(true);
            reportHtmlPage.buildDrawingCache();
            final Bitmap bitmap = Bitmap.createBitmap(reportHtmlPage.getMeasuredWidth(), reportHtmlPage.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            canvas.drawBitmap(bitmap, 0, bitmap.getHeight(), paint);
            reportHtmlPage.draw(canvas);

            // Gerando arquivo de saida, o que sera enviado/compartilhado...
            Log.d(LOG_TAG, "Salvando Bitmap gerado dentro do arquivo de buffer");
            FileOutputStream fos = new FileOutputStream(destinyTmpFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
            fos.close();
            destinyTmpFile.setReadable(true, false);

            // Enviando imagem para ser compartilhada...
            Log.d(LOG_TAG, "Lançando o 'Share' para o compartilhamento do arquivo");
            Intent sharingIntent = new Intent();
            sharingIntent.setAction(Intent.ACTION_SEND);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, "Relatório do CheckBill");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Relatório do CheckBill");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(destinyTmpFile));
            sharingIntent.setType("image/jpeg");
            startActivity(sharingIntent);
        } catch ( Exception ex ) {
            Log.e(LOG_TAG, ex.getMessage());
            new NotifyWindow(mContext).showErrorMessage(getString(R.string.app_name), "Não foi possível compartilhar o relatório", false);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // AsyncTask's, ler os dados do armazenados no banco de dados...
    private class LoadStaticInfos extends AsyncTask<Date, Void, Object> {
        private class ResponseClassInfo {
            int smsSended = 0;
            long totalSecondsInCall = 0;
            double unavailabilityMinutes = 0;
            long totalBytesTransferidos;
        }

        OrmLiteHelper orm;

        @Override
        public void onPreExecute() {
            orm = OrmLiteHelper.getInstance(mContext);
        }

        @Override
        protected Object doInBackground(Date... params) {
            if (params == null || params.length <= 0) {
                Log.e(LOG_TAG, "No Periodo Date");
                return null;
            }

            ResponseClassInfo responseClassInfo = new ResponseClassInfo();
            SimpleDateFormat sdfToDB = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfToStr = new SimpleDateFormat("yyyy-MM");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(params[0]);

            final String strDate = sdfToStr.format(params[0]);
            Log.d(LOG_TAG, "Consulting local data from database");
            try {
                Date[] range = {
                        sdfToDB.parse(strDate + "-01"),
                        sdfToDB.parse(strDate + "-" + calendar.getActualMaximum(Calendar.DATE))
                };

                // -> Obtendo os dados de ligacao
                List<CallMonitor> callMonitorData = orm.getCallMonitorDao().queryBuilder().
                        where().between("CALL_DAT_CAD", range[0], range[1]).
                        and().eq("CALL_TYPE", "OUTGOING").
                        and().eq("CALL_SENDED", false).query();
                Log.d(LOG_TAG, "CALLCount -> " + callMonitorData.size());
                for (CallMonitor callInfo : callMonitorData) {
                    responseClassInfo.totalSecondsInCall += callInfo.getElapsedTime();
                }

                // -> Obtendo os dados de SMS
                List<SmsMonitor> smsMonitorData = orm.getSmsMonitorDao().queryBuilder().
                        where().between("SMS_DAT_CAD", range[0], range[1]).
                        and().eq("SMS_SENDED", false).query();
                Log.d(LOG_TAG, "SMSCount -> " + smsMonitorData.size());
                responseClassInfo.smsSended = smsMonitorData.size();

                // -> Obtendo os dados de trafego web
                responseClassInfo.totalBytesTransferidos = new TrafficMonitor(mContext).getTotalMobileDataTransfer(range[0], range[1]);

                // -> Obtendo os dados de indisponibilidade
                List<Unavailability> listUnavailabilities = null;
                RuntimeExceptionDao<Unavailability, Integer> unavailabilitiesDao = OrmLiteHelper.getInstance(mContext).getUnavailabilityRuntimeDao();
                listUnavailabilities = unavailabilitiesDao.queryBuilder()
                        .where().between("UNAVA_DATE_STARTED", range[0], range[1])
                        .and().eq("UNAVA_USED", false)
                        .query();
                Log.d(LOG_TAG, "UnavailabilityCount -> " + String.valueOf(listUnavailabilities.size()));

                if (listUnavailabilities != null && !listUnavailabilities.isEmpty()) {
                    for (Unavailability unavailability : listUnavailabilities) {
                        DateTime dateInicio = new DateTime(unavailability.getDateStarted());
                        DateTime dateFim = new DateTime(unavailability.getDateFinished());

                        double secondsDif = (dateFim.getMillis() - dateInicio.getMillis()) / 1000;
                        double minutesDif = secondsDif / 60;
                        responseClassInfo.unavailabilityMinutes += minutesDif;
                    }
                }
            } catch (Exception ex) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(ex));
                return null;
            }

            // Retornando os dados
            return responseClassInfo;
        }

        @Override
        public void onPostExecute(Object result) {
            if (result == null) return;
            ResponseClassInfo response = (ResponseClassInfo) result;

            // Executando requisicao para obtenção da pagina do relatorio
            requestReportPage(response.smsSended, response.totalSecondsInCall, response.unavailabilityMinutes, response.totalBytesTransferidos);
        }
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(LOG_TAG, "Location changed");
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            // Parando o locationListener
            Log.d(LOG_TAG, "Done... Getting report");
            locationManager.removeUpdates(myLocationListener);
            coordenates = new double[] {location.getLatitude(), location.getLongitude()};
            loadReportData();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
