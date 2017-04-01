package com.zzj.zuzhiji.view;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by shawn on 2017-04-01.
 */

public class NestedScrollingView extends NestedScrollView {
    private int mState = RecyclerView.SCROLL_STATE_IDLE;
    private NestedScrollViewScrollStateListener mScrollListener;


    public NestedScrollingView(Context context) {
        super(context);
    }

    public NestedScrollingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedScrollingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollListener(NestedScrollViewScrollStateListener scrollListener) {
        this.mScrollListener = scrollListener;
    }

    @Override
    public void stopNestedScroll() {
        super.stopNestedScroll();
        dispatchScrollState(RecyclerView.SCROLL_STATE_IDLE);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        dispatchScrollState(RecyclerView.SCROLL_STATE_DRAGGING);
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        boolean superScroll = super.startNestedScroll(axes);
        dispatchScrollState(RecyclerView.SCROLL_STATE_DRAGGING);
        return superScroll;
    }

    private void dispatchScrollState(int state) {
        if (mScrollListener != null && mState != state) {
            mScrollListener.onNestedScrollViewStateChanged(state);
            mState = state;
        }
    }


    public interface NestedScrollViewScrollStateListener {
        void onNestedScrollViewStateChanged(int state);
    }

}
