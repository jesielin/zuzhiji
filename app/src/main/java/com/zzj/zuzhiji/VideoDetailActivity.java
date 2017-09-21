package com.zzj.zuzhiji;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 2017/9/20.
 */

public class VideoDetailActivity extends BaseActivity {


    @BindView(R.id.img)
    ImageView imageView;

    @BindView(R.id.title)
    TextView tvTitle;
    private String imgUrl;
    private String title;

    public static Intent newIntent(Context context, String imgUrl, String title) {
        Intent intent = new Intent(context, VideoDetailActivity.class);
        intent.putExtra("IMG", imgUrl);
        intent.putExtra("TITLE", title);
        return intent;

    }

    public void resolveIntent() {
        Intent intent = getIntent();
        imgUrl = intent.getStringExtra("IMG");
        title = intent.getStringExtra("TITLE");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        ButterKnife.bind(this);
        resolveIntent();
        setupLayout();
    }

    private void setupLayout() {
        Glide.with(VideoDetailActivity.this).load(imgUrl)

                .diskCacheStrategy(DiskCacheStrategy.ALL)

                .placeholder(R.color.text_hint)
                .into(imageView);

        tvTitle.setText(title);
    }

    @OnClick(R.id.back)
    public void back() {
        onBackPressed();
    }
}
