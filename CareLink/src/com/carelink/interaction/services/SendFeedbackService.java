package com.carelink.interaction.services;

import com.carelink.interaction.Api;
import com.carelink.interaction.Message;
import org.json.JSONObject;

/**
 * Created by fripSide on 2015/3/27.
 * Version 1.0
 */
public class SendFeedbackService extends ServiceBase {
    public ServiceBase setParams(String content) {
        params.put("content", content);
        return this;
    }

    @Override
    public String getUri() {
        return Api.getSendFeedBackApi();
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
