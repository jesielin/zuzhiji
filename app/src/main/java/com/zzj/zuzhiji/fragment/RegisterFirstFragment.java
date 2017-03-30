package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.RegisterResult;
import com.zzj.zuzhiji.util.ActivityManager;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by shawn on 2017-03-28.
 */

public class RegisterFirstFragment extends Fragment {

    @BindView(R.id.tel)
    EditText etTel;
    @BindView(R.id.verify)
    EditText etVerify;
    @BindView(R.id.get_verify)
    TextView tvGetVerify;
    CountDownTimer timer;
    private boolean isGetVerifyEnable = true;
    private String type = "0";

    private MaterialDialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(container.getContext(), R.layout.fragment_register_first, null);
        ButterKnife.bind(this, contentView);
        return contentView;

    }

    @OnClick({R.id.operator, R.id.single})
    public void typeCheck(View view) {
        switch (view.getId()) {
            case R.id.operator:
                type = "1";
                break;
            case R.id.single:
                type = "0";
                break;
        }
    }

    @OnClick(R.id.next)
    public void next(View view) {
        if (TextUtils.isEmpty(etTel.getText().toString().trim()) || TextUtils.getTrimmedLength(etTel.getText().toString()) != 11) {
            Toast.makeText(getActivity(), "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etVerify.getText().toString().trim())) {
            Toast.makeText(getActivity(), "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }


        Network.getInstance().register(etTel.getText().toString().trim(), etVerify.getText().toString().trim(), type)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        dialog = DialogUtils.showProgressDialog(getActivity(), "注册", "正在注册..."); // 需要在主线程执行
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
                .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<RegisterResult>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            dismissDialog();
                            DebugLog.e("error");
                        }

                        @Override
                        public void onNext(RegisterResult registerResult) {

                            SharedPreferencesUtils.getInstance().setLogin(
                                    registerResult.uuid,
                                    "",
                                    "",
                                    registerResult.userType,
                                    registerResult.loginName
                            );
                            dismissDialog();
                            DebugLog.e("next");
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, new RegisterSecondFragment(), "second").commit();


                        }
                    });


    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;

        }
    }

    @OnClick(R.id.get_verify)
    public void getVerify(View view) {
        if (!isGetVerifyEnable)
            return;

        if (TextUtils.isEmpty(etTel.getText().toString().trim()) || TextUtils.getTrimmedLength(etTel.getText().toString()) != 11) {
            Toast.makeText(getActivity(), "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        disableTvGetVerify();
        startCountDownTime(Constant.COUNT_DOWN_TIME);


        Network.getInstance().sendSms(etTel.getText().toString().trim())
                    .subscribe(new Subscriber<Object>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(Object o) {

                        }
                    });


    }

    private void startCountDownTime(long time) {
        /**
         * 最简单的倒计时类，实现了官方的CountDownTimer类（没有特殊要求的话可以使用）
         * 即使退出activity，倒计时还能进行，因为是创建了后台的线程。
         * 有onTick，onFinsh、cancel和start方法
         */
        timer = new CountDownTimer(time * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //每隔countDownInterval秒会回调一次onTick()方法
//                DebugLog.d("onTick  " + millisUntilFinished / 1000);

                tvGetVerify.setText(String.format("%s秒", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                DebugLog.d("onFinish -- 倒计时结束");
                enableTvGetVerify();
            }
        };
        timer.start();// 开始计时
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    private void enableTvGetVerify() {
        tvGetVerify.setTextColor(getResources().getColor(R.color.md_red_800));
        tvGetVerify.setText("获取验证码");
        isGetVerifyEnable = true;
    }

    private void disableTvGetVerify() {
        tvGetVerify.setTextColor(getResources().getColor(R.color.text_hint));
        isGetVerifyEnable = false;
    }

    @OnClick(R.id.back)
    public void back(View view) {
        ActivityManager.getInstance().finshActivities(getActivity().getClass());
    }
}
