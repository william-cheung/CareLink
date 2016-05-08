package com.carelink.interaction;

/**
 * Created by fripSide on 2015/3/24.
 * Version 1.0
 */
public class Message {
    public static final int STATUS_SUCCESS = 200;
    public static final int STATUS_INNER_SERVER_ERROR = 500;
    public static final int STATUS_JSON_ERROR = 600;
    public static final int STATUS_TOKEN_ERO = 700;
    public static final int STATUS_CAPTCHA_VERIFY_ERROR = 710;
    public static final int STATUS_CAPTCHA_SEND_FAILED = 720;
    public static final int STATUS_PHONE_NUMBER_IS_USED = 800;
    public static final int STATUS_NETWORK_ERROR = 1000;
    public static final int STATUS_LOGIN_WRONG_ACCOUNT = 1100;
    public static final int STATUS_WRONG_PASSWORD = 1110;
    private int statusCode;
    private String error;

    public Message(int statusCode) {
        this(statusCode, "");
    }

    public Message(int statusCode, String errorMsg) {
    	this.statusCode = statusCode;
        error = errorMsg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        //String msg = "";
        switch (statusCode) {
            case STATUS_SUCCESS:
                break;
            case STATUS_CAPTCHA_VERIFY_ERROR:
                break;
            case STATUS_TOKEN_ERO:
                break;
            case STATUS_LOGIN_WRONG_ACCOUNT:
                break;
            case STATUS_JSON_ERROR:
                break;
        }
        return "" + error;
    }
}
