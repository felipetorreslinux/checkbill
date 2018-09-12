package com.checkmybill.presentation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.adapters.AdapterRecargas;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.RecargasPlano;
import com.checkmybill.request.PlanoRequester;
import com.checkmybill.util.DatePickers;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.Util;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@EActivity(R.layout.activity_ger_creditos_plano)
public class GerCreditosPlanoActivity extends BaseActivity {
    @ViewById(R.id.recargas_list) protected RecyclerView recargas_list;
    @ViewById(R.id.recarga_valoTotalUsado) protected TextView recarga_valoTotalUsado;
    @ViewById(R.id.noRecargaListData) protected TextView noRecargaListData;
    @ViewById(R.id.filterDateRangeText) protected TextView filterDateRangeText;

    public static final int REQUEST_CODE = 1204;

    private List<RecargasPlano> recargasPlanoList;
    private AdapterRecargas adapterRecargas;

    private Date[] dateLimits;

    private RequestQueue requestQueue;
    private Context mContext;
    private ProgressDialog loadingWindow;

    Toolbar toolbar;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores/Eventos da Acitivty/Layout)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG_TAG = getClass().getName();

        requestQueue = Volley.newRequestQueue(this);
        mContext = this;
        this.loadingWindow = NotifyWindow.CreateLoadingWindow(mContext, "Créditos", "Carregando, aguarde...");
        this.loadingWindow.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                requestQueue.cancelAll(GerCreditosPlanoActivity.class.getName());
                dialogInterface.dismiss();
            }
        });

        // Inicializando Adapter
        this.recargasPlanoList = new ArrayList<>();
        this.adapterRecargas = new AdapterRecargas(this.recargasPlanoList, this, removerRecarga);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Créditos");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ger_creditos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;

            case R.id.add_creditos:
                gerCreditosFabButtonClick();
                break;
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        requestQueue.cancelAll( getClass().getName() );
    }

    @Override
    public void onStart() {
        super.onStart();

        // Definindo adapter
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        this.recargas_list.setLayoutManager(llm);
        this.recargas_list.setAdapter( this.adapterRecargas );

        // Definindo valores iniciais do periodo a ser pesquisado
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            int monthMaxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            this.dateLimits = new Date[]{
                    sdf.parse(String.format("%04d-%02d-%02d", c.get(Calendar.YEAR), 1 + c.get(Calendar.MONTH), 1)),
                    sdf.parse(String.format("%04d-%02d-%02d", c.get(Calendar.YEAR), 1 + c.get(Calendar.MONTH), monthMaxDays))
            };
        } catch ( Exception e ) {
            this.dateLimits = new Date[]{ new Date(), new Date() };
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }

        ObterListaRecargas();
    }



    // --------------------------------------------------------------------------------------------
    // Eventos dos elementos anexados/relacionados a View atual...
    @Click(R.id.filterSettingsLayoutContent)
    public void filterSettingsLayoutContentClick() {
        //new NotifyWindow(mContext).showWarningMessage("Créditos", "Atenção: Recurso não implementado", false);
        DatePickers.DateRangeDatePicker dateRangeDatePicker = new DatePickers.DateRangeDatePicker(this);
        dateRangeDatePicker.setTitle("Período");
        dateRangeDatePicker.setPositiveEvent(new DatePickers.DateRangeDatePickerInterface.OnPositiveEvent() {
            @Override
            public void onEvent(Date firstDate, Date secondDate, DialogInterface dialogInterface, int i) {
                dateLimits[0] = firstDate;
                dateLimits[1] = secondDate;

                // Executando atualizacao dos itens...
                dialogInterface.dismiss();
                ObterListaRecargas();
            }
        });
        dateRangeDatePicker.setNegativeEvent(new DatePickers.DateRangeDatePickerInterface.OnNegativeEvent() {
            @Override
            public void onEvent(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dateRangeDatePicker.showDialogWindow();
    }

    public void gerCreditosFabButtonClick() {
        final View pickerLayout = getLayoutInflater().inflate(R.layout.dialog_add_recarga_layout, null);
        final View manualDateContainer = pickerLayout.findViewById(R.id.manualDateContainer);
        final TextView valorRecarga = (TextView) pickerLayout.findViewById(R.id.valor_recarga);
        final CheckBox usarDataAtualRecarga = (CheckBox) pickerLayout.findViewById(R.id.usarDataAtual_recarga);
        final DatePicker datepickerDateRecarga = (DatePicker) pickerLayout.findViewById(R.id.datepickerDate_recarga);

        // Definindo evento docheckbox
        usarDataAtualRecarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = usarDataAtualRecarga.isChecked();
                if ( isChecked ) manualDateContainer.setVisibility(View.GONE);
                else manualDateContainer.setVisibility(View.VISIBLE);
            }
        });

        // Criando DialoBox com os elementos definidos
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setTitle("Adicionar Recarga");
        builder.setView(pickerLayout);
        builder.setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Checando se o valor foi informado
                if ( valorRecarga.getText().length() <= 0 ) {
                    valorRecarga.requestFocus();
                    Toast.makeText(mContext, "Digite o valor da recarga", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Obtendo os dados
                final Date recargaData;
                if ( usarDataAtualRecarga.isChecked() == false ) {
                    GregorianCalendar calendar = new GregorianCalendar(datepickerDateRecarga.getYear(), datepickerDateRecarga.getMonth(), datepickerDateRecarga.getDayOfMonth());
                    recargaData = calendar.getTime();
                } else {
                    recargaData = new Date();
                }

                // Ocultando interface e disparanco a exeucao do cadastro do plano...
                AdicionarRecarga(recargaData, Float.parseFloat( valorRecarga.getText().toString().replace(",",".") ));
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        builder.create().show();
    }

    private CustomItemClickListener removerRecarga = new CustomItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            StringBuilder sb = new StringBuilder();
            final RecargasPlano recargaInfo = recargasPlanoList.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            // Montando a string a ser exibida
            sb.append("Tem certeza que deseja remover a recarga de ");
            sb.append(String.format("'%.2f R$'", recargaInfo.getValorRecarga()));
            sb.append(", realizada no dia: '" + sdf.format( recargaInfo.getDataRecarga()));
            sb.append("'?");

            // Montando dialog
            builder.setTitle("Remover Recarga");
            builder.setMessage(sb.toString());
            builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    RemoverRecarga(recargaInfo.getId());
                    dialogInterface.dismiss();
                }
            });

            builder.create().show();
        }
    };

    // --------------------------------------------------------------------------------------------
    // Variaveis para o tratamento das requisições do Volley
    private Response.Listener delRecargaPlanoSuccessLienter = new Response.Listener<JSONObject>(){
        @Override
        public void onResponse(JSONObject response) {
            loadingWindow.dismiss();
            try {
                Log.d(LOG_TAG, response.toString());
                if ( !response.getString("status").equalsIgnoreCase("success") ) {
                    new NotifyWindow(mContext).showErrorMessage("Créditos", "Erro: " + response.getString("message"), false);
                    return;
                }

                // Exibindo mensagem de atualizão feita com sucesso e atualizando a lista atual...
                ObterListaRecargas();
                Toast.makeText(mContext, "Recarga removida com sucesso!", Toast.LENGTH_LONG).show();
            } catch ( JSONException e ) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
                new NotifyWindow(mContext).showErrorMessage("FATAL ERRO", Util.getMessageErrorFromExcepetion(e), false);
            }
        }
    };

    private Response.Listener addRecargaPlanoSuccessLienter = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            loadingWindow.dismiss();
            try {
                Log.d(LOG_TAG, response.toString());
                if ( !response.getString("status").equalsIgnoreCase("success") ) {
                    new NotifyWindow(mContext).showErrorMessage("Créditos", "Erro: " + response.getString("message"), false);
                    return;
                }

                // Exibindo mensagem de atualizão feita com sucesso e atualizando a lista atual...
                ObterListaRecargas();
                Toast.makeText(mContext, "Recarga realizada com sucesso!", Toast.LENGTH_LONG).show();
            } catch ( JSONException e ) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
                new NotifyWindow(mContext).showErrorMessage("FATAL ERRO", Util.getMessageErrorFromExcepetion(e), false);
            }
        }
    };

    private Response.Listener listRecargasPlanoSuccessLienter = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            loadingWindow.dismiss();
            try {
                Log.d(LOG_TAG, response.toString());
                if ( !response.getString("status").equalsIgnoreCase("success") ) {
                    new NotifyWindow(mContext).showErrorMessage("Créditos", "Erro: " + response.getString("message"), false);
                    return;
                }

                // Checando resposta
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                recargasPlanoList.clear();
                JSONArray recarga_info = response.getJSONArray("recarga_info");
                if ( recarga_info.length() <= 0 ) {
                    recargas_list.setVisibility(View.GONE);
                    noRecargaListData.setVisibility(View.VISIBLE);

                    // Atualizando texto do valor total usado no mmomento
                    recarga_valoTotalUsado.setText(String.format("R$ 0.00"));
                    sdf = new SimpleDateFormat("dd/MM/yyyy");
                    StringBuilder sb = new StringBuilder(sdf.format(dateLimits[0]));
                    sb.append(" à ");
                    sb.append(sdf.format(dateLimits[1]));
                    filterDateRangeText.setText( sb.toString() );

                    // Parando a execucao, nap há pq continuar se não há mais dados
                    return;
                }

                float valorTotal = 0;
                for ( int i = 0; i < recarga_info.length(); i++ ) {
                    RecargasPlano recargasPlano = new RecargasPlano();
                    recargasPlano.setId( recarga_info.getJSONObject(i).getInt("id_recarga") );
                    recargasPlano.setValorRecarga( Float.parseFloat(recarga_info.getJSONObject(i).getString("valor_recarga")) );
                    recargasPlano.setDataRecarga( sdf.parse(recarga_info.getJSONObject(i).getString("dat_cad")) );
                    valorTotal += recargasPlano.getValorRecarga();

                    recargasPlanoList.add( recargasPlano );
                }

                // Garantindo a exibicao das Views...
                noRecargaListData.setVisibility(View.GONE);
                recargas_list.setVisibility(View.VISIBLE);

                // Disparando atualizando da Lista
                adapterRecargas.setLista(recargasPlanoList);
                adapterRecargas.notifyDataSetChanged();

                // Atualizando texto do valor total usado no mmomento
                recarga_valoTotalUsado.setText(String.format("R$ %.2f", valorTotal));
                sdf = new SimpleDateFormat("dd/MM/yyyy");
                StringBuilder sb = new StringBuilder(sdf.format(dateLimits[0]));
                sb.append(" à ");
                sb.append(sdf.format(dateLimits[1]));
                filterDateRangeText.setText( sb.toString() );
            } catch ( Exception e) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
                new NotifyWindow(mContext).showErrorMessage("FATAL ERRO", Util.getMessageErrorFromExcepetion(e), false);
            }
        }
    };
    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            loadingWindow.dismiss();
            Log.e(LOG_TAG, error.getMessage());
            new NotifyWindow(mContext).showErrorMessage("FATAL ERRO", error.getMessage(), false);
        }
    };

    // --------------------------------------------------------------------------------------------
    // Metodos privados de uso interno da classe
    private void ObterListaRecargas() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.loadingWindow.show();

        // Obtendo a lista de recargas existentes...
        JsonObjectRequest recargasPlanoRequester = PlanoRequester.prepareConsultaRecargasPlanosUsuarioRequest(listRecargasPlanoSuccessLienter, errorListener, sdf.format(dateLimits[0]), sdf.format(dateLimits[1]), mContext);
        recargasPlanoRequester.setTag( getClass().getName() );
        requestQueue.add( recargasPlanoRequester );
    }

    private void AdicionarRecarga(Date date, float valor) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.loadingWindow.show();

        // Adicionar uma nova recarga...
        JsonObjectRequest recargasPlanoRequester = PlanoRequester.prepareAddRecargaPlanoUsuarioRequest(addRecargaPlanoSuccessLienter, errorListener, sdf.format(date), valor, mContext);
        recargasPlanoRequester.setTag( getClass().getName() );
        requestQueue.add( recargasPlanoRequester );
    }

    private void RemoverRecarga(int id_recarga) {
        this.loadingWindow.show();

        // Adicionar uma nova recarga...
        JsonObjectRequest recargasPlanoRequester = PlanoRequester.prepareRemoverRecargaPlanoUsuarioRequest(delRecargaPlanoSuccessLienter, errorListener, id_recarga, mContext);
        recargasPlanoRequester.setTag( getClass().getName() );
        requestQueue.add( recargasPlanoRequester );
    }
}
