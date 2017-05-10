package com.zzj.zuzhiji;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.OptionsPickerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.zzj.zuzhiji.app.App;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.CItem;
import com.zzj.zuzhiji.network.entity.PItem;
import com.zzj.zuzhiji.network.entity.RegisterStudioResult;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.GlideCircleTransform;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;
import com.zzj.zuzhiji.util.UIHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by shawn on 2017-05-04.
 */

public class RegisterStudioActivity extends BaseActivity {

    @BindView(R.id.tel)
    EditText etTel;
    @BindView(R.id.verify)
    EditText etVerify;
    @BindView(R.id.get_verify)
    TextView tvGetVerify;
    @BindView(R.id.nickname)
    EditText etNickName;
    @BindView(R.id.avator)
    ImageView ivAvator;
    @BindView(R.id.summary)
    EditText etSummary;
    @BindView(R.id.serial)
    EditText etSerial;
    @BindView(R.id.address)
    EditText etAddress;

    @BindView(R.id.image)
    ImageView ivDocs;

    @BindView(R.id.avator_pb)
    ProgressBar pbAvator;
    @BindView(R.id.docs_pb)
    ProgressBar pbDocs;

    @BindView(R.id.bankcardno)
    EditText etBankcardno;

    CountDownTimer timer;
    @BindView(R.id.cities)
    TextView tvCities;
    private boolean isGetVerifyEnable = true;
    private ArrayList<String> avatorPaths = new ArrayList<>();
    private ArrayList<String> docsPaths = new ArrayList<>();
    private List<PItem> provinces = new ArrayList<>();
    private List<List<CItem>> cities = new ArrayList<>();
    private String cityId;
    private String provinceId;
    private File avatorFile;
    private File docsFile;
    private boolean isChooseAddress = false;
    private Compressor compressor = new Compressor.Builder(App.getAppContext())
            .setMaxWidth(Constant.IMAGE_UPLOAD_MAX_WIDTH)
            .setMaxHeight(Constant.IMAGE_UPLOAD_MAX_HEIGHT)
            .setQuality(Constant.IMAGE_UPLOAD_QUALITY)
            .setCompressFormat(Bitmap.CompressFormat.JPEG)
            .setDestinationDirectoryPath(Glide.getPhotoCacheDir(App.getAppContext()).getAbsolutePath())
            .build();
    private int imgWhich = 0;
    private OptionsPickerView pvOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_studio);
        ButterKnife.bind(this);

        Network.getInstance().getArea()
                .subscribe(new Subscriber<List<PItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        Toast.makeText(RegisterStudioActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<PItem> pItems) {

                        if (pItems != null && pItems.size() > 0) {
                            pItems.remove(0);
                            provinces.addAll(pItems);
                            for (int i = 0; i < pItems.size(); i++) {
                                cities.add(pItems.get(i).city);
                            }

                            initAddressOptions();
                        }


                    }
                });
    }

    @OnClick(R.id.choose_img)
    public void chooseImage() {
        imgWhich = 1;
        choicePhotoWrapper();
    }

    @OnClick(R.id.cities)
    public void showAdd() {
        UIHelper.hideSoftInput(this, etNickName);
        if (pvOptions != null)
            pvOptions.show();
    }

    private void initAddressOptions() {
        pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
//                String tx = options1Items.get(options1).getPickerViewText()
//                        + options2Items.get(options1).get(options2)
//                       /* + options3Items.get(options1).get(options2).get(options3).getPickerViewText()*/;
//                btn_Options.setText(tx);
                DebugLog.e("index1:" + options1);
                DebugLog.e("index2:" + options2);
                PItem item = provinces.get(options1);

                provinceId = item.Id;
                cityId = item.city.get(options2).Id;
                tvCities.setText(item.Name + " " + item.city.get(options2).Name);
                isChooseAddress = true;
            }
        })
                .setTitleText("城市选择")
                .setContentTextSize(20)//设置滚轮文字大小
                .setDividerColor(getResources().getColor(R.color.colorPrimaryDark))//设置分割线的颜色
                .setSelectOptions(0, 0)//默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(getResources().getColor(R.color.colorPrimary))
                .setTitleColor(getResources().getColor(R.color.colorPrimaryDark))
                .setCancelColor(getResources().getColor(R.color.colorPrimaryDark))
                .setSubmitColor(getResources().getColor(R.color.colorPrimaryDark))
                .setTextColorCenter(getResources().getColor(R.color.colorPrimaryDark))
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
//                .setLabels("省","","区")
                .build();

        //pvOptions.setSelectOptions(1,1);



        /*pvOptions.setPicker(options1Items);//一级选择器*/
        pvOptions.setPicker(provinces, cities);//二级选择器
    }

    @OnClick(R.id.register)
    public void register() {

        if (avatorFile == null) {
            Toast.makeText(this, "请选择头像", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etTel.getText().toString().trim())) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etVerify.getText().toString().trim())) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etNickName.getText().toString().trim())) {
            Toast.makeText(this, "请输入工作室名称", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etSummary.getText().toString().trim())) {
            Toast.makeText(this, "请输入简介", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etSerial.getText().toString().trim())) {
            Toast.makeText(this, "请输入序列号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isChooseAddress) {
            Toast.makeText(this, "请选择城市", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etAddress.getText().toString().trim())) {
            Toast.makeText(this, "请输入详细地址", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etBankcardno.getText().toString().trim())) {
            Toast.makeText(this, "请输入银行卡号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (docsFile == null) {
            Toast.makeText(this, "请选择资质", Toast.LENGTH_SHORT).show();
            return;
        }


        upload();


    }

    private void upload() {


        mDialog = DialogUtils.showProgressDialog(RegisterStudioActivity.this, "请稍等...");


        /**
         * RequestBody loginName,
         RequestBody identifyCode,
         RequestBody title,
         RequestBody summary,
         RequestBody serial,
         RequestBody province,
         RequestBody city,
         RequestBody address,
         RequestBody bankcardno,
         MultipartBody.Part headSculpture,
         MultipartBody.Part license
         */


        RequestBody loginName = mkRqBody(etTel.getText().toString().trim());
        RequestBody identifyCode = mkRqBody(etVerify.getText().toString().trim());
        RequestBody title = mkRqBody(etNickName.getText().toString().trim());
        RequestBody summary = mkRqBody(etSummary.getText().toString());
        RequestBody serial = mkRqBody(etSerial.getText().toString().trim());
        RequestBody province = mkRqBody(provinceId);
        RequestBody city = mkRqBody(cityId);
        RequestBody address = mkRqBody(etAddress.getText().toString());
        RequestBody bankcardno = mkRqBody(etBankcardno.getText().toString().trim());

        // 创建 RequestBody，用于封装构建RequestBody
        RequestBody requestAvatorFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), avatorFile);

        // MultipartBody.Part  和后端约定好Key，这里的partName是用image
        MultipartBody.Part avatorPart =
                MultipartBody.Part.createFormData("headSculpture", avatorFile.getName(), requestAvatorFile);

        // 创建 RequestBody，用于封装构建RequestBody
        RequestBody requestDocsFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), docsFile);

        // MultipartBody.Part  和后端约定好Key，这里的partName是用image
        MultipartBody.Part docsPart =
                MultipartBody.Part.createFormData("license", docsFile.getName(), requestDocsFile);


        Network.getInstance().registerStudio(
                loginName, identifyCode, title, summary, serial, province, city, address, bankcardno, avatorPart, docsPart
        ).subscribe(new Subscriber<RegisterStudioResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                DialogUtils.dismissDialog(mDialog);
                Toast.makeText(RegisterStudioActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(RegisterStudioResult registerStudioResult) {
                Toast.makeText(RegisterStudioActivity.this, "注册成功", Toast.LENGTH_SHORT).show();

                SharedPreferencesUtils.getInstance().setStudioLogin(
                        registerStudioResult.summary,
                        registerStudioResult.address,
                        registerStudioResult.operateStatus,
                        registerStudioResult.city,
                        registerStudioResult.title,
                        registerStudioResult.uuid,
                        registerStudioResult.headSculpture,
                        registerStudioResult.license,
                        registerStudioResult.province,
                        registerStudioResult.serial,
                        registerStudioResult.loginName,
                        registerStudioResult.userType,
                        registerStudioResult.createDate,
                        registerStudioResult.status
                );

                signIn(registerStudioResult.uuid);
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
             *
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
                                Toast.makeText(RegisterStudioActivity.this, "网络错误 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无效的用户名 101
                            case EMError.INVALID_USER_NAME:
                                Toast.makeText(RegisterStudioActivity.this, "无效的用户名 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无效的密码 102
                            case EMError.INVALID_PASSWORD:
                                Toast.makeText(RegisterStudioActivity.this, "无效的密码 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 用户认证失败，用户名或密码错误 202
                            case EMError.USER_AUTHENTICATION_FAILED:
                                Toast.makeText(RegisterStudioActivity.this, "用户认证失败，用户名或密码错误 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 用户不存在 204
                            case EMError.USER_NOT_FOUND:
                                Toast.makeText(RegisterStudioActivity.this, "用户不存在 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无法访问到服务器 300
                            case EMError.SERVER_NOT_REACHABLE:
                                Toast.makeText(RegisterStudioActivity.this, "无法访问到服务器 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 等待服务器响应超时 301
                            case EMError.SERVER_TIMEOUT:
                                Toast.makeText(RegisterStudioActivity.this, "等待服务器响应超时 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 服务器繁忙 302
                            case EMError.SERVER_BUSY:
                                Toast.makeText(RegisterStudioActivity.this, "服务器繁忙 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 未知 Server 异常 303 一般断网会出现这个错误
                            case EMError.SERVER_UNKNOWN_ERROR:
                                Toast.makeText(RegisterStudioActivity.this, "未知的服务器异常 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(RegisterStudioActivity.this, "ml_sign_in_failed code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
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

    private RequestBody mkRqBody(String text) {
        return RequestBody.create(
                MediaType.parse("multipart/form-data"), text);
    }


    @OnClick(R.id.avator)
    public void chooseAvator(View view) {
        imgWhich = 0;
        choicePhotoWrapper();

    }

    @AfterPermissionGranted(Constant.REQUEST_CODE_PERMISSION_PHOTO_PICKER)
    private void choicePhotoWrapper() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // 拍照后照片的存放目录，改成你自己拍照后要存放照片的目录。如果不传递该参数的话就没有拍照功能
            File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");

            switch (imgWhich) {
                case 0:
                    startActivityForResult(BGAPhotoPickerActivity.newIntent(this, takePhotoDir, 1, avatorPaths, true), Constant.REQUEST_CODE_CHOOSE_PHOTO);
                    break;
                case 1:
                    startActivityForResult(BGAPhotoPickerActivity.newIntent(this, takePhotoDir, 1, docsPaths, true), Constant.REQUEST_CODE_CHOOSE_PHOTO);
                    break;
            }

        } else {
            EasyPermissions.requestPermissions(this, "图片选择需要以下权限:\n\n1.访问设备上的照片\n\n2.拍照", Constant.REQUEST_CODE_PERMISSION_PHOTO_PICKER, perms);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DebugLog.e("result:" + requestCode + "," + resultCode);
        if (resultCode == RESULT_OK && requestCode == Constant.REQUEST_CODE_CHOOSE_PHOTO) {
            switch (imgWhich) {
                case 0:

                    pbAvator.setVisibility(View.VISIBLE);

                    avatorPaths.clear();
                    avatorPaths.addAll(BGAPhotoPickerActivity.getSelectedImages(data));


                    compressor.compressToFileAsObservable(new File(avatorPaths.get(0)))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<File>() {
                                @Override
                                public void call(File file) {


                                    avatorFile = file;
                                    Glide.with(RegisterStudioActivity.this).load(file)
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .transform(new GlideCircleTransform(RegisterStudioActivity.this))
                                            .into(ivAvator);

                                    pbAvator.setVisibility(View.GONE);
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    pbAvator.setVisibility(View.GONE);
                                    Toast.makeText(RegisterStudioActivity.this, "压缩头像错误", Toast.LENGTH_SHORT).show();
                                }
                            });


                    break;
                case 1:

                    pbDocs.setVisibility(View.VISIBLE);
                    docsPaths.clear();
                    docsPaths.addAll(BGAPhotoPickerActivity.getSelectedImages(data));


                    compressor.compressToFileAsObservable(new File(docsPaths.get(0)))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<File>() {
                                @Override
                                public void call(File file) {

                                    docsFile = file;
                                    ivDocs.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                    pbDocs.setVisibility(View.GONE);
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    pbDocs.setVisibility(View.GONE);
                                    Toast.makeText(RegisterStudioActivity.this, "压缩资质图片错误", Toast.LENGTH_SHORT).show();
                                }
                            });
                    break;
            }

        }

    }

    @OnClick(R.id.get_verify)
    public void getVerify(View view) {
        if (!isGetVerifyEnable)
            return;

        if (TextUtils.isEmpty(etTel.getText().toString().trim()) || TextUtils.getTrimmedLength(etTel.getText().toString()) != 11) {
            Toast.makeText(RegisterStudioActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(RegisterStudioActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
