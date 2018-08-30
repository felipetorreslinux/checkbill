package com.checkmybill.presentation.CreateUserPlanSteps;

import android.view.View;

/**
 * Created by ESPENOTE-06 on 03/11/2016.
 */

public abstract class UserPlanStepFragmentbase {
    public String validateErrorMessage = "";
    abstract public View getLayout();
    abstract public boolean validateStepFields();
}
