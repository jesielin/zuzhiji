package com.zzj.zuzhiji.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.zzj.zuzhiji.app.Constant;

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

}
