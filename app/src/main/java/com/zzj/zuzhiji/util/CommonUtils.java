package com.zzj.zuzhiji.util;

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
}
