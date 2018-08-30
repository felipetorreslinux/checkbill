package com.checkmybill.tutorial;

/**
 * Created by ESPENOTE-06, ${CORP} on 24/11/2016.
 */

public class TutorialException extends Exception {
    public TutorialException(String detailMessage) {
        super(detailMessage);
    }

    public TutorialException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TutorialException(Throwable throwable) {
        super(throwable);
    }
}
