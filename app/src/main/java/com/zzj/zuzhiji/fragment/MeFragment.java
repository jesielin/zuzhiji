package com.zzj.zuzhiji.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zzj.zuzhiji.CommentsReplyActivity;
import com.zzj.zuzhiji.HomePageActivity;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.SettingActivity;
import com.zzj.zuzhiji.UserInfoSettingActivity;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.StudioItem;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by shawn on 2017-03-29.
 */

public class MeFragment extends BaseFragment {


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
    @BindView(R.id.join)
    View joinStudio;

    @BindView(R.id.avator)
    ImageView ivAvator;
    @BindView(R.id.title)
    TextView tvTitle;

    //1 技师 0 用户
    String userType = "0";
    private Dialog chooseDialog;

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


        shipin.setVisibility(View.GONE);
        huifu.setVisibility(View.VISIBLE);
        guanzhu.setVisibility(View.GONE);
        fensi.setVisibility(View.GONE);
        userType = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.USER_TYPE);
        DebugLog.e("userType:" + userType);
        switch (userType) {
            case "0":
                dongtai.setVisibility(View.VISIBLE);
                anli.setVisibility(View.GONE);
                jibie.setVisibility(View.GONE);
                joinStudio.setVisibility(View.GONE);
                break;
            case "1":
                dongtai.setVisibility(View.GONE);
                anli.setVisibility(View.VISIBLE);
                jibie.setVisibility(View.VISIBLE);
                joinStudio.setVisibility(View.VISIBLE);
                break;
            case "2":
                dongtai.setVisibility(View.GONE);
                anli.setVisibility(View.VISIBLE);
                jibie.setVisibility(View.VISIBLE);
                joinStudio.setVisibility(View.GONE);
                break;
        }
    }

    @OnClick(R.id.join)
    public void joinStu() {
        Network.getInstance().getUnjoinStudioByUser(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID))
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mProgressDialog = DialogUtils.showProgressDialog(getActivity(), "正在获取工作室列表...");

                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<StudioItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), "获取工作室错误:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        DialogUtils.dismissDialog(mProgressDialog);
                    }

                    @Override
                    public void onNext(final List<StudioItem> studioItems) {
                        DialogUtils.dismissDialog(mProgressDialog);

                        if (studioItems != null && studioItems.size() == 0) {
                            Toast.makeText(getActivity(), "当前没有可选工作室", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final List<String> items = new ArrayList<String>();
                        final String[] contents = new String[studioItems.size()];
                        for (int i = 0; i < studioItems.size(); i++) {
                            contents[i] = studioItems.get(i).title;
                        }

                        chooseDialog = DialogUtils.showSingleChoiceDialog(getActivity(), "选择工作室", contents, new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                DialogUtils.dismissDialog(chooseDialog);

                                Network.getInstance().joinStudio(
                                        SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID),
                                        studioItems.get(position).id
                                )
                                        .doOnSubscribe(new Action0() {
                                            @Override
                                            public void call() {
                                                mProgressDialog = DialogUtils.showProgressDialog(getActivity(), "正在加入工作室...");

                                            }
                                        })
                                        .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Subscriber<Object>() {
                                            @Override
                                            public void onCompleted() {

                                            }

                                            @Override
                                            public void onError(Throwable e) {

                                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                DialogUtils.dismissDialog(mProgressDialog);
                                            }

                                            @Override
                                            public void onNext(Object o) {
                                                Toast.makeText(getActivity(), "加入工作室成功", Toast.LENGTH_SHORT).show();
                                                DialogUtils.dismissDialog(mProgressDialog);

                                            }
                                        });
                            }
                        });

                    }
                });
    }

    @OnClick({R.id.dongtai, R.id.anli})
    public void toHomePage() {
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
    public void setInfo() {
        startActivity(new Intent(getActivity(), UserInfoSettingActivity.class));
    }

    @OnClick(R.id.huifu)
    public void message() {
        startActivity(new Intent(getActivity(), CommentsReplyActivity.class));
    }
}
