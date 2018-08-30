package com.checkmybill.presentation;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.checkmybill.R;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.PlanoFiltroOpts;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_plano_filtro)
public class PlanoFiltroActivity extends BaseActivity {

    public static final String FIELD_LOCAL = "local";
    public static final String FIELD_INTERURBANO = "interurbano";
    public static final String FIELD_INTERNACIONAL = "internacional";
    public static final String FIELD_CONTROLE = "controle";
    public static final String FIELD_PREPAGO = "prepago";
    public static final String FIELD_POSPAGO = "pospago";
    public static final String FIELD_SIM = "sim";
    public static final String FIELD_NAO = "nao";

    private boolean doubleBackToExitPressedOnce = false;

    @ViewById(R.id.planoFiltroLigacaoRadioGroup)
    protected RadioGroup ligacaoRadioGroup;
    @ViewById(R.id.planoFiltroTipoRadioGroup)
    protected RadioGroup tipoPlanoRadioGroup;
    @ViewById(R.id.planoFiltroViagemRadioGroup)
    protected RadioGroup viagemRadioGroup;
    @ViewById(R.id.planoFiltroRegiaoText)
    protected TextView regiaoTV;
    @ViewById(R.id.planoFiltroBtnSave)
    protected Button btnSave;

    private PlanoFiltroOpts planoFiltroOpts;

    @Override
    protected void onStart() {
        super.onStart();
        // Obtendo as opcoes informadas
        Intent argumentIntent = getIntent();
        this.planoFiltroOpts = (PlanoFiltroOpts) argumentIntent.getSerializableExtra("PLANOFILTROOPTSCLASS");
        if (this.planoFiltroOpts == null) {
            // Nao há valores... Definindo opcoes padrao
            this.planoFiltroOpts = new PlanoFiltroOpts();
            this.planoFiltroOpts.setTipoLigacao(FIELD_LOCAL);
            this.planoFiltroOpts.setRegiao("");
            this.planoFiltroOpts.setViagem(FIELD_NAO);
            this.planoFiltroOpts.setTipoPlano(FIELD_PREPAGO);
        }

        // Populando os valores dos campo...
        this.populateFields(this.planoFiltroOpts);
    }

    @Click
    protected void planoFiltroBtnSave(){
        final int selectedLigacaoRadioID = ligacaoRadioGroup.getCheckedRadioButtonId();
        final int selectedTipoPlanoRadioID = tipoPlanoRadioGroup.getCheckedRadioButtonId();
        final int selectedViagemID = viagemRadioGroup.getCheckedRadioButtonId();
        final String regiaoValue = regiaoTV.getText().toString();

        // Checando se todos os campos foram de fato, seleconados/escolhidos/preenchidos...
        if (selectedLigacaoRadioID < 0 || selectedTipoPlanoRadioID < 0 || selectedViagemID < 0) {
            Toast.makeText(this, "Você deve selecionar todos os campos", Toast.LENGTH_LONG).show();
            return;
        } else if (regiaoValue.length() <= 0) {
            Toast.makeText(this, "É necessário que você digite qual é a sua 'Região'", Toast.LENGTH_LONG).show();
            return;
        }

        // Salvando dados obtitdos...
        Boolean useUpdate = planoFiltroOpts.getId() != null;
        planoFiltroOpts.setRegiao(regiaoTV.getText().toString());
        planoFiltroOpts.setTipoPlano(_obterValorTipoPlanoByID(selectedTipoPlanoRadioID));
        planoFiltroOpts.setViagem(_obterValorViagemByID(selectedViagemID));
        planoFiltroOpts.setTipoLigacao(_obterValorLigacaoByID(selectedLigacaoRadioID));

        // Atualizando dados...
        Log.i("PlanoFiltroActivity", "Atualizando dados:" + planoFiltroOpts.getId());
        OrmLiteHelper orm = new OrmLiteHelper(this);
        RuntimeExceptionDao<PlanoFiltroOpts, Integer> s = orm.getPlanoFiltroOptsRuntimeDao();
        if (useUpdate) s.update(planoFiltroOpts);
        else {
            if (s.isTableExists()) s.create(planoFiltroOpts);
            else s.createIfNotExists(planoFiltroOpts);
        }
        Log.i("PlanoFiltroActivity", "All Done: Return RESULT_OK");
        Intent resultIntent = new Intent();
        resultIntent.putExtra("PLANOFILTROOPTSCLASS", planoFiltroOpts);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    protected void populateFields(PlanoFiltroOpts filtroOpts) {
        // Definindo opcao: Ligacao
        if (filtroOpts.getTipoLigacao().equalsIgnoreCase(FIELD_LOCAL))
            ((RadioButton)ligacaoRadioGroup.getChildAt(0)).setChecked(true);
        else if (filtroOpts.getTipoLigacao().equalsIgnoreCase(FIELD_INTERURBANO))
            ((RadioButton)ligacaoRadioGroup.getChildAt(1)).setChecked(true);
        else if (filtroOpts.getTipoLigacao().equalsIgnoreCase(FIELD_INTERNACIONAL))
            ((RadioButton)ligacaoRadioGroup.getChildAt(2)).setChecked(true);
        // Definindo opcao: TipoPlano
        Log.d("TipoPlano", filtroOpts.getTipoPlano());
        if (filtroOpts.getTipoPlano().equalsIgnoreCase(FIELD_CONTROLE)) {
            ((RadioButton) this.findViewById(R.id.planoFiltroRadio_TipoControle)).setChecked(true);
        } else if (filtroOpts.getTipoPlano().equalsIgnoreCase(FIELD_PREPAGO))
            ((RadioButton) this.findViewById(R.id.planoFiltroRadio_TipoPrePago)).setChecked(true);
        else if (filtroOpts.getTipoPlano().equalsIgnoreCase(FIELD_POSPAGO))
            ((RadioButton) this.findViewById(R.id.planoFiltroRadio_TipoPosPago)).setChecked(true);
        // Definindo opcao: Viagem
        if (filtroOpts.getViagem().equalsIgnoreCase(FIELD_SIM))
            ((RadioButton)viagemRadioGroup.getChildAt(0)).setChecked(true);
        else if (filtroOpts.getViagem().equalsIgnoreCase(FIELD_NAO))
            ((RadioButton)viagemRadioGroup.getChildAt(1)).setChecked(true);
        // Definindo opcao: Regiao
        this.regiaoTV.setText(filtroOpts.getRegiao());
    }

    // Definindo evento do back Button
    @Override
    public void onBackPressed() {
        if (this.doubleBackToExitPressedOnce) {
            Toast.makeText(this, "Cancelado: O filtro de planos não foi salvo.", Toast.LENGTH_LONG).show();
            this.setResult(RESULT_CANCELED);
            super.onBackPressed();
            return;
        }

        // Definindo Timeout para o proximo 'backPressed'
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Pressione 'Voltar' duas vezes para sair", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2500);
    }
    // -> Metodos privados da classe...
    private String _obterValorLigacaoByID(final int id) {
        if (R.id.planoFiltroRadio_LigInternacional == id)
            return FIELD_INTERNACIONAL;
        else if (R.id.planoFiltroRadio_LigInterubano == id)
            return FIELD_INTERURBANO;
        else if (R.id.planoFiltroRadio_LigLocal == id)
            return FIELD_LOCAL;

        return null;
    }
    private String _obterValorTipoPlanoByID(final int id) {
        if (id == R.id.planoFiltroRadio_TipoControle)
            return FIELD_CONTROLE;
        else if (id == R.id.planoFiltroRadio_TipoPosPago)
            return FIELD_POSPAGO;
        else if (id == R.id.planoFiltroRadio_TipoPrePago)
            return FIELD_CONTROLE;

        return null;
    }
    private String _obterValorViagemByID(final int id) {
        if (id == R.id.planoFiltroRadio_ViagemSim) return FIELD_SIM;
        else if (id == R.id.planoFiltroRadio_ViagemNao) return FIELD_NAO;
        return null;
    }
}
