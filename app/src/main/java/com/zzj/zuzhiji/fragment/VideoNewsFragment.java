package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.VideoPlayActivity;
import com.zzj.zuzhiji.network.entity.NewsResult;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shawn on 17/3/27.
 */

public class VideoNewsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;


    private List<NewsResult> datas = new ArrayList<>();
    private VideoNewsAdapter videoNewsAdapter = new VideoNewsAdapter();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_video_news, null);
        ButterKnife.bind(this, contentView);


        setupLayout();
        return contentView;
    }

    private void setupLayout() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(videoNewsAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });
    }

    @Override
    public void onRefresh() {


//        Network.getInstance().getNews("2")
//                .subscribe(new Subscriber<List<NewsResult>>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                        swipeRefreshLayout.setRefreshing(false);
//                    }
//
//                    @Override
//                    public void onNext(List<NewsResult> newsResults) {
//                        if (newsResults != null) {
//                            datas.clear();
//                            datas.addAll(newsResults);
//                            videoNewsAdapter.notifyDataSetChanged();
//                            swipeRefreshLayout.setRefreshing(false);
//                        }
//                    }
//                });

    }

    class VideoNewsVH extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.subtitle)
        TextView tvSubTitle;
        @BindView(R.id.imv)
        ImageView imv;
        @BindView(R.id.hot)
        TextView tvHot;
        @BindView(R.id.date)
        TextView tvDate;

        public VideoNewsVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


    }


    private class VideoNewsAdapter extends RecyclerView.Adapter<VideoNewsVH> {

        @Override
        public VideoNewsVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VideoNewsVH(View.inflate(parent.getContext(), R.layout.item_video_news, null));
        }

        @Override
        public void onBindViewHolder(VideoNewsVH holder, int position) {

            final NewsResult item = datas.get(position);
            Glide.with(getActivity()).load(item.titleImgUrl)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.text_hint)
                    .into(holder.imv);
            holder.tvTitle.setText(item.title);
            holder.tvHot.setText(item.hot);
            holder.tvDate.setText(CommonUtils.getDate(Double.valueOf(item.createDate)));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DebugLog.e("url:" + item.contents);
                    startActivity(VideoPlayActivity.newIntent(getActivity(), item.contents));
                }
            });

        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
}
