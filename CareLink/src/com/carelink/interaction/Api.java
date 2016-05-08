package com.carelink.interaction;

/**
 * Created by fripSide on 2015/3/24.
 * Version 1.0
 */
public class Api {
    private static final String HOST = "http://123.56.132.72:8800";
//    private static final String HOST = "http://219.223.179.148:5000";
    private static final String LOGIN = HOST + "/api/signin";
    private static final String REGISTER = HOST + "/api/signup";
    private static final String RESET_PSW = HOST + "/api/reset_password";
    private static final String RESET_PHONE_NUM = HOST + "/api/reset_phonenum";
    private static final String GET_CAPTCHA = HOST + "/api/get_captcha";
    private static final String VERIFY_CAPTCHA = HOST +"/api/verify_captcha";
    private static final String GET_USER_INFO = HOST + "/api/get_userinfo";
    private static final String GET_HEALTHPROFILE = HOST + "/api/get_health_profile";
    private static final String UPDATE_HEALTHPROFILE = HOST + "/api/update_health_profile";
    private static final String UPLOAD_RECORD = HOST + "/api/upload_records";
    private static final String DOWNLOAD_RECORD = HOST + "/api/download_records";
    private static final String SEND_FEEDBACK = HOST + "/api/send_feedback";
    private static final String DELETE_RECORDS = HOST + "/api/delete_remote_records";

    public static String getSignInApi() {
        return LOGIN;
    }

    public static String getSignUpApi() { 
        return REGISTER;
    }

    public static String getGetUserInfoApi() {
        return GET_USER_INFO;
    }

    public static String getGetCaptchaApi() {
        return GET_CAPTCHA;
    }

    public static String getVerifyCaptchaApi() {
        return VERIFY_CAPTCHA;
    }

    public static String getResetPasswordApi() {
        return RESET_PSW;
    }

    public static String getResetPhoneNumApi() {
        return RESET_PHONE_NUM;
    }

    public static String getDownloadRecordApi() {
        return DOWNLOAD_RECORD;
    }

    public static String getUploadRecordApi() {
        return UPLOAD_RECORD;
    }

    public static String getGetHealthProfileApi() {
        return GET_HEALTHPROFILE;
    }

    public static String getUpdateHealthProfileApi() {
        return UPDATE_HEALTHPROFILE;
    }

    public static String getSendFeedBackApi() {
        return SEND_FEEDBACK;
    }

    public static String getDeleteRecordsApi() {
        return DELETE_RECORDS;
    }
}
