package com.carelink.interaction.services;

import android.util.Log;
import com.carelink.interaction.Api;
import com.carelink.interaction.Message;
import org.json.JSONObject;

/**
 * Created by fripSide on 2015/3/27.
 * Version 1.0
 */
public class ResetPhoneNumberService extends ServiceBase {
    @Override
    public String getUri() {
        return Api.getResetPhoneNumApi();
    }

    public ServiceBase setParams(String newPhoneNumber, String oldPhoneNumber, String captcha) {
        params.put("old-phone", oldPhoneNumber);
        params.put("phone-number", newPhoneNumber);
        params.put("captcha", captcha);
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
