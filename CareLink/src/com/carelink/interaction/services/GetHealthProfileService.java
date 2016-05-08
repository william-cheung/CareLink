package com.carelink.interaction.services;

import android.util.Log;
import com.carelink.interaction.Api;
import com.carelink.interaction.Message;
import com.carelink.model.HealthProfile;
import com.google.gson.Gson;
import org.json.JSONObject;

/**
 * Created by fripSide on 2015/3/27.
 * Version 1.0
 */
public class GetHealthProfileService extends ServiceBase {
    @Override
    public String getUri() {
        return Api.getGetHealthProfileApi();
    }

    @Override
    public void onFinish(JSONObject ret, RequestCallback callback) {
        try {
            int code = ret.getInt("code");
            String msg = ret.getString("msg");
            if (code == Message.STATUS_SUCCESS) {
                String data = ret.getString("data");
                HealthProfile healthProfile = new Gson().fromJson(data, HealthProfile.class);
                callback.onSuccess(new Response(healthProfile));
                return;
            }
            callback.onFailed(new Message(code, msg));
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            callback.onFailed(new Message(Message.STATUS_JSON_ERROR, e.getMessage()));
        }

    }
}
