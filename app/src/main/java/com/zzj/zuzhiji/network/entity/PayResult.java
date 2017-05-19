package com.zzj.zuzhiji.network.entity;

/**
 * Created by shawn on 2017-05-18.
 * <p>
 * {"package":"Sign\u003dWXPay",
 * "appid":"wx74a229d11fd9c1b0",
 * "sign":"789E433B322E592E5F7B60AFD5CC1D8D",
 * "prepayid":"wx201705190315413547093b5c0315298479",
 * "partnerid":"1465807002",
 * "noncestr":"16026d60ff9b54410b3435b403afd226",
 * "timestamp":"1495134941"}
 */

public class PayResult {
    public String appid;
    public String sign;
    public String prepayid;
    public String partnerid;
    public String noncestr;
    public String timestamp;
}
