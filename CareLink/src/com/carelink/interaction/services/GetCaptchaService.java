package com.carelink.interaction.services;

import com.carelink.interaction.Api;
import com.carelink.interaction.Message;
import org.json.JSONObject;

/**
 * Created by fripSide on 2015/3/26.
 * Version 1.0
 */
public class GetCaptchaService extends ServiceBase {
    @Override
    public String getUri() {
        return Api.getGetCaptchaApi();
    }

    public ServiceBase setParams(String phoneNumber) {
        params.put("phone-number", phoneNumber);
        return this;
    }

    @Override
    public void onFinish(JSONObject ret, RequestCallback callback) {
        try {
            int code = ret.getInt("code");
            if (code == Message.STATUS_SUCCESS) {
                String msg = ret.getString("msg");
                callback.onSuccess(new Response(msg));
                return;
            }
            callback.onFailed(new Message(code));
        } catch (Exception e) {
            callback.onFailed(new Message(Message.STATUS_JSON_ERROR, e.getMessage()));
        }
    }
}
