package com.checkmybill.tutorial;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.checkmybill.R;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.List;

/**
 * Created by Petrus A. (R@G3) on 24/11/2016.
 * Implementa uma interface para controlar e exibir telas de tutoriais de modo simples e rápido...
 */

public class Tutorial {
    // Interface com o evento
    public interface TutorialListener {
        void onDone();
    }

    private String LOG_TAG;
    private Activity activity;
    private List<TutorialItem> itemList;
    private boolean isVisible = false;
    private TutorialListener listener;
    ShowcaseView showcaseView;

    public Tutorial(Activity activity) throws TutorialException {
        if ( activity == null ) throw new TutorialException("Need an Activity");
        this.activity = activity;
        this.LOG_TAG = "Tutorial";

    }

    public Tutorial(Activity activity, List<TutorialItem> itemList) throws TutorialException {
        if ( activity == null ) throw new TutorialException("Need an Activity");
        this.activity = activity;
        this.itemList = itemList;
        this.LOG_TAG = "Tutorial";
    }

    public void startTutorial() throws TutorialException {
        if ( itemList == null || itemList.size() <= 0 )
            throw new TutorialException("Item List is Empty");

        this.createShowCate(0);
    }

    public List<TutorialItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<TutorialItem> itemList) {
        this.itemList = itemList;
    }

    public void setListener(TutorialListener listener) {
        this.listener = listener;
    }

    private void createShowCate(final int step) {
        TutorialItem item = itemList.get(step);

        if ( this.showcaseView == null ) {
            ShowcaseView.Builder builder = new ShowcaseView.Builder(activity);
            builder.setTarget(new ViewTarget(item.getTargetView()));
            builder.setContentText(item.getContent());
            builder.setContentTitle(item.getTitle());
            builder.setStyle(R.style.CheckbillShowcaseTheme);
            builder.setOnClickListener(new View.OnClickListener() {
                private int currentStep = step;
                @Override
                public void onClick(View view) {
                    currentStep++;
                    if ( currentStep < itemList.size() ) {
                        Log.d(LOG_TAG, "Next Step ->" + currentStep);
                        createShowCate(currentStep);
                    } else {
                        // All Done
                        Log.d(LOG_TAG, "All Done");
                        isVisible = false;
                        if ( listener != null) listener.onDone();
                        showcaseView.hide();
                        showcaseView.destroyDrawingCache();
                    }
                }
            });
            this.showcaseView = builder.build();
        } else {
            this.showcaseView.setContentText(item.getContent());
            this.showcaseView.setTarget(new ViewTarget(item.getTargetView()));
            this.showcaseView.setContentTitle(item.getTitle());
        }

        this.isVisible = true;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void hideNow() {
        if ( this.showcaseView != null ) this.showcaseView.hide();
    }
}
