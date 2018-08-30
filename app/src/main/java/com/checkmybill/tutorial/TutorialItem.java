package com.checkmybill.tutorial;

import android.app.Activity;
import android.view.View;

/**
 * Created by ESPENOTE-06, ${CORP} on 24/11/2016.
 */

public class TutorialItem {
    private View targetView;
    private String title;
    private String content;

    public TutorialItem(Activity activity, int targetView, String title, String content) {
        View target = activity.findViewById(targetView);
        this.targetView = target;
        this.title = title;
        this.content = content;
    }

    public TutorialItem(View targetView, String title, String content) {
        this.targetView = targetView;
        this.title = title;
        this.content = content;
    }

    public View getTargetView() {
        return targetView;
    }

    public void setTargetView(View targetView) {
        this.targetView = targetView;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
