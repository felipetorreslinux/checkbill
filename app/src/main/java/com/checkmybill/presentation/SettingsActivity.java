package com.checkmybill.presentation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.checkmybill.R;
import com.checkmybill.entity.Colabore;
import com.checkmybill.service.ServiceEnviarColabore;
import com.checkmybill.util.IntentMap;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_settings)
public class SettingsActivity extends BaseActivity {

    @ViewById(R.id.aboutOption)
    protected LinearLayout aboutOption;

    @ViewById(R.id.helpOption)
    protected LinearLayout helpOption;

    @ViewById(R.id.acconuntOption)
    protected LinearLayout acconuntOption;

    @Click
    protected void aboutOption() {
        Dialog dialogAbout = createAboutDialog();
        dialogAbout.show();
    }

    @Click
    protected void helpOption() {
        //Intent it = new Intent(IntentMap.HELP);
        //startActivity(it);
        Dialog dialogColabore  = createDialogColabore();
        dialogColabore.show();
    }

    @Click
    protected void acconuntOption() {
        Intent it = new Intent(IntentMap.ACCOUNT);
        startActivity(it);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public Dialog createAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        LayoutInflater inflater = SettingsActivity.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_about, null);
        builder.setView(view)
                .setTitle("Sobre")
                //.setMessage("Estamos ansiosos para saber sua opinião. Avalie!")
                .setIcon(R.mipmap.ic_info_black_24dp)
                .setNegativeButton("FECHAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    public Dialog createDialogColabore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        LayoutInflater inflater = SettingsActivity.this.getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_colabore, null);

        final Spinner spSubject = (Spinner) view.findViewById(R.id.spSubject);
        final EditText editText = (EditText) view.findViewById(R.id.edtMessage);

        builder.setView(view)
                .setTitle("Ajuda")
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

                        Intent intent = new Intent(SettingsActivity.this, ServiceEnviarColabore.class);
                        intent.putExtra(ServiceEnviarColabore.EXTRA_COLABORE, colabore);
                        startService(intent);

                        dialogInterface.dismiss();
                    }
                });
        return builder.create();
    }

}
