package com.zzj.zuzhiji;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zzj.zuzhiji.app.App;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.SetInfoResult;
import com.zzj.zuzhiji.network.entity.StudioItem;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.GlideCircleTransform;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by shawn on 17/4/7.
 */

public class UserInfoSettingActivity extends AppCompatActivity {


    @BindView(R.id.nickname)
    TextView tvNickName;
    @BindView(R.id.gender)
    TextView tvGender;
    @BindView(R.id.studio)
    TextView tvStudio;
    @BindView(R.id.avator)
    ImageView ivAvator;
    @BindView(R.id.studio_view)
    View studioView;
    @BindView(R.id.summary)
    TextView tvSummary;

    private int genderIndex = 0;

    private MaterialDialog dialog;


    private boolean isChangeAvator = false;
    private boolean isChangeNickName = false;
    private boolean isChangeGender = false;
    private boolean isChangeStudio = false;
    private boolean isChangeSummary = false;

    private String mAvator;
    private String mNickName;
    private String mGender;
    private String studioTitle;
    private String mSummary;
    private String studioId;

    private ArrayList<String> paths = new ArrayList<>();

    private Compressor compressor = new Compressor.Builder(this)
            .setMaxWidth(Constant.IMAGE_UPLOAD_MAX_WIDTH)
            .setMaxHeight(Constant.IMAGE_UPLOAD_MAX_HEIGHT)
            .setQuality(Constant.IMAGE_UPLOAD_QUALITY)
            .setCompressFormat(Bitmap.CompressFormat.JPEG)
            .setDestinationDirectoryPath(Glide.getPhotoCacheDir(this).getAbsolutePath())
            .build();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_setting);
        ButterKnife.bind(this);

        mAvator = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.AVATOR);
        mNickName = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.NICK_NAME);
        mGender = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.USER_GENDER);
        studioTitle = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.STUDIO_TITLE);
        mSummary = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.SUMMARY);
        studioId = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.STUDIO_ID);

        CommonUtils.loadAvator(ivAvator, mAvator, this);

        tvSummary.setText(mSummary);

        tvNickName.setText(mNickName);

        switch (mGender) {
            case Constant.GENDER_MALE:
                tvGender.setText("男");
                genderIndex = 0;
                break;
            case Constant.GENDER_FEMALE:
                tvGender.setText("女");
                genderIndex = 1;
                break;
        }

        String userType = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.USER_TYPE);
        switch (userType) {
            case Constant.USER_TYPE_SINGLE:
                //个人
                studioView.setVisibility(View.GONE);
                break;
            case Constant.USER_TYPE_TECH:
                //技师
                studioView.setVisibility(View.VISIBLE);
                break;
        }


        tvStudio.setText(studioTitle);
    }

    @OnClick(R.id.back)
    public void back(View view) {
        onBackPressed();
    }

    @OnClick(R.id.summary_view)
    public void setSummary(View view) {
        new MaterialDialog.Builder(this)
                .title("更新简介")

                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .inputRange(1, 80)
                .theme(Theme.LIGHT)
                .positiveText("确定")
                .input("简介", tvSummary.getText().toString(), false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        tvSummary.setText(input.toString());
                        isChangeSummary = true;
                    }
                }).show();

    }

    @OnClick(R.id.nickname_view)
    public void setNickName(View view) {
        new MaterialDialog.Builder(this)
                .title("输入新的昵称")

                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .inputRange(1, 16)
                .theme(Theme.LIGHT)
                .positiveText("确定")
                .input("昵称", tvNickName.getText().toString(), false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        tvNickName.setText(input.toString());
                        isChangeNickName = true;
                    }
                }).show();

    }

    @OnClick(R.id.gender_view)
    public void setGender(View view) {
        new MaterialDialog.Builder(this)
                .title("选择性别")
                .items("男", "女")
                .theme(Theme.LIGHT)
                .itemsCallbackSingleChoice(genderIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        genderIndex = which;
                        switch (genderIndex) {
                            case 0:
                                tvGender.setText("男");
                                break;
                            case 1:
                                tvGender.setText("女");
                                break;
                        }
                        isChangeGender = true;
                        return true;
                    }
                })
                .positiveText("确定")
                .show();
    }

    @OnClick(R.id.studio_view)
    public void chooseStudio(View view) {
        Network.getInstance().getAllStudio()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        dialog = new MaterialDialog.Builder(UserInfoSettingActivity.this)
                                .title("工作室")
                                .content("获取列表中..")
                                .progress(true, 0)
                                .cancelable(false)
                                .theme(Theme.LIGHT)
                                .progressIndeterminateStyle(false)
                                .show();

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
                        Toast.makeText(UserInfoSettingActivity.this, "获取工作室错误:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dismissDialog();
                    }

                    @Override
                    public void onNext(final List<StudioItem> studioItems) {
                        dismissDialog();
                        final List<String> items = new ArrayList<String>();
                        for (StudioItem i : studioItems) {
                            items.add(i.title);
                        }

                        new MaterialDialog.Builder(UserInfoSettingActivity.this)
                                .title("选择工作室")
                                .items(items)
                                .cancelable(true)
                                .theme(Theme.LIGHT)
                                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
//                                        Toast.makeText(UserInfoSettingActivity.this, which + "", Toast.LENGTH_SHORT).show();
//                                        dismissDialog();
                                        studioId = studioItems.get(which).id;
                                        studioTitle = text.toString();
                                        tvStudio.setText(studioTitle);
                                        isChangeStudio = true;
                                        return true;
                                    }
                                })
                                .positiveText("确定").show();
                    }
                });

    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @OnClick(R.id.avator)
    public void chooseAvator(View view) {
        choicePhotoWrapper();

    }

    @AfterPermissionGranted(Constant.REQUEST_CODE_PERMISSION_PHOTO_PICKER)
    private void choicePhotoWrapper() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // 拍照后照片的存放目录，改成你自己拍照后要存放照片的目录。如果不传递该参数的话就没有拍照功能
            File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");

            startActivityForResult(BGAPhotoPickerActivity.newIntent(this, takePhotoDir, 1, paths, true), Constant.REQUEST_CODE_CHOOSE_PHOTO);
        } else {
            EasyPermissions.requestPermissions(this, "图片选择需要以下权限:\n\n1.访问设备上的照片\n\n2.拍照", Constant.REQUEST_CODE_PERMISSION_PHOTO_PICKER, perms);
        }
    }

    @OnClick(R.id.complete)
    public void complete(View view) {


        if (!isChangeAvator && !isChangeNickName && !isChangeGender && !isChangeStudio && !isChangeSummary) {
            Toast.makeText(this, "没有更改..", Toast.LENGTH_SHORT).show();
            return;
        }


        dialog = DialogUtils.showProgressDialog(this, "设置信息", "正在设置，请稍等...");
        if (isChangeAvator) {
            File avatorFile = new File(paths.get(0));


            compressor.compressToFileAsObservable(isChangeAvator ? avatorFile : null)
                    .subscribeOn(Schedulers.computation())

                    .subscribe(new Subscriber<File>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(UserInfoSettingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            dismissDialog();
                        }

                        @Override
                        public void onNext(File file) {
                            update(file);
                        }
                    });
        } else {
            update(null);
        }


    }

    private void update(File file) {

        MultipartBody.Part avatorPart = null;
        if (isChangeAvator) {

            // 创建 RequestBody，用于封装构建RequestBody
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), file);

            // MultipartBody.Part  和后端约定好Key，这里的partName是用image
            avatorPart =
                    MultipartBody.Part.createFormData("headSculpture", file.getName(), requestFile);
        }


        //添加UUID
        String uuidText = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID);
        RequestBody uuid =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), uuidText);


        // 添加nickname
        String nickNameText = tvNickName.getText().toString();
        RequestBody nickName =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), nickNameText);


        // 添加sex
        final String genderText = genderIndex == 0 ? Constant.GENDER_MALE : Constant.GENDER_FEMALE;
        RequestBody gender =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), genderText);


        RequestBody studio =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), studioId);

        //添加summary
        String summaryText = tvSummary.getText().toString();
        RequestBody summary =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), summaryText);

        Network.getInstance().setUserInfo(uuid, nickName, gender, studio, summary, avatorPart)
                .subscribe(new Subscriber<SetInfoResult>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {

                        Toast.makeText(UserInfoSettingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        dismissDialog();


                    }

                    @Override
                    public void onNext(SetInfoResult setInfoResult) {
                        Map<String, String> values = new ArrayMap<String, String>();
                        values.put(Constant.SHARED_KEY.AVATOR, CommonUtils.getAvatorAddress(setInfoResult.uuid));
                        values.put(Constant.SHARED_KEY.NICK_NAME, setInfoResult.nickName);
                        values.put(Constant.SHARED_KEY.USER_GENDER, setInfoResult.sex);
                        values.put(Constant.SHARED_KEY.STUDIO_ID, setInfoResult.studio);
                        values.put(Constant.SHARED_KEY.STUDIO_TITLE, setInfoResult.studioTitle);
                        values.put(Constant.SHARED_KEY.SUMMARY, setInfoResult.summary);
                        SharedPreferencesUtils.getInstance().setValues(values);
                        dismissDialog();

                        finish();

                    }
                });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DebugLog.e("result:" + requestCode + "," + resultCode);
        if (resultCode == RESULT_OK && requestCode == Constant.REQUEST_CODE_CHOOSE_PHOTO) {
            paths.clear();
            paths.addAll(BGAPhotoPickerActivity.getSelectedImages(data));

            isChangeAvator = true;

            Glide.with(this).load(paths.get(0))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transform(new GlideCircleTransform(this))
                    .into(ivAvator);
        }

    }

}
