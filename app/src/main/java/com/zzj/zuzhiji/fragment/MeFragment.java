package com.zzj.zuzhiji.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzj.zuzhiji.CommentsReplyActivity;
import com.zzj.zuzhiji.HomePageActivity;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.SettingActivity;
import com.zzj.zuzhiji.UserInfoSettingActivity;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 2017-03-29.
 */

public class MeFragment extends Fragment {


    @BindView(R.id.dongtai)
    View dongtai;
    @BindView(R.id.anli)
    View anli;
    @BindView(R.id.jibie)
    View jibie;
    @BindView(R.id.shipin)
    View shipin;
    @BindView(R.id.huifu)
    View huifu;
    @BindView(R.id.guanzhu)
    View guanzhu;
    @BindView(R.id.fensi)
    View fensi;
    @BindView(R.id.summary)
    TextView tvSummary;

    @BindView(R.id.avator)
    ImageView ivAvator;
    @BindView(R.id.title)
    TextView tvTitle;

    //1 技师 0 用户
    String userType = "0";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_me, null);
        ButterKnife.bind(this, contentView);

        setupLayout();

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAvatorAndName();
    }

    private void loadAvatorAndName() {
        String avator = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.AVATOR);
        String title = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.NICK_NAME);
        String summary = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.SUMMARY);
        DebugLog.e("avator:" + avator);
        if (TextUtils.isEmpty(title)) {
            title = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.LOGIN_NAME);
        }
        tvTitle.setText(title);

        if (TextUtils.isEmpty(summary)) {
            tvSummary.setVisibility(View.GONE);
        } else {
            tvSummary.setVisibility(View.VISIBLE);
            tvSummary.setText(summary);

        }

        CommonUtils.loadAvator(ivAvator,avator,getActivity());
    }

    private void setupLayout() {
//        loadAvatorAndName();


        shipin.setVisibility(View.GONE);
        huifu.setVisibility(View.VISIBLE);
        guanzhu.setVisibility(View.GONE);
        fensi.setVisibility(View.GONE);
        userType = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.USER_TYPE);
        switch (userType) {
            case "0":
                dongtai.setVisibility(View.VISIBLE);
                anli.setVisibility(View.GONE);
                jibie.setVisibility(View.GONE);
//                shipin.setVisibility(View.GONE);
                //huifu.setVisibility(View.GONE);
//                guanzhu.setVisibility(View.VISIBLE);
//                fensi.setVisibility(View.GONE);
                break;
            case "1":
                dongtai.setVisibility(View.GONE);
                anli.setVisibility(View.VISIBLE);
                jibie.setVisibility(View.VISIBLE);
//                shipin.setVisibility(View.VISIBLE);
                //huifu.setVisibility(View.VISIBLE);
//                guanzhu.setVisibility(View.GONE);
//                fensi.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick({R.id.dongtai, R.id.anli})
    public void toHomePage(View view) {
        startActivity(HomePageActivity.newIntent(getActivity(),
                SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.AVATOR),
                SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.NICK_NAME),
                "",
                SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.USER_TYPE),
                SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID),
                Constant.USER_IS_FRIEND));
    }

    @OnClick(R.id.shezhi)
    public void setting() {
        startActivity(new Intent(getActivity(), SettingActivity.class));
    }


    @OnClick(R.id.header)
    public void setInfo(View view) {
        startActivity(new Intent(getActivity(), UserInfoSettingActivity.class));
    }

    @OnClick(R.id.huifu)
    public void message(View view) {
        startActivity(new Intent(getActivity(), CommentsReplyActivity.class));
    }
}
