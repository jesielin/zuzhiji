package com.zzj.zuzhiji;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.zzj.zuzhiji.app.App;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGASortableNinePhotoLayout;
import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by shawn on 2017-03-30.
 */

public class PublishActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, BGASortableNinePhotoLayout.Delegate {

    //    private static final int REQUEST_CODE_PERMISSION_PHOTO_PICKER = 1;
//    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
//    private static final int REQUEST_CODE_PHOTO_PREVIEW = 2;
    @BindView(R.id.title)
    EditText etTitle;
    @BindView(R.id.subtitle)
    EditText etSubTitle;
    /**
     * 拖拽排序九宫格控件
     */
    @BindView(R.id.image_group)
    BGASortableNinePhotoLayout mPhotosSnpl;
    private MaterialDialog dialog;
    private Compressor compressor = new Compressor.Builder(App.getContext())
            .setMaxWidth(Constant.IMAGE_UPLOAD_MAX_WIDTH)
            .setMaxHeight(Constant.IMAGE_UPLOAD_MAX_HEIGHT)
            .setQuality(Constant.IMAGE_UPLOAD_QUALITY)
            .setCompressFormat(Bitmap.CompressFormat.JPEG)
            .setDestinationDirectoryPath(Glide.getPhotoCacheDir(App.getContext()).getAbsolutePath())
            .build();
    private ArrayList<String> paths = new ArrayList<>();
//    private List<File> files = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        ButterKnife.bind(this);

        setupLayout();
    }

    private void setupLayout() {
        mPhotosSnpl.setMaxItemCount(9);
        mPhotosSnpl.setEditable(true);
        mPhotosSnpl.setPlusEnable(true);
        mPhotosSnpl.setDelegate(this);
        mPhotosSnpl.setData(paths);
    }

    @AfterPermissionGranted(Constant.REQUEST_CODE_PERMISSION_PHOTO_PICKER)
    private void choicePhotoWrapper() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // 拍照后照片的存放目录，改成你自己拍照后要存放照片的目录。如果不传递该参数的话就没有拍照功能
            File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");

            startActivityForResult(BGAPhotoPickerActivity.newIntent(this, takePhotoDir, mPhotosSnpl.getMaxItemCount(), paths, true), Constant.REQUEST_CODE_CHOOSE_PHOTO);
        } else {
            EasyPermissions.requestPermissions(this, "图片选择需要以下权限:\n\n1.访问设备上的照片\n\n2.拍照", Constant.REQUEST_CODE_PERMISSION_PHOTO_PICKER, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == Constant.REQUEST_CODE_PERMISSION_PHOTO_PICKER) {
            Toast.makeText(this, "您拒绝了「图片选择」所需要的相关权限!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.REQUEST_CODE_CHOOSE_PHOTO)
                mPhotosSnpl.addMoreData(BGAPhotoPickerActivity.getSelectedImages(data));

            else if (requestCode == Constant.REQUEST_CODE_PHOTO_PREVIEW) {
                mPhotosSnpl.setData(BGAPhotoPickerPreviewActivity.getSelectedImages(data));
            }
        }
    }

    @OnClick(R.id.back)
    public void back(View view) {
        finish();
    }

    @OnClick(R.id.complete)
    public void complete(View view) {
        if (TextUtils.isEmpty(etTitle.getText().toString().trim())) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etSubTitle.getText().toString().trim())) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        dialog = DialogUtils.showProgressDialog(this, "发表案例", "正在上传...");
        //TODO:DIALOG BUG

        Observable.create(new Observable.OnSubscribe<List<File>>() {
            @Override
            public void call(Subscriber<? super List<File>> subscriber) {
                List<File> files = new ArrayList<File>();
                for (String path : paths) {
                    files.add(compressor.compressToFile(new File(path)));
                }
                DebugLog.e("files:" + new Gson().toJson(files));
                subscriber.onNext(files);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.computation())

                .subscribe(new Subscriber<List<File>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        DebugLog.e("error");
                        Toast.makeText(PublishActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        dismissDialog();
                    }

                    @Override
                    public void onNext(List<File> files) {
                        DebugLog.e("files:" + new Gson().toJson(files));
                        upload(files);
                    }
                });

    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private void upload(List<File> files) {
        MultipartBody.Part[] parts = new MultipartBody.Part[files.size()];
        for (int i = 0; i < files.size(); i++) {

            // 创建 RequestBody，用于封装构建RequestBody
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), files.get(i));

            // MultipartBody.Part  和后端约定好Key，这里的partName是用image
            parts[i] = MultipartBody.Part.createFormData("photos", files.get(i).getName(), requestFile);
        }


        //uuid
        String uuidText = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID);
        RequestBody uuid =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), uuidText);

        //uuid
        String nickNameText = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.NICK_NAME);
        RequestBody nickName =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), nickNameText);
        // 添加message
        String messageText = etTitle.getText().toString() + "\n" + etSubTitle.getText().toString();
        RequestBody message =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), messageText);

        Network.getInstance().postSocial(uuid, message, nickName, parts)
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        DebugLog.e("error");
                        Toast.makeText(PublishActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        dismissDialog();
                    }

                    @Override
                    public void onNext(Object o) {

                        setResult(Constant.ACTIVITY_CODE.RESULT_CODE_PUBLISH_SUCCESS);
                        dismissDialog();
                        finish();
                    }
                });
    }


    @Override
    public void onClickAddNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, ArrayList<String> models) {
        choicePhotoWrapper();
    }

    @Override
    public void onClickDeleteNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        mPhotosSnpl.removeItem(position);
    }

    @Override
    public void onClickNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        startActivityForResult(BGAPhotoPickerPreviewActivity.newIntent(this, mPhotosSnpl.getMaxItemCount(), models, models, position, false), Constant.REQUEST_CODE_PHOTO_PREVIEW);
    }
}
