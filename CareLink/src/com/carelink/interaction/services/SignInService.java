package com.carelink.interaction.services;

import android.util.Log;
import com.carelink.interaction.Api;
import com.carelink.interaction.Message;
import org.json.JSONObject;

/**
 * Created by fripSide on 2015/3/24.
 * Version 1.0
 */
public class SignInService extends ServiceBase {
    @Override
    public String getUri() {
        return Api.getSignInApi();
    }

    public ServiceBase setParams(String phoneNumber, String password) {
        params.put("phone-number", phoneNumber);
        params.put("password", password);
        return this;
    }


    @Override
    public void onFinish(JSONObject ret, RequestCallback callback) {
        try {
        	Log.d(TAG, ret.toString());
            int code = ret.getInt("code");
            String msg = ret.getString("msg");
            if (code == Message.STATUS_SUCCESS) {
                Integer uid = ret.getInt("uid");
                callback.onSuccess(new Response(uid));
                return;
            } else {
            	callback.onFailed(new Message(code, msg));
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            callback.onFailed(new Message(Message.STATUS_JSON_ERROR));
        }
    }
}
