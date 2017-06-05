package com.brokepal.boite.web.constant;

/**
 * Created by Administrator on 2017/5/23.
 */
public class BoiteConst {
    private BoiteConst(){}

    public static final int TRY_COUNT = 3; //尝试次数，尝试那么多次失败后锁定账号
    public static final int LOCK_TIME = 10; //锁定时间，单位：分

    public static final String KEY_SESSION_ID = "sessionId";
    public static final String KEY_TOKEN = "token";
    public static final String CONNECT_URI = "/static/connect";
    public static final String KEY_PERMISSION = "permissions";

}
