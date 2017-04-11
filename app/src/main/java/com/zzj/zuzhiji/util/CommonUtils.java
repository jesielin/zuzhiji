package com.zzj.zuzhiji.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by shawn on 17/3/29.
 */

public class CommonUtils {
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日",
            Locale.getDefault());

    public static String getDate(double timeMills) {

        return sdf.format(timeMills);
    }


    public static void showSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        //imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static void hideSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
    }

    public static boolean isShowSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //获取状态信息
        return imm.isActive();//true 打开
    }

    public static String getAvatorAddress(String uuid){
        return String.format("http://101.201.155.115:3113/heads/%s-head.jpg",uuid);
    }
}
