package com.zzj.zuzhiji.network.entity;

/**
 * Created by shawn on 2017-05-09.
 * {"summary":"不都是",
 * "address":"记得记得",
 * "operateStatus":"success",
 * "city":"110100",
 * "title":"好滴版本",
 * "uuid":"lm5cmoby54",
 * "headSculpture":"http://101.201.155.115:3113/heads/14725836914-studiohead.jpg",
 * "license":"http://101.201.155.115:3113/license/14725836914-license.jpg",
 * "province":"110000",
 * "serial":"546464664",
 * "loginName":"14725836914",
 * "userType":"0",
 * "createDate":1494270474248,
 * "status":"2"}}
 */

public class RegisterStudioResult {
    //// TODO: 2017-05-09

    public String summary;
    public String address;
    public String operateStatus;
    public String city;
    public String title;
    public String uuid;
    public String headSculpture;
    public String license;
    public String province;
    public String serial;
    public String loginName;
    public String userType;
    public String createDate;
    public String status;
    public String bankcardno;

    @Override
    public String toString() {
        return "RegisterStudioResult{" +
                "summary='" + summary + '\'' +
                ", \naddress='" + address + '\'' +
                ", \noperateStatus='" + operateStatus + '\'' +
                ", \ncity='" + city + '\'' +
                ", \ntitle='" + title + '\'' +
                ", \nuuid='" + uuid + '\'' +
                ", \nheadSculpture='" + headSculpture + '\'' +
                ", \nlicense='" + license + '\'' +
                ", \nprovince='" + province + '\'' +
                ", \nserial='" + serial + '\'' +
                ", \nloginName='" + loginName + '\'' +
                ", \nuserType='" + userType + '\'' +
                ", \ncreateDate='" + createDate + '\'' +
                ", \nstatus='" + status + '\'' +
                '}';
    }
}
