package com.zzj.zuzhiji;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class UserInfoSettingActivity extends BaseActivity {


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

    private Compressor compressor = new Compressor.Builder(App.getAppContext())
            .setMaxWidth(Constant.IMAGE_UPLOAD_MAX_WIDTH)
            .setMaxHeight(Constant.IMAGE_UPLOAD_MAX_HEIGHT)
            .setQuality(Constant.IMAGE_UPLOAD_QUALITY)
            .setCompressFormat(Bitmap.CompressFormat.JPEG)
            .setDestinationDirectoryPath(Glide.getPhotoCacheDir(App.getAppContext()).getAbsolutePath())
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

        studioView.setVisibility(View.GONE);
//        String userType = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.USER_TYPE);
//        switch (userType) {
//            case Constant.USER_TYPE_SINGLE:
//                //个人
//                studioView.setVisibility(View.GONE);
//                break;
//            case Constant.USER_TYPE_TECH:
//                //技师
//                studioView.setVisibility(View.VISIBLE);
//                break;
//        }


        tvStudio.setText(studioTitle);
    }

    @OnClick(R.id.back)
    public void back(View view) {
        onBackPressed();
    }

    @OnClick(R.id.summary_view)
    public void setSummary(View view) {


        DialogUtils.showEditDialog(UserInfoSettingActivity.this, "更新简介", tvSummary.getText().toString(), new DialogUtils.OnEditTextConfirmListener() {

            @Override
            public void onEditTextConfirm(String text, boolean isChanged) {
                isChangeSummary = isChanged;
                if (isChanged)
                    tvSummary.setText(text);
            }
        });

    }

    @OnClick(R.id.nickname_view)
    public void setNickName(View view) {


        DialogUtils.showEditDialog(UserInfoSettingActivity.this, "输入新的昵称", tvNickName.getText().toString(), new DialogUtils.OnEditTextConfirmListener() {

            @Override
            public void onEditTextConfirm(String text, boolean isChanged) {
                isChangeNickName = isChanged;
                if (isChanged)
                    tvNickName.setText(text);
            }
        });
    }

    @OnClick(R.id.gender_view)
    public void setGender(View view) {

        mDialog = DialogUtils.showSingleChoiceDialog(UserInfoSettingActivity.this, "选择性别", new String[]{"男", "女"}, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                genderIndex = position;
                switch (genderIndex) {
                    case 0:
                        tvGender.setText("男");
                        break;
                    case 1:
                        tvGender.setText("女");
                        break;
                }
                isChangeGender = true;
                DialogUtils.dismissDialog(mDialog);
            }
        });


    }

    @OnClick(R.id.studio_view)
    public void chooseStudio(View view) {


        Network.getInstance().getAllStudio()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mDialog = DialogUtils.showProgressDialog(UserInfoSettingActivity.this, "正在获取工作室列表...");

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
                        DialogUtils.dismissDialog(mDialog);
                    }

                    @Override
                    public void onNext(final List<StudioItem> studioItems) {
                        DialogUtils.dismissDialog(mDialog);
                        final List<String> items = new ArrayList<String>();
                        final String[] contents = new String[studioItems.size()];
                        for (int i = 0; i < studioItems.size(); i++) {
                            contents[i] = studioItems.get(i).title;
                        }

                        mDialog = DialogUtils.showSingleChoiceDialog(UserInfoSettingActivity.this, "选择工作室", contents, new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                studioId = studioItems.get(position).id;
                                studioTitle = contents[position];
                                tvStudio.setText(studioTitle);
                                isChangeStudio = true;
                                DialogUtils.dismissDialog(mDialog);
                            }
                        });

                    }
                });

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


        mDialog = DialogUtils.showProgressDialog(UserInfoSettingActivity.this, "正在设置，请稍等...");
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
                            DialogUtils.dismissDialog(mDialog);
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
        String genderText = genderIndex == 0 ? Constant.GENDER_MALE : Constant.GENDER_FEMALE;
        RequestBody gender =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), genderText);


        DebugLog.e("studio id:" + studioId);
        RequestBody studio = null;
        if (isChangeStudio) {
            studio =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), studioId);
        }


        //添加summary
        String summaryText = tvSummary.getText().toString();
        RequestBody summary =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), summaryText);


        Network.getInstance().setUserInfo(uuid, nickName, gender, summary, avatorPart)
                .subscribe(new Subscriber<SetInfoResult>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {

                        Toast.makeText(UserInfoSettingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        DialogUtils.dismissDialog(mDialog);


                    }

                    @Override
                    public void onNext(SetInfoResult setInfoResult) {
                        Map<String, String> values = new ArrayMap<String, String>();
                        values.put(Constant.SHARED_KEY.AVATOR, setInfoResult.headSculpture);
                        values.put(Constant.SHARED_KEY.NICK_NAME, setInfoResult.nickName);
                        values.put(Constant.SHARED_KEY.USER_GENDER, setInfoResult.sex);
                        values.put(Constant.SHARED_KEY.STUDIO_ID, setInfoResult.studio == null ? "" : setInfoResult.studio);
                        values.put(Constant.SHARED_KEY.STUDIO_TITLE, setInfoResult.studioTitle == null ? "" : setInfoResult.studioTitle);
                        values.put(Constant.SHARED_KEY.SUMMARY, setInfoResult.summary);
                        SharedPreferencesUtils.getInstance().setValues(values);
                        DialogUtils.dismissDialog(mDialog);

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
