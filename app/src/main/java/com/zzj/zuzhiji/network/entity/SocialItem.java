package com.zzj.zuzhiji.network.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shawn on 17/2/19.
 */

/*
{
    "momentOwner": "FFF1-33",
    "momentUserNickname":null
    "comments": {
        "commenterUUID": "FF2",
        "targetCommenterUUID": "1",
        "message": "不错"
    },
    "momentsID": "58a5522e599014decb00987f",
    "message": "我喜欢榴莲酥3",
    "photos": null,
    "createDate": 1487229486991
}
 */
public class SocialItem {
    public String momentUserNickname;
    public String momentsID;
    public String momentOwner;
    public String momentOwnerHead;
    public ArrayList<String> photos;
    public String message;
    public List<CommentItem> comments;
    public String createDate;
    public boolean isExpand;

    public boolean hasComments() {
        return comments != null && comments.size() > 0;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        this.isExpand = expand;
    }

    @Override
    public String toString() {
        return "SocialItem{" +
                "momentsID='" + momentsID + '\'' +
                ", momentOwner='" + momentOwner + '\'' +
                ", photos=" + photos +
                ", message='" + message + '\'' +
                ", comments=" + comments +
                ", createDate='" + createDate + '\'' +
                ", isExpand=" + isExpand +
                '}';
    }


}
