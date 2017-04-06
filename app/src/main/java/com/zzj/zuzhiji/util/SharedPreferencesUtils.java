package com.zzj.zuzhiji.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.zzj.zuzhiji.app.Constant;

import java.util.Map;

/**
 * Created by shawn on 17/3/28.
 */
public class SharedPreferencesUtils {
    private static SharedPreferencesUtils ourInstance = new SharedPreferencesUtils();
    private SharedPreferences mSharedPreference;

    private SharedPreferencesUtils() {
    }

    public static SharedPreferencesUtils getInstance() {
        return ourInstance;
    }

    public void init(Context context) {
        mSharedPreference = context.getSharedPreferences(Constant.SHARED_KEY.SHARED_FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLogin() {
        return !TextUtils.isEmpty(mSharedPreference.getString(Constant.SHARED_KEY.UUID, ""));
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

    public String getValue(String key) {
        return mSharedPreference.getString(key, "");
    }

    public void setValues(Map<String, String> items) {
        SharedPreferences.Editor edit = mSharedPreference.edit();
        for (Map.Entry entry : items.entrySet()) {
            edit.putString(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        edit.apply();
    }

    public void setValue(String key,String value){
        SharedPreferences.Editor edit = mSharedPreference.edit();
            edit.putString(key,  value);
        edit.apply();
    }

    public boolean isEmLogin() {

        return mSharedPreference.getBoolean("EM_LOGIN", false);
    }

    public void setEmLogin(boolean isLogin) {
        mSharedPreference.edit().putBoolean("EM_LOGIN", isLogin).apply();
    }
}
