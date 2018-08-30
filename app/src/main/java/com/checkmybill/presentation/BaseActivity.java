package com.checkmybill.presentation;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.checkmybill.R;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.Colabore;
import com.checkmybill.service.ServiceAutoStarter;
import com.checkmybill.service.ServiceEnviarColabore;
import com.checkmybill.util.IntentMap;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

/**
 * Created by guinetik on 8/20/16.
 */
public class BaseActivity extends AppCompatActivity {
    public String LOG_TAG;
    public static final int REQUEST_CODE_CAMERA_PICK = 1;

    protected Bundle savedInstance;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.savedInstance = savedInstance;
    }

    public BaseActivity() {
        super();
        LOG_TAG = getClass().getName();
    }

    protected void initToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.logo_small);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent it = new Intent(IntentMap.SETTINGS);
            startActivity(it);
            return true;
        }
        if (id == R.id.action_report) {
            /*
            Intent send = new Intent(Intent.ACTION_SENDTO);
            String uriText = "mailto:" + Uri.encode("checkbillapp@gmail.com") + "," + Uri.encode("checkmybill@gmail.com") +
                    "?subject=" + Uri.encode("Colaborador CheckBill") +
                    "&body=" + Uri.encode("Eu gostaria de ajudar a comunidade CheckBill informando:\n\n");
            Uri uri = Uri.parse(uriText);

            send.setData(uri);
            startActivity(Intent.createChooser(send, "Enviar email..."));*/

            Dialog dialogColabore = createDialogColabore();
            dialogColabore.show();

            return true;
        }
        if (id == R.id.action_rate) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/apps/testing/com.checkmybill.app"));
            startActivity(intent);

            return true;
        }
        if (id == R.id.action_quer_reclamar) {
            Dialog dialogQuerReclamar = createReclamarDialog();
            dialogQuerReclamar.show();

            return true;
        }
        if (id == R.id.action_reclame) {
            Dialog dialogFuncionalidadeBeta = createDialogFuncionalidadeBeta();
            dialogFuncionalidadeBeta.show();
            return true;
        }
        if (id == R.id.action_auditoria) {
            Intent it = new Intent(IntentMap.AUDITORIA);
            startActivity(it);
            return true;
        }
        if (id == R.id.action_teste) {
            Intent it = new Intent(IntentMap.TEST);
            startActivity(it);
            return true;
        }
        if (id == R.id.action_teste_speed) {
            Intent it = new Intent(IntentMap.SPEED_TEST);
            startActivity(it);
            return true;
        }
        if ( id == R.id.action_logout ) {
            // Confirmando acao de logout
            this.confirmLogoutAction();
            return true;
        }
        if ( id == R.id.action_login ) {
            Intent it = new Intent(IntentMap.INTRO);
            it.putExtra("HIDDEN_JUMP_BUTTON", true);
            startActivity(it);
            return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Checando qual menu deve ser obtido
        final String accessKey = new SharedPrefsUtil(this).getAccessKey();
        final int menuID = (accessKey == null || accessKey.length() <= 0) ? R.menu.menu_home_not_logged : R.menu.menu_home_logged;

        getMenuInflater().inflate(menuID, menu);
        return true;
    }

    public Dialog createReclamarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha um canal")
                .setItems(R.array.array_links_quer_reclamar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String url = null;
                        switch (which) {
                            case 0:
                                url = "http://www.anatel.gov.br/consumidor/";
                                break;
                            case 1:
                                url = "http://www.vivo.com.br/portalweb/appmanager/env/web?_nfls=false&_nfpb=true&_pageLabel=vcAtendimentoBook&WT.ac=portal.paravoce.atendimento";
                                break;
                            case 2:
                                url = "http://www.tim.com.br/pe/para-voce/atendimento";
                                break;
                            case 3:
                                url = "http://www.oi.com.br/minha-oi/celular/";
                                break;
                            case 4:
                                url = "http://www.claro.com.br/atendimento/";
                                break;
                            case 5:
                                url = "http://www.nextel.com.br/atendimento";
                                break;
                        }

                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });
        return builder.create();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA_PICK:
                try {
                    boolean k = false;
                    if (data != null && data.getExtras() != null) {
                        Bitmap imgBitmap = (Bitmap) data.getExtras().get("data");
                        if(imgBitmap != null) {
                            k = true;
                            Intent intent = new Intent(IntentMap.RECLAME);
                            intent.putExtra("img", imgBitmap);
                            startActivity(intent);
                        }
                    }
                    if(!k) {
                        Toast.makeText(this, "Não foi possivel completar sua solicitação", Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error onActivityResult. maybe the activity has been destroyed", e);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void confirmLogoutAction() {
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sair do Checkbill");
        builder.setMessage("Tem certeza que deseja realizar o logout?");
        builder.setCancelable(false);
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Saindo do facebook
                if (FacebookSdk.isInitialized() == false ) FacebookSdk.sdkInitialize(getBaseContext());
                LoginManager.getInstance().logOut();

                // Parando servicos
                /*new ServiceAutoStarter(getBaseContext()).stopAllService();

                // Limpando banco da dados...
                OrmLiteHelper ormLiteHelper = OrmLiteHelper.getInstance(context);
                ormLiteHelper.clearAllTables();*/
                //ormLiteHelper.close();

                // Limpando todas as configurações...
                SharedPrefsUtil prefsUtil = new SharedPrefsUtil(context);
                prefsUtil.clearAllData();

                // Saindo do aplicativo...
                dialog.dismiss();

                // Abrindo a intent 'Intro' limpando a ActivityStack (historico do back-button)
                Intent it = new Intent(IntentMap.INTRO);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(it);
                finish();
            }
        });
        builder.create().show();
    }

    public Dialog createDialogColabore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
        LayoutInflater inflater = BaseActivity.this.getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_colabore, null);

        final Spinner spSubject = (Spinner) view.findViewById(R.id.spSubject);
        final EditText editText = (EditText) view.findViewById(R.id.edtMessage);

        builder.setView(view)
                .setTitle("Colabore/Ajuda")
                .setIcon(R.mipmap.ic_info_black_24dp)
                .setNegativeButton("FECHAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("ENVIAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Colabore colabore = new Colabore();
                        colabore.setMessage(editText.getEditableText().toString());
                        colabore.setSubject(spSubject.getSelectedItem().toString());

                        Intent intent = new Intent(BaseActivity.this, ServiceEnviarColabore.class);
                        intent.putExtra(ServiceEnviarColabore.EXTRA_COLABORE, colabore);
                        startService(intent);

                        dialogInterface.dismiss();
                    }
                });
        return builder.create();
    }

    public Dialog createDialogFuncionalidadeBeta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Essa funcionalidade é apenas uma demonstração.")
                .setTitle("Atenção")
                .setPositiveButton("Entendi!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}
