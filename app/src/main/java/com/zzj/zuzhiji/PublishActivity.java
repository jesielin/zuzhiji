package com.zzj.zuzhiji;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jaeger.ninegridimageview.NineGridImageView;
import com.jaeger.ninegridimageview.NineGridImageViewAdapter;
import com.zzj.zuzhiji.util.ActivityManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 2017-03-30.
 */

public class PublishActivity extends AppCompatActivity {

    @BindView(R.id.title)
    EditText etTitle;
    @BindView(R.id.subtitle)
    EditText etSubTitle;
    @BindView(R.id.image_group)
    NineGridImageView nineGridImageView;

    private List<ImageModel> images = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        ButterKnife.bind(this);
        ActivityManager.getInstance().addActivity(this);

        setupLayout();
    }

    private void setupLayout() {
        nineGridImageView.setAdapter(mImageAdapter);
        images.add(new ImageModel(ImageModel.TYPE_ADD, R.drawable.add_pic));
        images.add(0, new ImageModel(ImageModel.TYPE_IMAGE, R.drawable.add_pic));
        images.add(0, new ImageModel(ImageModel.TYPE_IMAGE, R.drawable.add_pic));
        images.add(0, new ImageModel(ImageModel.TYPE_IMAGE, R.drawable.add_pic));
        images.add(0, new ImageModel(ImageModel.TYPE_IMAGE, R.drawable.add_pic));
        images.add(0, new ImageModel(ImageModel.TYPE_IMAGE, R.drawable.add_pic));
        images.add(0, new ImageModel(ImageModel.TYPE_IMAGE, R.drawable.add_pic));
        images.add(0, new ImageModel(ImageModel.TYPE_IMAGE, R.drawable.add_pic));
        images.add(0, new ImageModel(ImageModel.TYPE_IMAGE, R.drawable.add_pic));
        images.add(0, new ImageModel(ImageModel.TYPE_IMAGE, R.drawable.add_pic));
        nineGridImageView.setImagesData(images);
    }

    @OnClick(R.id.back)
    public void back(View view) {
        ActivityManager.getInstance().finshActivities(this.getClass());
    }

    @OnClick(R.id.complete)
    public void complete(View view) {
        if (TextUtils.isEmpty(etTitle.getText().toString().trim())) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }


    }

    private static class ImageModel {
        public ImageModel(int type, Object src) {
            this.type = type;
            this.src = src;
        }

        public static int TYPE_ADD = 1;
        public static int TYPE_IMAGE = 2;

        public int type;
        public Object src;

    }

    private NineGridImageViewAdapter<ImageModel> mImageAdapter = new NineGridImageViewAdapter<ImageModel>() {
        @Override
        protected void onDisplayImage(Context context, ImageView imageView, ImageModel bean) {

            Glide.with(context).load(bean.src)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.md_blue_grey_400)
                    .into(imageView);
        }

        @Override
        protected ImageView generateImageView(Context context) {
            return super.generateImageView(context);
        }

        @Override
        protected void onItemImageClick(Context context, ImageView imageView, int index, List<ImageModel> list) {
            super.onItemImageClick(context, imageView, index, list);
//            Intent intent = new Intent(mContext,PhotoReviewActivity.class);
//            intent.putExtra("position",index);
//            intent.putExtra("list", list.toArray(new String[0]));
//            context.startActivity(intent);
            ImageModel bean = list.get(index);

            if (bean.type == ImageModel.TYPE_ADD) {
                Toast.makeText(context, index + ":添加图片", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, index + ":正常", Toast.LENGTH_SHORT).show();

            }
        }
    };
}
