package com.zzj.zuzhiji.network.entity;

/**
 * Created by shawn on 17/2/19.
 */

/**
 * {
 * "commenterUUID": "FF2",
 * "targetCommenterUUID": "1",
 * "message": "不错"
 * }
 */
public class CommentItem {
    public String commenterUUID;
    public String targetCommenterUUID;
    public String message;


    public CommentItem(String commenterUUID, String targetCommenterUUID, String message) {
        this.commenterUUID = commenterUUID;
        this.targetCommenterUUID = targetCommenterUUID;
        this.message = message;
    }
}
