package com.carelink.interaction.services;

import android.util.Log;
import com.carelink.interaction.Api;
import com.carelink.interaction.Message;
import com.carelink.model.Record;
import com.google.gson.Gson;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by fripSide on 2015/3/27.
 * Version 1.0
 */
public class UploadRecordsService extends ServiceBase {

    public ServiceBase setParams(Record[] records, Date date) {
        String data = new Gson().toJson(records);
        params.put("data", data);
        params.put("date", "" + date.getTime());
        return this;
    }

    public ServiceBase setParams(Record[] records) {
        String data = new Gson().toJson(records);
        params.put("data", data);
        return this;
    }

    public ServiceBase setParams(Record[] records, int days) {
        String data = new Gson().toJson(records);
        params.put("data", data);
        params.put("days", "" + days);
        return this;
    }

    @Override
    public String getUri() {
        return Api.getUploadRecordApi();
    }

    @Override
    public void onFinish(JSONObject ret, RequestCallback callback) {
        try {
            int code = ret.getInt("code");
            String msg = ret.getString("msg");
            if (code == Message.STATUS_SUCCESS) {
                int num = ret.getInt("num");
                callback.onSuccess(new Response(num));
                return;
            }
            callback.onFailed(new Message(code, msg));
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            callback.onFailed(new Message(Message.STATUS_JSON_ERROR, e.getMessage()));
        }
    }
}
