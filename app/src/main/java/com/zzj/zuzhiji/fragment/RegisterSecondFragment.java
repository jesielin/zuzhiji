package com.zzj.zuzhiji.fragment;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.app.App;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.StudioItem;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.GlideCircleTransform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import id.zelory.compressor.Compressor;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

import static android.app.Activity.RESULT_OK;

/**
 * Created by shawn on 2017-03-28.
 */

public class RegisterSecondFragment extends Fragment {

    @BindView(R.id.avator)
    ImageView ivAvator;
    @BindView(R.id.choose_studio)
    Button btnChooseStudio;

    private String gender = "1";

    private MaterialDialog dialog;

    private Compressor compressor = new Compressor.Builder(App.getAppContext())
            .setMaxWidth(Constant.IMAGE_UPLOAD_MAX_WIDTH)
            .setMaxHeight(Constant.IMAGE_UPLOAD_MAX_HEIGHT)
            .setQuality(Constant.IMAGE_UPLOAD_QUALITY)
            .setCompressFormat(Bitmap.CompressFormat.JPEG)
            .setDestinationDirectoryPath(Glide.getPhotoCacheDir(App.getAppContext()).getAbsolutePath())
            .build();


    private ArrayList<String> paths = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(container.getContext(), R.layout.fragment_register_second, null);
        ButterKnife.bind(this, contentView);
        return contentView;
    }

    @OnClick(R.id.choose_studio)
    public void chooseStudio(View view) {

        Network.getInstance().getAllStudio()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        dialog = new MaterialDialog.Builder(getActivity())
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
                        Toast.makeText(getActivity(), "获取工作室错误:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dismissDialog();
                    }

                    @Override
                    public void onNext(List<StudioItem> studioItems) {
                        List<String> items = new ArrayList<String>();
                        for (StudioItem i : studioItems) {
                            items.add(i.title);
                        }

                        dialog.getBuilder()
                                .title("选择工作室")
                                .items(items)
                                .cancelable(true)
                                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        Toast.makeText(getActivity(), which + "", Toast.LENGTH_SHORT).show();
                                        dismissDialog();
                                        return false;
                                    }
                                })
                                .positiveText("确定").show();
                    }
                });

    }

//    @OnClick(R.id.complete)
//    public void complete(View view) {
//
//
//        if (paths.size() == 0) {
//            Toast.makeText(getActivity(), "请选择头像", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//
//
//
//        File avatorFile = new File(paths.get(0));
//
//        dialog = DialogUtils.showProgressDialog(getActivity(), "设置信息", "正在设置，请稍等...");
//        compressor.compressToFileAsObservable(avatorFile)
//                .subscribeOn(Schedulers.computation())
//
//                .subscribe(new Subscriber<File>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                        dismissDialog();
//                    }
//
//                    @Override
//                    public void onNext(File file) {
//
//                        MultipartBody.Part avatorPart = null;
//                        // 创建 RequestBody，用于封装构建RequestBody
//                        RequestBody requestFile =
//                                RequestBody.create(MediaType.parse("multipart/form-data"), file);
//
//                        // MultipartBody.Part  和后端约定好Key，这里的partName是用image
//                        avatorPart =
//                                MultipartBody.Part.createFormData("headSculpture", file.getName(), requestFile);
//
//                        //添加UUID
//                        String uuidText = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID);
//                        RequestBody uuid =
//                                RequestBody.create(
//                                        MediaType.parse("multipart/form-data"), uuidText);
//                        // 添加nickname
//                        String nickNameText = "";
//                        RequestBody nickName =
//                                RequestBody.create(
//                                        MediaType.parse("multipart/form-data"), nickNameText);
//                        // 添加sex
//                        String sexText = gender;
//                        RequestBody sex =
//                                RequestBody.create(
//                                        MediaType.parse("multipart/form-data"), sexText);
//
//                        Network.getInstance().setUserInfo(uuid, nickName, sex, sex,avatorPart)
//                                    .subscribe(new Subscriber<SetInfoResult>() {
//                                        @Override
//                                        public void onCompleted() {
//                                        }
//
//                                        @Override
//                                        public void onError(Throwable e) {
//
//                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                                            dismissDialog();
//
//
//                                        }
//
//                                        @Override
//                                        public void onNext(SetInfoResult setInfoResult) {
//                                            Map<String, String> values = new ArrayMap<String, String>();
//                                            values.put(Constant.SHARED_KEY.AVATOR, setInfoResult.headSculpture);
//                                            values.put(Constant.SHARED_KEY.NICK_NAME, setInfoResult.nickName);
//                                            SharedPreferencesUtils.getInstance().setValues(values);
//                                            dismissDialog();
//
//                                            getActivity().finish();
//
//                                        }
//                                    });
//
//
//                    }
//                });
//
//
//    }


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
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            // 拍照后照片的存放目录，改成你自己拍照后要存放照片的目录。如果不传递该参数的话就没有拍照功能
            File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");

            startActivityForResult(BGAPhotoPickerActivity.newIntent(getActivity(), takePhotoDir, 1, paths, true), Constant.REQUEST_CODE_CHOOSE_PHOTO);
        } else {
            EasyPermissions.requestPermissions(this, "图片选择需要以下权限:\n\n1.访问设备上的照片\n\n2.拍照", Constant.REQUEST_CODE_PERMISSION_PHOTO_PICKER, perms);
        }
    }

    @OnClick({R.id.male, R.id.female})
    public void switchGender(View view) {
        switch (view.getId()) {
            case R.id.male:
                gender = "1";
                break;
            case R.id.female:
                gender = "0";
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DebugLog.e("result:" + requestCode + "," + resultCode);
        if (resultCode == RESULT_OK && requestCode == Constant.REQUEST_CODE_CHOOSE_PHOTO) {
            paths.clear();
            paths.addAll(BGAPhotoPickerActivity.getSelectedImages(data));


            Glide.with(getActivity()).load(paths.get(0))
                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.placeholder_avator)
                    .transform(new GlideCircleTransform(getActivity()))
                    .into(ivAvator);
        }

    }

    @OnClick(R.id.back)
    public void back(View view) {
        getActivity().finish();
    }
}
