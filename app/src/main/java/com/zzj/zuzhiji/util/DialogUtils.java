package com.zzj.zuzhiji.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by shawn on 2017-03-28.
 */

public class DialogUtils {

    public static MaterialDialog showProgressDialog(Context context,String title, String content){
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .progress(true, 0)
                .cancelable(false)
                .progressIndeterminateStyle(false);
        MaterialDialog dialog = builder.build();
        dialog.show();
        return dialog;
    }
}
