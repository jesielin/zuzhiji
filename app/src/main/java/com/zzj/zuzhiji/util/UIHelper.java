package com.zzj.zuzhiji.util;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


/**
 * Created by 大灯泡 on 2016/10/26.
 * <p>
 * ui工具类
 */
public class UIHelper {


    //图片压缩

    // =============================================================tools
    // methods


    /**
     * dip转px
     */
    public static int dipToPx(float dip,Context context) {
        return (int) (dip * context.getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    /**
     * px转dip
     */
    public static int pxToDip(float pxValue,Context context) {
        final float scale = context.getResources()
                .getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将sp值转换为px值
     */
    public static int sp2px(float spValue,Context context) {
        final float fontScale = context.getResources()
                .getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取屏幕分辨率：宽
     */
    public static int getScreenWidthPix(@Nullable Context context) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        return width;
    }

    /**
     * 获取屏幕分辨率：高
     */
    public static int getScreenHeightPix(@Nullable Context context) {
        int height = context.getResources().getDisplayMetrics().heightPixels;
        return height;
    }

    /**
     * 获取状态栏的高度
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources()
                    .getDimensionPixelSize(resourceId);
        }
        return result;
    }


    /**
     * 显示软键盘
     */
    public static void showSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        //imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 隐藏软键盘
     * @param context
     * @param view
     */
    public static void hideSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
    }


    /**
     * 键盘显示状态
     * @param context
     * @return
     */
    public static boolean isShowSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //获取状态信息
        return imm.isActive();//true 打开
    }


    /**
     * Toast封装
     */
    public static void ToastMessage(String msg,Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * =============================================================
     * 一些工具方法
     */
    public static void setViewsClickListener(@NonNull View.OnClickListener listener, View... views) {
        for (View view : views) {
            if (view != null) {
                view.setOnClickListener(listener);
            }
        }
    }


    /**
     * =============================================================
     * 资源工具
     */

    public static int getResourceColor(int colorResId,Context context) {
        try {
            return context
                    .getResources()
                    .getColor(colorResId);
        } catch (Exception e) {
            return Color.TRANSPARENT;
        }
    }


}
