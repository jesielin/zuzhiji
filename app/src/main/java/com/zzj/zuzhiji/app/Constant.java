package com.zzj.zuzhiji.app;

/**
 * Created by shawn on 17/3/28.
 */

public interface Constant {

    int DEFAULT_TIMEOUT = 5;
    int COUNT_DOWN_TIME = 60;
    int PAGE_SIZE = 10;
    int MAX = 10000;
    int IMAGE_UPLOAD_MAX_SIZE = 500;//KB
    int IMAGE_UPLOAD_MAX_HEIGHT = 1920;
    int IMAGE_UPLOAD_MAX_WIDTH = 1080;
    int IMAGE_UPLOAD_QUALITY = 75;
    String BASE_URL_NOR = "http://101.201.155.115:6068";
    String BASE_URL_SMS = "http://101.201.155.115:8086";

    interface SHARED_KEY {
        String SHARED_FILE_NAME = "ZZJ";
        String UUID = "UUID";
        String AVATOR = "AVATOR";
        String LOGIN_NAME = "LOGIN_NAME";
        String NICK_NAME = "NICK_NAME";
        String USER_TYPE = "USER_TYPE";

    }

    interface UI_CODE {
        int REQUEST_CODE_SOCIAL_FRAGMENT = 0x01;
        int RESULT_CODE_PUBLISH_SUCCESS = 0x02;
    }
}
