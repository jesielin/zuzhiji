package com.zzj.zuzhiji.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zzj.zuzhiji.R;

import java.text.DecimalFormat;
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

    public static String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }




    public static String getAvatorAddress(String uuid){
        return String.format("http://101.201.155.115:3113/heads/%s-head.jpg",uuid);
    }

    public static void loadAvator(ImageView imageView,String  url,Context context){

            Glide.with(context)
                    .load(url)
                    .asBitmap()
                    .fitCenter()
                    .placeholder(R.drawable.placeholder_circle_image)
                    .error(R.drawable.placeholder_circle_image)

//                    .placeholder(R.color.text_hint)
//                    .error(R.drawable.avator_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
//                    .priority(Priority.IMMEDIATE)
                    .transform(new GlideCircleTransform(context))

                    .into(imageView);


    }


}
