package com.zzj.zuzhiji.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

/**
 * Created by shawn on 2017-03-28.
 */

public class DialogUtils {

    public static MaterialDialog showProgressDialog(Context context,String title, String content){
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .progress(true, 0)
                .cancelable(false)
                .theme(Theme.LIGHT)
                .progressIndeterminateStyle(false)
                .show();

        return dialog;
    }
}
