package com.zzj.zuzhiji.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by shawn on 17/3/29.
 */

public class MarqueeTextView extends android.support.v7.widget.AppCompatTextView {

    public MarqueeTextView(Context con) {
        super(con);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        // TODO Auto-generated method stub
//        return true;
        return super.isFocused();
    }
}