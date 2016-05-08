package com.carelink.interaction.services;

import com.carelink.interaction.Api;
import com.carelink.interaction.Message;
import com.carelink.model.Record;
import com.google.gson.Gson;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fripSide on 2015/3/27.
 * Version 1.0
 */
public class DeleteRecordsService extends ServiceBase {

    public ServiceBase setParams(Record[] records) {
        Map<String, Integer> rids = new HashMap<String, Integer>();
        for (Record rd : records) {
            rids.put(rd.getGuid(), rd.getType());
        }
        String data = new Gson().toJson(rids);
        params.put("rids", data);
        return this;
    }

    @Override
    public String getUri() {
        return Api.getDeleteRecordsApi();
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
