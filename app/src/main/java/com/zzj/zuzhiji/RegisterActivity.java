package com.zzj.zuzhiji;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.RegisterResult;
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

public class RegisterActivity extends BaseActivity {
    @BindView(R.id.tel)
    EditText etTel;
    @BindView(R.id.verify)
    EditText etVerify;
    @BindView(R.id.get_verify)
    TextView tvGetVerify;
    @BindView(R.id.nickname)
    EditText etNickName;

    CountDownTimer timer;
    private boolean isGetVerifyEnable = true;
    private String type = "0";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
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

    @OnClick(R.id.register)
    public void next(View view) {
        if (TextUtils.isEmpty(etTel.getText().toString().trim()) || TextUtils.getTrimmedLength(etTel.getText().toString()) != 11) {
            Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etVerify.getText().toString().trim())) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etNickName.getText().toString().trim())) {
            Toast.makeText(this, "请输入昵称", Toast.LENGTH_SHORT).show();
            return;
        }


        Network.getInstance().register(etTel.getText().toString().trim(), etVerify.getText().toString().trim(), type, etNickName.getText().toString())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mDialog = DialogUtils.showProgressDialog(RegisterActivity.this, "正在注册..."); // 需要在主线程执行
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
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        DialogUtils.dismissDialog(mDialog);
                        DebugLog.e("error");
                    }

                    @Override
                    public void onNext(RegisterResult registerResult) {

                        SharedPreferencesUtils.getInstance().setLogin(
                                registerResult.uuid,
                                registerResult.nickName,
                                Constant.AVATOR_DEFAULT,
                                registerResult.userType,
                                registerResult.loginName,
                                Constant.GENDER_MALE,
                                Constant.EMPTY,
                                Constant.EMPTY,
                                Constant.EMPTY
                        );

                        signIn(registerResult.uuid);

                        DebugLog.e("uuid:" + registerResult.uuid);


                    }
                });


    }

    /**
     * 登录方法
     */
    private void signIn(String uuid) {

        String password = "123456";

        EMClient.getInstance().login(uuid, password, new EMCallBack() {
            /**
             * 登陆成功的回调
             */
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtils.dismissDialog(mDialog);

                        // 加载所有会话到内存
                        EMClient.getInstance().chatManager().loadAllConversations();
                        // 加载所有群组到内存，如果使用了群组的话
                        // EMClient.getInstance().groupManager().loadAllGroups();

                        // 登录成功跳转界面
                        DebugLog.e("登录成功");

                        onBackPressed();
                    }
                });
            }

            /**
             * 登陆错误的回调
             * @param i
             * @param s
             */
            @Override
            public void onError(final int i, final String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtils.dismissDialog(mDialog);
                        Log.d("lzan13", "登录失败 Error code:" + i + ", message:" + s);
                        /**
                         * 关于错误码可以参考官方api详细说明
                         * http://www.easemob.com/apidoc/android/chat3.0/classcom_1_1hyphenate_1_1_e_m_error.html
                         */
                        switch (i) {
                            // 网络异常 2
                            case EMError.NETWORK_ERROR:
                                Toast.makeText(RegisterActivity.this, "网络错误 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无效的用户名 101
                            case EMError.INVALID_USER_NAME:
                                Toast.makeText(RegisterActivity.this, "无效的用户名 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无效的密码 102
                            case EMError.INVALID_PASSWORD:
                                Toast.makeText(RegisterActivity.this, "无效的密码 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 用户认证失败，用户名或密码错误 202
                            case EMError.USER_AUTHENTICATION_FAILED:
                                Toast.makeText(RegisterActivity.this, "用户认证失败，用户名或密码错误 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 用户不存在 204
                            case EMError.USER_NOT_FOUND:
                                Toast.makeText(RegisterActivity.this, "用户不存在 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无法访问到服务器 300
                            case EMError.SERVER_NOT_REACHABLE:
                                Toast.makeText(RegisterActivity.this, "无法访问到服务器 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 等待服务器响应超时 301
                            case EMError.SERVER_TIMEOUT:
                                Toast.makeText(RegisterActivity.this, "等待服务器响应超时 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 服务器繁忙 302
                            case EMError.SERVER_BUSY:
                                Toast.makeText(RegisterActivity.this, "服务器繁忙 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 未知 Server 异常 303 一般断网会出现这个错误
                            case EMError.SERVER_UNKNOWN_ERROR:
                                Toast.makeText(RegisterActivity.this, "未知的服务器异常 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(RegisterActivity.this, "ml_sign_in_failed code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }


    @OnClick(R.id.get_verify)
    public void getVerify(View view) {
        if (!isGetVerifyEnable)
            return;

        if (TextUtils.isEmpty(etTel.getText().toString().trim()) || TextUtils.getTrimmedLength(etTel.getText().toString()) != 11) {
            Toast.makeText(RegisterActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
        onBackPressed();
    }
}

