package com.zzj.zuzhiji;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;

/**
 * Created by shawn on 2017-03-28.
 */

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.avator)
    ImageView imvAvator;



    private List<String> selectedPhotos = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }



    @OnClick(R.id.avator)
    public void chooseAvator(View view){
        PhotoPicker.builder()
                .setPhotoCount(1)
                .setPreviewEnabled(false)
                .setShowCamera(false)
                .start(this);
    }



    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {

            List<String> photos = null;
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                Uri uri = Uri.fromFile(new File(photos.get(0)));

                boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(imvAvator.getContext());

                if (canLoadImage) {
                    Glide.with(this)
                            .load(uri)
                            .centerCrop()
                            .thumbnail(0.1f)
                            .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                            .error(R.drawable.__picker_ic_broken_image_black_48dp)
                            .into(imvAvator);
                }
            }

        }
    }
}
