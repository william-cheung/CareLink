package com.carelink.interaction;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.carelink.interaction.services.ServiceBase;
import org.json.JSONException;
import org.json.JSONObject;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Map;

/**
 * Created by fripSide on 2015/3/24.
 * Version 1.0
 */
public class Services {
    public static final String TAG = "CareLinkService";
    private static final int SOCKET_TIMEOUT = 1000;
    private static RequestQueue mRequestQueue;
    private static Services services;
    private String token;
    private Context context;

    private Services() {
    }

    private Services(Context context) {
        this.context = context;
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
    }

    public static synchronized Services getInstance(Context context) {
        if (services == null) {
            services = new Services(context);
        }
        return services;
    }

    private synchronized String getToken() {
        //ToDo: 浠庢湰鍦皌oken璇诲彇token
        return token;
    }

    private synchronized void setToken(String tk) {
        //ToDo: 淇濆瓨token鍒版湰鍦�
        token = tk;
    }
    
    public boolean isOnLine() {
    	if (token == null) {
    		Log.d(TAG, "token : null");
    		return false;
    	}
    	return true;
    }
    
    public void signOut() {
    	token = null;
    }

    public void sendRequest(final ServiceBase request, final ServiceBase.RequestCallback callback) {
        Map<String, String> params = request.getParams();
        String tk = getToken();
        if (!params.containsKey("token") && tk != null) {
            params.put("token", tk);
        }
		if (!hasNetwork()) {
            callback.onFailed(new Message(Message.STATUS_NETWORK_ERROR, "No Network Available"));
            return;
        } 
		
        JsonObjectRequest strRequest = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, request.getUri(),
                new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject jsonObj) {
                if (jsonObj.has("token")) {
                    try {
                        setToken(jsonObj.getString("token"));
                    } catch (JSONException e) {
                        e.printStackTrace();
//                        callback.onFailed(StatusCode.STATUS_TOKEN_ERO, "token璇诲彇澶辫触!");
                    }
                }
                request.onFinish(jsonObj, callback);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                request.onError(error, callback);
            }
        }
        );
		strRequest.setRetryPolicy(new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        addRequest(strRequest, TAG);
    }
	
	private boolean hasNetwork() {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isAvailable());
    }

    private static void addRequest(com.android.volley.Request<?> request, String tag) {
        if (tag != null) {
            request.setTag(tag);
        }
        mRequestQueue.add(request);
    }

    public static void cancelAll() {
        if (mRequestQueue == null) {
            return;
        }
        mRequestQueue.cancelAll(TAG);
    }
}
