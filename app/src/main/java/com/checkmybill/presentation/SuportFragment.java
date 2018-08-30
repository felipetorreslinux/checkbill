package com.checkmybill.presentation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.checkmybill.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Victor Guerra on 09/05/2016.
 */
@EFragment(R.layout.fragment_suport)
public class SuportFragment extends BaseFragment {

    @ViewById(R.id.tvwAssunto)
    protected EditText tvwAssunto;

    @ViewById(R.id.tvwDescricao)
    protected EditText tvwDescricaoo;

    @ViewById(R.id.txt_hide)
    protected EditText txt_hide;

    @ViewById(R.id.btnEnviar)
    protected Button btnEnviar;

    @Click
    public void btnEnviar(){
        String assunto = tvwAssunto.getEditableText().toString();
        String tvwDescricao = tvwDescricaoo.getEditableText().toString();

        if (assunto.length() > 0) {
            tvwAssunto.setError(null);
            if (tvwDescricao.length() > 0) {
                tvwDescricaoo.setError(null);

                Toast.makeText(getActivity(), "Mensagem enviada!", Toast.LENGTH_SHORT).show();

                tvwAssunto.setText("");
                tvwDescricaoo.setText("");

                txt_hide.requestFocus();
                closeKeyBoard();

                Intent send = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("checkbillapp@gmail.com") + "," + Uri.encode("victor.guerra007@gmail.com") +
                        "?subject=" + Uri.encode(tvwAssunto.getEditableText().toString()) +
                        "&body=" + Uri.encode("Estou com problemas:\n\n" +
                        Uri.encode(tvwDescricaoo.getEditableText().toString()));
                Uri uri = Uri.parse(uriText);

                send.setData(uri);
                startActivity(Intent.createChooser(send, "Enviar email..."));

            } else {
                tvwDescricaoo.setError("Obrigatório");
                tvwDescricaoo.requestFocus();
            }
        } else {
            tvwAssunto.setError("Obrigatório");
            tvwAssunto.requestFocus();
        }
    }

    private void closeKeyBoard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
