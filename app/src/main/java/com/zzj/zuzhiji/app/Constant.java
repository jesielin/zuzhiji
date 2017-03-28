package com.zzj.zuzhiji.app;

/**
 * Created by shawn on 17/3/28.
 */

public interface Constant {

    public interface SHARED_KEY {
        String SHARED_FILE_NAME = "ZZJ";
        String UUID = "UUID";
        String AVATOR = "AVATOR";
        String LOGIN_NAME = "LOGIN_NAME";
        String NICK_NAME = "NICK_NAME";
        String USER_TYPE = "USER_TYPE";

    }

    int DEFAULT_TIMEOUT = 5;

    int COUNT_DOWN_TIME = 60;

    int PAGE_SIZE = 10;

    int MAX = 10000;

    String BASE_URL_NOR = "http://101.201.155.115:6068";
    String BASE_URL_SMS = "http://101.201.155.115:8086";
}
