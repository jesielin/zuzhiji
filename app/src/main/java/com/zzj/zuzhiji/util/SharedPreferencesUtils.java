package com.zzj.zuzhiji.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.zzj.zuzhiji.app.Constant;

import java.security.PublicKey;

/**
 * Created by shawn on 17/3/28.
 */
public class SharedPreferencesUtils {
    private SharedPreferences mSharedPreference;
    private static SharedPreferencesUtils ourInstance = new SharedPreferencesUtils();

    public static SharedPreferencesUtils getInstance() {
        return ourInstance;
    }

    private SharedPreferencesUtils() {
    }

    public void init(Context context) {
        mSharedPreference = context.getSharedPreferences(Constant.SHARED_KEY.SHARED_FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLogin() {
        if (TextUtils.isEmpty(mSharedPreference.getString(Constant.SHARED_KEY.UUID, "")))
            return false;
        return true;
    }

    public void setLogin(String uuid,String nickName,String avator,String userType,String loginName){
        mSharedPreference.edit()
                .putString(Constant.SHARED_KEY.UUID,uuid)
                .putString(Constant.SHARED_KEY.NICK_NAME,nickName)
                .putString(Constant.SHARED_KEY.AVATOR,avator)
                .putString(Constant.SHARED_KEY.USER_TYPE,userType)
                .putString(Constant.SHARED_KEY.LOGIN_NAME,loginName)
                .apply();
    }

    public void setLogout(){
        mSharedPreference.edit()
                .putString(Constant.SHARED_KEY.UUID,"")
                .putString(Constant.SHARED_KEY.NICK_NAME,"")
                .putString(Constant.SHARED_KEY.AVATOR,"")
                .putString(Constant.SHARED_KEY.USER_TYPE,"")
                .putString(Constant.SHARED_KEY.LOGIN_NAME,"")
                .apply();
    }

}
