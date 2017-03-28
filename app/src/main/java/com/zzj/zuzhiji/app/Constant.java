package com.zzj.zuzhiji.app;

/**
 * Created by shawn on 17/3/28.
 */

public interface Constant {

    public interface SHARED_KEY {
        String SHARED_FILE_NAME = "ZZJ";
        String UUID = "UUID";
    }

    int DEFAULT_TIMEOUT = 5;

    int COUNT_DOWN_TIME = 60;

    String BASE_URL_NOR = "http://101.201.155.115:6068";
    String BASE_URL_SMS = "http://101.201.155.115:8086";
}
