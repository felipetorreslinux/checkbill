package com.checkmybill.util;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONObject;

/**
 * Created by guinetik on 8/12/16.
 */
public class FBUtil {
    private static JSONObject me;

    public static void getFBMe(final AccessToken accessToken, final Runnable cb) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                Log.d("FBUtil", graphResponse.toString());
                Log.d("FBUtil", graphResponse.getRawResponse());
                Log.d("FBUtil:USER", user.toString());
                FBUtil.me = user;
                cb.run();
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,first_name,last_name,gender");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public static JSONObject getMe() {
        return me;
    }
}
