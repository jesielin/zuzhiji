package com.zzj.zuzhiji.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.youth.banner.Banner;
import com.youth.banner.loader.ImageLoader;
import com.zzj.zuzhiji.HomePageActivity;
import com.zzj.zuzhiji.MainActivity;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.ReservationActivity;
import com.zzj.zuzhiji.SearchActivity;
import com.zzj.zuzhiji.TrainActivity;
import com.zzj.zuzhiji.VideoActivity;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.AdvertResult;
import com.zzj.zuzhiji.network.entity.Notice;
import com.zzj.zuzhiji.network.entity.RecommendBean;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by shawn on 2017-03-29.
 */

public class HomeFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.banner)
    Banner banner;

    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    @BindView(R.id.tablayout)
    TabLayout tabLayout;

    @BindView(R.id.notice)
    TextView tvNotice;

    private RecommendTechAdapter mTechAdapter = new RecommendTechAdapter();
    private RecommendStudioAdapter mStudioAdapter = new RecommendStudioAdapter();

    private List<RecommendBean> techs = new ArrayList<>();
    private List<RecommendBean> studios = new ArrayList<>();

    private int tabIndex = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_home, null);
        ButterKnife.bind(this, contentView);


        setupBanner();

        setupRecyclerView();

        setupTab();

        setupNotice();
        return contentView;
    }

    private void setupNotice() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        Network.getInstance().getNotice(str)
                .subscribe(new Subscriber<List<Notice>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {


                    }

                    @Override
                    public void onNext(List<Notice> notices) {

                        if (notices != null && notices.size() > 0) {
                            tvNotice.setText(notices.get(0).content);
                        }
                    }
                });

        //// TODO: 17/4/25 跑马灯效果
//        tvNotice.requestFocus();
    }


    private void setupTab() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tabIndex = 0;
                        recyclerView.setAdapter(mTechAdapter);
                        if (techs.size() == 0)
                            doRefresh();

                        break;
                    case 1:
                        tabIndex = 1;
                        recyclerView.setAdapter(mStudioAdapter);
                        if (studios.size() == 0)
                            doRefresh();

                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @OnClick(R.id.reservation)
    public void reserv(View view) {
        Intent intent = new Intent(getActivity(), ReservationActivity.class);
        intent.putExtra(ReservationActivity.KEY_FROM, ReservationActivity.FROM_HOME);
        startActivity(intent);
    }

    @OnClick(R.id.customer_service)
    public void call(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:01085911987"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @OnClick(R.id.service)
    public void service(View view) {
        startActivity(new Intent(getActivity(), VideoActivity.class));
    }

    @OnClick(R.id.video)
    public void video(View view) {

        startActivity(new Intent(getActivity(), TrainActivity.class));
//        Toast.makeText(getActivity(), "该模块正在开发中..", Toast.LENGTH_SHORT).show();
//        MainActivity activity = (MainActivity) getActivity();
//
//        SharedPreferencesUtils.getInstance().setValue(Constant.SHARED_KEY.NEWS_TAB_INDEX, Constant.SHARED_VALUES.NEWS_VIDEO_TAB_INDEX);
//        activity.switchFragment(2);
    }

    @OnClick(R.id.news)
    public void news(View view) {
        MainActivity activity = (MainActivity) getActivity();

        SharedPreferencesUtils.getInstance().setValue(Constant.SHARED_KEY.NEWS_TAB_INDEX, Constant.SHARED_VALUES.NEWS_TEXT_TAB_INDEX);
        activity.switchFragment(2);
    }

    private void doRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
    }

    private void setupRecyclerView() {

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                doRefresh();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

    }

    private void setupBanner() {
//        List<Integer> integers = Arrays.asList(R.drawable.placeholder_advert1, R.drawable.placeholder_advert2);
        List<String> integers = Arrays.asList("http://101.201.155.115:3113/information/word/apptop1.jpg", "http://101.201.155.115:3113/information/word/apptop1.jpg");

        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
//        banner.setImages(integers);

        banner.setDelayTime(3000);

        //banner设置方法全部调用完毕时最后调用
//        banner.start();
    }

    @OnClick(R.id.search)
    public void search(View view) {
        startActivity(new Intent(getActivity(), SearchActivity.class));
    }

    @Override
    public void onRefresh() {

        Network.getInstance().getAdvert("1")
                .subscribe(new Subscriber<List<AdvertResult>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<AdvertResult> advertResults) {

                        List<String> banners = new ArrayList<String>();
                        for (AdvertResult result : advertResults) {
                            banners.add(result.imgurl);
                        }

                        banner.setImages(banners);
                        banner.start();
                    }
                });

        switch (tabIndex) {
            case 0:
                Network.getInstance().getRecommendTech(Constant.PAGE_SIZE, "1")
                        .subscribe(new Subscriber<List<RecommendBean>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                swipeRefreshLayout.setRefreshing(false);
                            }

                            @Override
                            public void onNext(List<RecommendBean> recommendBeen) {
                                if (recommendBeen != null && recommendBeen.size() > 0) {
                                    HomeFragment.this.techs.clear();
                                    HomeFragment.this.techs.addAll(recommendBeen);
                                    recyclerView.setAdapter(mTechAdapter);

                                }
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });

                break;
            case 1:
                Network.getInstance().getRecommendTech(Constant.PAGE_SIZE, "0")
                        .subscribe(new Subscriber<List<RecommendBean>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                swipeRefreshLayout.setRefreshing(false);
                            }

                            @Override
                            public void onNext(List<RecommendBean> recommendBeen) {
                                if (recommendBeen != null && recommendBeen.size() > 0) {
                                    HomeFragment.this.studios.clear();
                                    HomeFragment.this.studios.addAll(recommendBeen);
                                    recyclerView.setAdapter(mStudioAdapter);

                                }
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                break;

        }

    }

    public class RecommendTechVH extends RecyclerView.ViewHolder {
        @BindView(R.id.avator)
        ImageView ivAvator;
        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.subtitle)
        TextView tvSubTitle;
        @BindView(R.id.clickArea)
        View clickArea;

        public RecommendTechVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class RecommendStudioVH extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.subtitle)
        TextView tvSubtitle;
        @BindView(R.id.avator)
        ImageView ivAvator;

        public RecommendStudioVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    private class RecommendStudioAdapter extends RecyclerView.Adapter<RecommendStudioVH> {


        @Override
        public RecommendStudioVH onCreateViewHolder(ViewGroup parent, int viewType) {

            return new RecommendStudioVH(View.inflate(parent.getContext(), R.layout.item_studio, null));
        }

        @Override
        public void onBindViewHolder(RecommendStudioVH holder, final int position) {
            RecommendBean studioItem = studios.get(position);
            RecommendStudioVH recommendStudioVH = holder;
            recommendStudioVH.tvTitle.setText(studioItem.nickName);
            CommonUtils.loadAvator(holder.ivAvator, studioItem.headSculpture, getActivity());
        }

        @Override
        public int getItemCount() {
            return studios.size();
        }
    }

    private class RecommendTechAdapter extends RecyclerView.Adapter<RecommendTechVH> {


        @Override
        public RecommendTechVH onCreateViewHolder(ViewGroup parent, int viewType) {

            return new RecommendTechVH(View.inflate(parent.getContext(), R.layout.item_search, null));
        }

        @Override
        public void onBindViewHolder(RecommendTechVH holder, final int position) {

            final RecommendBean item = techs.get(position);
            RecommendTechVH recommendTechVH = holder;
            recommendTechVH.tvTitle.setText(TextUtils.isEmpty(item.nickName) ? item.id : item.nickName);
            if (TextUtils.isEmpty(item.summary))
                recommendTechVH.tvSubTitle.setVisibility(View.GONE);
            else
                recommendTechVH.tvSubTitle.setText(item.summary);


            CommonUtils.loadAvator(recommendTechVH.ivAvator, item.headSculpture, getActivity());
            recommendTechVH.clickArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(HomePageActivity.newIntent(getActivity(),
                            item.headSculpture,
                            item.nickName,
                            item.summary,
                            item.userType,
                            item.uuid
                    ), Constant.ACTIVITY_CODE.REQUEST_CODE_HOME_TO_HOME_PAGE);
                }
            });

//                case TYPE_STUDIO:
//                    StudioItem studioItem = studios.get(position);
//                    RecommendStudioVH recommendStudioVH = (RecommendStudioVH) holder;
//                    recommendStudioVH.tvTitle.setText(studioItem.title);
//                    break;
        }

        @Override
        public int getItemCount() {
            return techs.size();
        }
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
