package com.carelink.interaction.services;

import com.carelink.interaction.Api;
import com.carelink.interaction.Message;
import org.json.JSONObject;

/**
 * Created by fripSide on 2015/3/26.
 * Version 1.0
 */
public class VerifyCaptchaService extends ServiceBase {
    @Override
    public String getUri() {
        return Api.getVerifyCaptchaApi();
    }

    public ServiceBase setCaptcha(String captcha) {
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
            callback.onFailed(new Message(Message.STATUS_JSON_ERROR, e.getMessage()));
        }
    }
}
