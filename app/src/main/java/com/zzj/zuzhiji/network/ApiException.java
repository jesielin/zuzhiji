package com.zzj.zuzhiji.network;

/**
 * Created by shawn on 17/3/28.
 */

public class ApiException extends RuntimeException {


    public ApiException(String detailMessage) {
        super(detailMessage);
    }

}
