package com.zzj.zuzhiji.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.app.App;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.ApiException;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.SetInfoResult;
import com.zzj.zuzhiji.util.ActivityManager;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.GlideCircleTransform;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import me.shaohui.advancedluban.Luban;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Subscriber;

/**
 * Created by shawn on 2017-03-28.
 */

public class RegisterSecondFragment extends Fragment {

    @BindView(R.id.avator)
    ImageView ivAvator;
    @BindView(R.id.nickname)
    EditText etNickName;

    private String gender = "1";


    private ArrayList<String> paths = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(container.getContext(), R.layout.fragment_register_second, null);
        ButterKnife.bind(this, contentView);
        return contentView;
    }


    //TODO:压缩图片
    @OnClick(R.id.complete)
    public void complete(View view) {
        if (TextUtils.isEmpty(etNickName.getText().toString().trim())) {
            Toast.makeText(getActivity(), "请输入昵称", Toast.LENGTH_SHORT).show();
            return;
        }

        if (paths.size() == 0) {
            Toast.makeText(getActivity(), "请选择头像", Toast.LENGTH_SHORT).show();
            return;
        }

        final MaterialDialog dialog = DialogUtils.showProgressDialog(getActivity(), "设置信息", "正在设置，请稍等...");


        File avatorFile = new File(paths.get(0));
        //TODO:
        Luban.compress(App.getContext(), avatorFile)
                .putGear(Luban.CUSTOM_GEAR)
                .asObservable()
                .subscribe(new Subscriber<File>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(File file) {
                        Log.i("TAG", file.getAbsolutePath());

                        MultipartBody.Part avatorPart = null;
                        // 创建 RequestBody，用于封装构建RequestBody
                        RequestBody requestFile =
                                RequestBody.create(MediaType.parse("multipart/form-data"), file);

                        // MultipartBody.Part  和后端约定好Key，这里的partName是用image
                        avatorPart =
                                MultipartBody.Part.createFormData("headSculpture", file.getName(), requestFile);

                        //添加UUID
                        String uuidText = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID);
                        RequestBody uuid =
                                RequestBody.create(
                                        MediaType.parse("multipart/form-data"), uuidText);
                        // 添加nickname
                        String nickNameText = etNickName.getText().toString().trim();
                        RequestBody nickName =
                                RequestBody.create(
                                        MediaType.parse("multipart/form-data"), nickNameText);
                        // 添加sex
                        String sexText = gender;
                        RequestBody sex =
                                RequestBody.create(
                                        MediaType.parse("multipart/form-data"), sexText);

                        try {
                            Network.getInstance().setUserInfo(uuid, nickName, sex, avatorPart)
                                    .subscribe(new Subscriber<SetInfoResult>() {
                                        @Override
                                        public void onCompleted() {
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            DebugLog.e("message:" + e.getMessage());
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }

                                        @Override
                                        public void onNext(SetInfoResult setInfoResult) {
                                            Map<String, String> values = new ArrayMap<String, String>();
                                            values.put(Constant.SHARED_KEY.AVATOR, setInfoResult.headSculpture);
                                            values.put(Constant.SHARED_KEY.NICK_NAME, setInfoResult.nickName);
                                            SharedPreferencesUtils.getInstance().setValues(values);
                                            dialog.dismiss();
                                            getActivity().finish();
                                        }
                                    });
                        } catch (ApiException ex) {
                            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });




    }


    @OnClick(R.id.avator)
    public void chooseAvator(View view) {
        FilePickerBuilder.getInstance().setMaxCount(1)
                .setSelectedFiles(paths)
                .setActivityTheme(R.style.AppTheme_NoActionBar)
                .pickPhoto(this);

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

        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    paths.clear();
                    paths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));

                    Glide.with(getActivity()).load(paths.get(0))
                            .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.placeholder_avator)
                            .transform(new GlideCircleTransform(getActivity()))
                            .into(ivAvator);
                }
                break;
        }

    }

    @OnClick(R.id.back)
    public void back(View view) {
        ActivityManager.getInstance().finshActivities(getActivity().getClass());
    }
}
