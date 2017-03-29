package com.zzj.zuzhiji.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.youth.banner.Banner;
import com.youth.banner.loader.ImageLoader;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.SearchActivity;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.ApiException;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.Tech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by shawn on 2017-03-29.
 */

public class HomeFragment extends Fragment {

    @BindView(R.id.banner)
    Banner banner;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    @BindView(R.id.tablayout)
    TabLayout tabLayout;

    private RecommendAdapter mAdapter = new RecommendAdapter();

    private List<Tech> datas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_home, null);
        ButterKnife.bind(this, contentView);


        setupBanner();

        setupRecyclerView();

        setupTab();
        return contentView;
    }

    private void setupTab() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(getActivity(), tab.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {


            Network.getInstance().getRecommendTech(Constant.PAGE_SIZE)
                    .subscribe(new Subscriber<List<Tech>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(List<Tech> teches) {

                        }
                    });
        } catch (ApiException ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(mAdapter);
    }

    public class RecommendVH extends RecyclerView.ViewHolder {
        @BindView(R.id.avator)
        ImageView ivAvator;
        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.subtitle)
        TextView tvSubTitle;

        public RecommendVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class RecommendAdapter extends RecyclerView.Adapter<RecommendVH> {

        @Override
        public RecommendVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecommendVH(View.inflate(parent.getContext(), R.layout.item_recommend, null));
        }

        @Override
        public void onBindViewHolder(RecommendVH holder, int position) {
            holder.tvTitle.setText(datas.get(position).id);
            holder.tvSubTitle.setText(datas.get(position).summary);
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
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
