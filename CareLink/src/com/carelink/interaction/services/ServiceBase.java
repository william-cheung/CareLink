package com.carelink.interaction.services;

import com.android.volley.VolleyError;
import com.carelink.interaction.Message;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fripSide on 2015/3/24.
 * Version 1.0
 */
public abstract class ServiceBase {
    protected static final String TAG = ServiceBase.class.getName();
    protected Map<String, String> params = new HashMap<String, String>();

    public abstract String getUri();

    public Map<String, String> getParams() {
        return params;
    }

    public abstract void onFinish(JSONObject ret, RequestCallback callback);

    public void onError(VolleyError error, RequestCallback callback) {
        String msg = "failed to connect to the server";
        if (error.networkResponse != null) {
            msg = new String(error.networkResponse.data);
        }
        callback.onFailed(new Message(Message.STATUS_NETWORK_ERROR, msg));
    }

    public static interface RequestCallback {
        public void onSuccess(Response response);

        public void onFailed(Message msg);
    }

    public static class Response {
        Object data;

        public Response(Object obj) {
            data = obj;
        }

        public Object getData() {
            return data;
        }
    }
}
