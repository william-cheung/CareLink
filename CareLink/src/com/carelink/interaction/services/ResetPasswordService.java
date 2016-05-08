package com.carelink.interaction.services;

import android.util.Log;
import com.carelink.interaction.Api;
import com.carelink.interaction.Message;
import org.json.JSONObject;

/**
 * Created by fripSide on 2015/3/27.
 * Version 1.0
 */
public class ResetPasswordService extends ServiceBase {
    @Override
    public String getUri() {
        return Api.getResetPasswordApi();
    }

    public ServiceBase setParams(String phoneNumber, String captcha, String newPassword) {
        params.put("new-password", newPassword);
        params.put("phone-number", phoneNumber);
        params.put("captcha", captcha);
        return this;
    }

    public ServiceBase setParams(String oldPassword, String newPassword) {
        params.put("new-password", newPassword);
        params.put("old-password", oldPassword);
        return this;
    }

    @Override
    public void onFinish(JSONObject ret, RequestCallback callback) {
        try {
            int code = ret.getInt("code");
            String msg = ret.getString("msg");
            if (code == Message.STATUS_SUCCESS) {
                callback.onSuccess(new Response(msg));
                return;
            }
            callback.onFailed(new Message(code, msg));
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            callback.onFailed(new Message(Message.STATUS_JSON_ERROR));
        }
    }
}
