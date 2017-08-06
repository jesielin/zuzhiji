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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zzj.zuzhiji.ArticleActivity;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.NewsResult;
import com.zzj.zuzhiji.network.entity.NewsTotal;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by shawn on 2017-08-06.
 */

public class LectureFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

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
    private TrainAdapter trainAdapter = new TrainAdapter();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_train, null);
        ButterKnife.bind(this, contentView);


        setupLayout();
        return contentView;
    }

    private void setupLayout() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(trainAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                DebugLog.d("last visible:" + linearLayoutManager.findLastVisibleItemPosition());
                DebugLog.d("last visible complete:" + linearLayoutManager.findLastCompletelyVisibleItemPosition());
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

    }

    @Override
    public void onRefresh() {

        page = 1;
        Network.getInstance().getNews("1", page)
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
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                trainAdapter.setCanLoadMore(true);
                            else
                                trainAdapter.setCanLoadMore(false);
//                            textNewsAdapter.notifyDataSetChanged();

                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

    }

    private void onLoadMore() {
        DebugLog.e("loading more...");
        page++;
        Network.getInstance().getNews("1", page)
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
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(List<NewsResult> newsResults) {
                        if (newsResults != null) {
                            datas.addAll(newsResults);
                            Collections.sort(datas, comparator);
                            DebugLog.e("data size:" + datas.size());
                            if (page < totalPage)
                                trainAdapter.setCanLoadMore(true);
                            else
                                trainAdapter.setCanLoadMore(false);
//                            textNewsAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
    }

    class TrainVH extends RecyclerView.ViewHolder {

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

        public TrainVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


    }

    class LoadMoreVH extends RecyclerView.ViewHolder {

        public LoadMoreVH(View itemView) {
            super(itemView);
        }
    }


    private class TrainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


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
                return new TrainVH(View.inflate(parent.getContext(), R.layout.item_text_news, null));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


            if (getItemViewType(position) == LOAD_TYPE) {

                onLoadMore();


            } else {
                TrainVH vh = (TrainVH) holder;
                final NewsResult item = datas.get(position);
                Glide.with(getActivity()).load(item.titleImgUrl)
                        .asBitmap().fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)

                        .placeholder(R.color.text_hint)
                        .into(vh.imv);
                vh.tvTitle.setText(item.title);
                vh.tvHot.setText(item.hot);
                vh.tvSubTitle.setText(item.author);
                vh.tvDate.setText(CommonUtils.getDate(Double.valueOf(item.createDate)));

                vh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(ArticleActivity.newIntent(getActivity(), item.title, item.contents));
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
