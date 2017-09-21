package com.zzj.zuzhiji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.NewsResult;
import com.zzj.zuzhiji.network.entity.NewsTotal;
import com.zzj.zuzhiji.util.DebugLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by shawn on 2017/9/7.
 */

public class VideoActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {


    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    Comparator comparator = new Comparator<NewsResult>() {
        @Override
        public int compare(NewsResult newsResult, NewsResult t1) {
            return (int) (Double.valueOf(t1.createDate) - Double.valueOf(newsResult.createDate));
        }
    };

    int page = 1;
    int totalPage = 1;
    private List<NewsResult> datas = new ArrayList<>();
    private VideoAdapter videoAdapter = new VideoAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);
        setupLayout();
    }

    private void setupLayout() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, gridLayoutManager
//                .getOrientation());
//        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(videoAdapter);
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

    @OnClick(R.id.back)
    public void back() {
        onBackPressed();
    }

    @Override
    public void onRefresh() {
        page = 1;
        Network.getInstance().getNews("2", page)
                .observeOn(Schedulers.io())
                .map(new Func1<NewsTotal, List<NewsResult>>() {
                    @Override
                    public List<NewsResult> call(NewsTotal newsTotal) {
                        totalPage = newsTotal.totalPage;

                        return newsTotal.items;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<NewsResult>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(VideoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(List<NewsResult> newsResults) {
                        if (newsResults != null) {
                            datas.clear();
                            datas.addAll(newsResults);
                            Collections.sort(datas, comparator);
                            DebugLog.e("data size:" + datas.size());
                            if (page < totalPage)
                                videoAdapter.setCanLoadMore(true);
                            else
                                videoAdapter.setCanLoadMore(false);
//                            textNewsAdapter.notifyDataSetChanged();

                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void onLoadMore() {
        DebugLog.e("loading more...");
        page++;
        Network.getInstance().getNews("2", page)
                .observeOn(Schedulers.io())
                .map(new Func1<NewsTotal, List<NewsResult>>() {
                    @Override
                    public List<NewsResult> call(NewsTotal newsTotal) {
                        totalPage = newsTotal.totalPage;

                        return newsTotal.items;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<NewsResult>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(VideoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(List<NewsResult> newsResults) {
                        if (newsResults != null) {
                            datas.addAll(newsResults);
                            Collections.sort(datas, comparator);
                            DebugLog.e("data size:" + datas.size());
                            if (page < totalPage)
                                videoAdapter.setCanLoadMore(true);
                            else
                                videoAdapter.setCanLoadMore(false);
//                            textNewsAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
    }


    class VideoVH extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView tvTitle;
//        @BindView(R.id.subtitle)
//        TextView tvSubTitle;
        @BindView(R.id.imv)
        ImageView imv;
//        @BindView(R.id.hot)
//        TextView tvHot;
//        @BindView(R.id.date)
//        TextView tvDate;

        public VideoVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


    }

    class LoadMoreVH extends RecyclerView.ViewHolder {

        public LoadMoreVH(View itemView) {
            super(itemView);
        }
    }


    private class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        int NORMAL_TYPE = 1;
        int LOAD_TYPE = 2;
        boolean canLoadMore = false;

        public void setCanLoadMore(boolean canLoadMore) {
            this.canLoadMore = canLoadMore;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (datas.size() == position && canLoadMore) {
                return LOAD_TYPE;
            } else
                return NORMAL_TYPE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (LOAD_TYPE == viewType)
                return new LoadMoreVH(View.inflate(parent.getContext(), R.layout.item_load_more, null));
            else
                return new VideoVH(View.inflate(parent.getContext(), R.layout.item_video, null));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


            if (getItemViewType(position) == LOAD_TYPE) {

                onLoadMore();


            } else {
                VideoVH vh = (VideoVH) holder;
                final NewsResult item = datas.get(position);
                Glide.with(VideoActivity.this).load(item.img1)
                        .asBitmap().fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)

                        .placeholder(R.color.text_hint)
                        .into(vh.imv);
                vh.tvTitle.setText(item.title);
//                vh.tvHot.setText(item.hot);
//                vh.tvSubTitle.setText(item.author);
//                vh.tvDate.setText(CommonUtils.getDate(Double.valueOf(item.createDate)));

                vh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(VideoDetailActivity.newIntent(VideoActivity.this, item.titleImgUrl, item.title));
//                        startActivity(ArticleActivity.newIntent(VideoActivity.this, item.title, item.contents));
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if (canLoadMore)
                return datas.size() + 1;
            else
                return datas.size();
        }
    }
}
