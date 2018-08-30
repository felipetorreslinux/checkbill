package com.checkmybill.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.checkmybill.R;

/**
 * Created by espe on 13/07/2016 -> 22/11/2016.
 */
public class NotifyWindow {
    private Context mContext;
    private AlertDialog.Builder builder;
    private AlertDialog dlg;

    public NotifyWindow(Context mContext) {
        this.mContext = mContext;

        this.builder = new AlertDialog.Builder(mContext);
        this.builder.setCancelable(false);
    }

    public AlertDialog.Builder getBuilder() {
        return builder;
    }

    public void showWarningMessage(final String title, final String message, boolean cancelable) {
        this.showWarningMessage(title, message, cancelable, true);
    }

    public void showWarningMessage(final String title, final String message, boolean cancelable, boolean showIcon) {
        if ( showIcon ) this.showMessage(title, message, cancelable, R.drawable.ic_warning_amber);
        else this.showMessage(title, message, cancelable, -1);
        this.show();
    }

    public void showErrorMessage(final String title, final String message, boolean cancelable) {
        this.showMessage(title, message, cancelable, R.drawable.ic_error_red);
        this.show();
    }

    public void showErrorMessage(final String title, final String message, boolean cancelable, DialogInterface.OnClickListener onClickListener) {
        this.showMessage(title, message, cancelable, R.drawable.ic_error_red, onClickListener);
        this.show();
    }

    public void showMessage(final String title, final String message, boolean cancelable, int icon, DialogInterface.OnClickListener onClickListener) {
        this.builder.setCancelable(cancelable);
        this.builder.setMessage(message);
        this.builder.setTitle(title);
        this.builder.setPositiveButton("OK", onClickListener);
        if ( icon >= 0 ) this.builder.setIcon(icon);
        this.show();
    }

    public void showMessage(final String title, final String message, boolean cancelable, int icon) {
        this.builder.setCancelable(cancelable);
        this.builder.setMessage(message);
        this.builder.setTitle(title);
        this.builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        if ( icon >= 0 ) this.builder.setIcon(icon);
        this.show();
    }

    public void show() {
        if (dlg != null && dlg.isShowing()) {
            dlg.dismiss();
            dlg = null;
        }

        this.dlg = this.builder.create();

        try {
            this.dlg.show();
        } catch(Exception e) {
            Log.e(getClass().getName(), "Error onPostExcecute. maybe the activity has been destroyed", e);
        }
    }

    public static ProgressDialog CreateLoadingWindow(Context mContext, final String title, final String message) {
        ProgressDialog loadingDialog = new ProgressDialog(mContext);
        loadingDialog.setCancelable(false);
        loadingDialog.setTitle(title);
        loadingDialog.setMessage(message);
        return loadingDialog;
    }
}
