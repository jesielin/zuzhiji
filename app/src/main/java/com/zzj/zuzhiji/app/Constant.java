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

    int REQUEST_CODE_PERMISSION_PHOTO_PREVIEW = 1;
    int REQUEST_CODE_PERMISSION_PHOTO_PICKER = 1;
    int REQUEST_CODE_CHOOSE_PHOTO = 1;
    int REQUEST_CODE_PHOTO_PREVIEW = 2;

    String USER_TYPE_SINGLE = "0";
    String USER_TYPE_TECH = "1";

    String USER_IS_FRIEND = "1";
    String USER_NOT_FRIEND = "0";
    interface SHARED_KEY {
        String SHARED_FILE_NAME = "ZZJ";
        String UUID = "UUID";
        String AVATOR = "AVATOR";
        String LOGIN_NAME = "LOGIN_NAME";
        String NICK_NAME = "NICK_NAME";
        String USER_TYPE = "USER_TYPE";

        String NEWS_TAB_INDEX = "NEWS_TAB_INDEX";


    }

    interface SHARED_VALUES{
        String NEWS_VIDEO_TAB_INDEX = "0";
        String NEWS_TEXT_TAB_INDEX = "1";
    }

    interface ACTIVITY_CODE {
        int REQUEST_CODE_SOCIAL_FRAGMENT = 0x01;
        int RESULT_CODE_PUBLISH_SUCCESS = 0x02;

        int REQUEST_CODE_SEARCH_TO_HOME_PAGE = 0x03;
        int RESULT_CODE_HOME_PAGE_CHANGE_STATUS_BACK_TO_SEARCH = 0x04;

        int REQUEST_CODE_SOCIAL_TO_DETAIL = 0x05;
        int RESULT_CODE_DETAIL_CHANGE_STATUS_BACK_TO_SOCIAL = 0x06;

    }

    interface HOME_PAGE_KEYS {
        String FRIEND_UUID = "FRIEND_UUID";
        String IS_FRIEND = "IS_FRIEND";
        String FRIEND_AVATOR = "FRIEND_AVATOR";
        String FRIEND_NICKNAME = "FRIEND_NICKNAME";
        String FRIEND_SUMMARY = "FRIEND_SUMMARY";
        String FRIEND_TYPE = "FRIEND_TYPE";

    }

    interface CASE_DETAIL_KEYS {
        String ITEM_JSON = "ITEM_JSON";
    }

    interface VIDEO_PLAY_KEYS {
        String VIDEO_URL = "VIDEO_URL";
    }

}
