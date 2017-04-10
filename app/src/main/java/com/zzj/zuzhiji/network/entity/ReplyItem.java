package com.zzj.zuzhiji.network.entity;

/**
 * {
 * "id": "58eaf03f0cf29e0400aaaf89",
 * "userUUID": [
 * "init",
 * "fBI7C9Hb49",
 * "utxHouIf23"
 * ],
 * "momentsID": "58eaf0270cf29e0400aaaf87",
 * "commenterUUID": "fBI7C9Hb49",
 * "commenterNickname": "动画电影",
 * "targetCommentUUID": "",
 * "targetCommentNickname": "",
 * "message": "大好河山"
 * }
 * Created by shawn on 17/4/10.
 */

public class ReplyItem {

    public String id;
    public String momentsID;
    public String commenterUUID;
    public String commenterNickname;
    public String targetCommentUUID;
    public String targetCommentNickname;
    public String message;
}
