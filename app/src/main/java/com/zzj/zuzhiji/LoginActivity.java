package com.zzj.zuzhiji;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.LoginResult;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

public class LoginActivity extends AppCompatActivity {


    @BindView(R.id.tel)
    EditText etTel;
    @BindView(R.id.verify)
    EditText etVerify;
    @BindView(R.id.get_verify)
    TextView tvGetVerify;
    CountDownTimer timer;
    private boolean isGetVerifyEnable = true;
    private MaterialDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);


        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SharedPreferencesUtils.getInstance().isLogin()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @OnClick(R.id.login)
    public void login(View view) {
        if (TextUtils.isEmpty(etTel.getText().toString().trim()) || TextUtils.getTrimmedLength(etTel.getText().toString()) != 11) {
            Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etVerify.getText().toString().trim())){
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }


            Network.getInstance().login(etTel.getText().toString().trim(), etVerify.getText().toString().trim())
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            dialog = DialogUtils.showProgressDialog(LoginActivity.this, "登录", "正在登录，请稍等..."); // 需要在主线程执行
                        }
                    })
                    .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<LoginResult>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            dismissDialog();
                        }

                        @Override
                        public void onNext(LoginResult loginResult) {
                            SharedPreferencesUtils.getInstance().setLogin(
                                    loginResult.uuid,
                                    loginResult.nickName,
                                    TextUtils.isEmpty(loginResult.headSculpture)?Constant.AVATOR_DEFAULT:loginResult.headSculpture,
                                    loginResult.userType,
                                    loginResult.loginName,
                                    TextUtils.isEmpty(loginResult.sex)?Constant.GENDER_MALE:loginResult.sex
                            );


                            DebugLog.e("uuid:" + loginResult.uuid);

//                            EMClient.getInstance().login(loginResult.uuid, "123456", new EMCallBack() {//回调
//                                @Override
//                                public void onSuccess() {
//                                    EMClient.getInstance().groupManager().loadAllGroups();
//                                    EMClient.getInstance().chatManager().loadAllConversations();
//
//                                    DebugLog.d("登录聊天服务器成功！");
//                                    SharedPreferencesUtils.getInstance().setEmLogin(true);
//                                    dismissDialog();
//
//                                }
//
//                                @Override
//                                public void onProgress(int progress, String status) {
//
//                                }
//
//                                @Override
//                                public void onError(int code, String message) {
//                                    DebugLog.d("code:"+code+",MESSAGE:"+message);
//                                    SharedPreferencesUtils.getInstance().setEmLogin(false);
//                                    dismissDialog();
//                                }
//                            });


                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            finish();

                        }
                    });


    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @OnClick(R.id.register)
    public void register(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @OnClick(R.id.get_verify)
    public void getVerify(View view) {
        if (!isGetVerifyEnable)
            return;

        if (TextUtils.isEmpty(etTel.getText().toString().trim()) || TextUtils.getTrimmedLength(etTel.getText().toString()) != 11) {
            Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
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
}
