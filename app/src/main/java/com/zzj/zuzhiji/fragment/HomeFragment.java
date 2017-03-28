package com.zzj.zuzhiji.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.youth.banner.Banner;
import com.youth.banner.loader.ImageLoader;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.SearchActivity;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 2017-03-29.
 */

public class HomeFragment extends Fragment {

    @BindView(R.id.banner)
    Banner banner;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_home, null);
        ButterKnife.bind(this, contentView);


        setupBanner();


        return contentView;
    }

    private void setupBanner() {
        List<Integer> integers = Arrays.asList(R.drawable.placeholder_advert1, R.drawable.placeholder_advert2);

        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(integers);

        banner.setDelayTime(3000);

        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }

    @OnClick(R.id.search)
    public void search(View view) {
        startActivity(new Intent(getActivity(), SearchActivity.class));
    }

    private class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            /**
             注意：
             1.图片加载器由自己选择，这里不限制，只是提供几种使用方法
             2.返回的图片路径为Object类型，由于不能确定你到底使用的那种图片加载器，
             传输的到的是什么格式，那么这种就使用Object接收和返回，你只需要强转成你传输的类型就行，
             切记不要胡乱强转！
             */


            //Glide 加载图片简单用法
            Glide.with(context).load(path).into(imageView);

        }

    }




}
