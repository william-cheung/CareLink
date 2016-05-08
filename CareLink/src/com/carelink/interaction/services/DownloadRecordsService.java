package com.carelink.interaction.services;

import android.util.Log;
import com.carelink.interaction.Api;
import com.carelink.interaction.Message;
import com.carelink.model.Record;
import com.carelink.util.JsonHelper;
import com.google.gson.*;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by fripSide on 2015/3/27.
 * Version 1.0
 */
public class DownloadRecordsService extends ServiceBase {

    public ServiceBase setParams(int type, Date date) {
        params.put("type", "" + type);
        params.put("date", "" + date.getTime());
        return this;
    }

    public ServiceBase setParamsByDate(Date date) {
        params.put("date", "" + date.getTime());
        return this;
    }

    public ServiceBase setParamsByTypes(int type) {
        params.put("type", "" + type);
        return this;
    }

    public ServiceBase setParamsByDays(int days) {
        params.put("days", "" + days);
        return this;
    }

    @Override
    public String getUri() {
        return Api.getDownloadRecordApi();
    }

    @Override
    public void onFinish(JSONObject ret, RequestCallback callback) {
        try {
            int code = ret.getInt("code");
            String msg = ret.getString("msg");
            if (code == Message.STATUS_SUCCESS) {

                String data = ret.getString("data");
                JsonArray jArray = new JsonParser().parse(data).getAsJsonArray();
                int n = jArray.size();
                Record[] records = new Record[n];
                for (int i = 0; i < n; ++i) {
                    JsonElement obj = jArray.get(i);
                    records[i] = JsonHelper.jsonStringToRecord(obj.toString());
                }
                Log.d(TAG, data);
                callback.onSuccess(new Response(records));
                return;
            }
            callback.onFailed(new Message(code, msg));
        } catch (Exception e) {
            callback.onFailed(new Message(Message.STATUS_JSON_ERROR, e.getMessage()));
        }
    }
}
