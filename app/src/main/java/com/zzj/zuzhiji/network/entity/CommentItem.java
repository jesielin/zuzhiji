package com.zzj.zuzhiji.network.entity;

/**
 * Created by shawn on 17/2/19.
 */

/**
 *
 * {
 "id": "58eaf03f0cf29e0400aaaf89",
 "userUUID": [
 "init",
 "fBI7C9Hb49",
 "utxHouIf23"
 ],
 "momentsID": "58eaf0270cf29e0400aaaf87",
 "commenterUUID": "fBI7C9Hb49",
 "commenterNickname": "动画电影",
 "targetCommentUUID": "",
 "targetCommentNickname": "",
 "message": "大好河山"
 }
 * {
 * "targetCommenterNickname":null
 * "commenterNickname":null
 * "commenterUUID": "FF2",
 * "targetCommenterUUID": "1",
 * "message": "不错"
 * }
 */
public class CommentItem {
    public String targetCommenterNickname;
    public String commenterNickname;
    public String commenterUUID;
    public String targetCommenterUUID;
    public String message;


    public CommentItem(String commenterUUID, String commenterNickname, String targetCommenterUUID, String targetCommenterNickname, String message) {
        this.commenterUUID = commenterUUID;
        this.commenterNickname = commenterNickname;
        this.targetCommenterUUID = targetCommenterUUID;
        this.targetCommenterNickname = targetCommenterNickname;
        this.message = message;
    }
}
