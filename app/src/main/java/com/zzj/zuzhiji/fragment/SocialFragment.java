package com.zzj.zuzhiji.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zzj.zuzhiji.CaseDetailActivity;
import com.zzj.zuzhiji.PublishActivity;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.SocialItem;
import com.zzj.zuzhiji.network.entity.SocialTotal;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGANinePhotoLayout;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by shawn on 2017-03-29.
 */

public class SocialFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, BGANinePhotoLayout.Delegate {


    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;


    private SocialAdapter mAdapter = new SocialAdapter();


    private List<SocialItem> datas = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_social, null);
        ButterKnife.bind(this, contentView);
        setupLayout();
        return contentView;
    }

    @OnClick(R.id.publish)
    public void publish() {
        startActivityForResult(new Intent(getActivity(), PublishActivity.class),
                Constant.ACTIVITY_CODE.REQUEST_CODE_SOCIAL_FRAGMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DebugLog.e("fragment on activity result::" + requestCode + "," + resultCode);
        if ((requestCode == Constant.ACTIVITY_CODE.REQUEST_CODE_SOCIAL_FRAGMENT
                && resultCode == Constant.ACTIVITY_CODE.RESULT_CODE_PUBLISH_SUCCESS)
                || (requestCode == Constant.ACTIVITY_CODE.REQUEST_CODE_SOCIAL_TO_DETAIL
                && resultCode == Constant.ACTIVITY_CODE.RESULT_CODE_DETAIL_CHANGE_STATUS_BACK_TO_SOCIAL)) {
            DebugLog.e("fragment on activity result");
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    doRefresh();
                }
            });
        }
    }

    private void setupLayout() {


        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                doRefresh();
            }
        });
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && mAdapter.isCanLoadMore()
                        && linearLayoutManager.findLastCompletelyVisibleItemPosition() == mAdapter.getItemCount() - 1)
                    loadMore();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void doRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
    }


    private void loadMore() {


        Network.getInstance().getSocialItems(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID)
                , mPage, Constant.PAGE_SIZE)
                .observeOn(Schedulers.io())
                .map(new Func1<SocialTotal, List<SocialItem>>() {
                    @Override
                    public List<SocialItem> call(SocialTotal socialTotal) {
                        mTotalPage = socialTotal.total;
                        return socialTotal.list;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<SocialItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onNext(List<SocialItem> socialItems) {
                        if (socialItems != null) {
                            datas.addAll(socialItems);

                            DebugLog.e("page:" + mPage);
                            DebugLog.e("totalPage:" + mTotalPage);
                            if (mPage < mTotalPage) {
                                mAdapter.setCanLoadMore(true);
                                mPage++;

                            } else {
                                mAdapter.setCanLoadMore(false);
                            }


                        }

                    }
                });
    }

    @Override
    public void onRefresh() {
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 500);

        mPage = 1;

        Network.getInstance().getSocialItems(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID)
                , mPage, Constant.PAGE_SIZE)
                .observeOn(Schedulers.io())
                .map(new Func1<SocialTotal, List<SocialItem>>() {
                    @Override
                    public List<SocialItem> call(SocialTotal socialTotal) {
                        mTotalPage = socialTotal.total;
                        return socialTotal.list;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<SocialItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(List<SocialItem> socialItems) {
                        if (socialItems != null) {
                            datas.clear();
                            datas.addAll(socialItems);

                            DebugLog.e("page:" + mPage);
                            DebugLog.e("totalPage:" + mTotalPage);
                            if (mPage < mTotalPage) {
                                mAdapter.setCanLoadMore(true);
                                mPage++;
                            } else {
                                mAdapter.setCanLoadMore(false);
                            }

                        }
                        swipeRefreshLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }, 1500);
                    }
                });

    }


    @Override
    public void onClickNinePhotoItem(BGANinePhotoLayout ninePhotoLayout, View view, int position, String model, List<String> models) {
        photoPreviewWrapper(ninePhotoLayout);
    }

    /**
     * 图片预览，兼容6.0动态权限
     */
    @AfterPermissionGranted(Constant.REQUEST_CODE_PERMISSION_PHOTO_PREVIEW)
    private void photoPreviewWrapper(BGANinePhotoLayout bgaNinePhotoLayout) {
        if (bgaNinePhotoLayout == null) {
            return;
        }

        // 保存图片的目录，改成你自己要保存图片的目录。如果不传递该参数的话就不会显示右上角的保存按钮
        File downloadDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerDownload");

        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            if (bgaNinePhotoLayout.getItemCount() == 1) {
                // 预览单张图片

                startActivity(BGAPhotoPreviewActivity.newIntent(getActivity(), downloadDir, bgaNinePhotoLayout.getCurrentClickItem()));
            } else if (bgaNinePhotoLayout.getItemCount() > 1) {
                // 预览多张图片

                startActivity(BGAPhotoPreviewActivity.newIntent(getActivity(), downloadDir, bgaNinePhotoLayout.getData(), bgaNinePhotoLayout.getCurrentClickItemPosition()));
            }
        } else {
            EasyPermissions.requestPermissions(this, "图片预览需要以下权限:\n\n1.访问设备上的照片", Constant.REQUEST_CODE_PERMISSION_PHOTO_PREVIEW, perms);
        }
    }

    public class SocialVH extends RecyclerView.ViewHolder {

        @BindView(R.id.avator)
        ImageView ivAvator;
        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.subtitle)
        TextView tvSubTitle;
        @BindView(R.id.date)
        TextView tvDate;
        @BindView(R.id.comment_num)
        TextView tvCommentNum;
        @BindView(R.id.image_group)
        BGANinePhotoLayout bgaNinePhotoLayout;
        @BindView(R.id.container)
        View clickAreaView;

        public SocialVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            bgaNinePhotoLayout.setDelegate(SocialFragment.this);
        }
    }


    public class LoadMoreVH extends RecyclerView.ViewHolder {

        @BindView(R.id.bar)
        View barView;
        @BindView(R.id.text)
        TextView tvStatus;

        public LoadMoreVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class HeaderVH extends RecyclerView.ViewHolder {

        public HeaderVH(View itemView) {
            super(itemView);
        }
    }

    private class SocialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_LOAD_MORE = 1;
        private static final int TYPE_NORMAL = 2;
        private static final int TYPE_HEADER = 3;
        private Gson gson;
        private boolean canLoadMore = false;


        SocialAdapter() {
            GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
            gson = gsonBuilder.create();
        }

        public boolean isCanLoadMore() {
            return canLoadMore;
        }

        public void setCanLoadMore(boolean canLoadMore) {
            this.canLoadMore = canLoadMore;
            notifyDataSetChanged();
//            notifyItemInserted(datas.size());
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_NORMAL)
                return new SocialVH(View.inflate(parent.getContext(), R.layout.item_social, null));
            else if (viewType == TYPE_LOAD_MORE)
                return new LoadMoreVH(View.inflate(parent.getContext(), R.layout.item_load_more, null));
            else
                return new HeaderVH(View.inflate(parent.getContext(), R.layout.header_social, null));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            DebugLog.e("bind view holder:_" + position);
            if (getItemViewType(position) == TYPE_NORMAL) {
                SocialVH vh = (SocialVH) holder;
                DebugLog.d("get before");
                final SocialItem item = datas.get(position - 1);
                DebugLog.d("get after");
                vh.bgaNinePhotoLayout.setData(item.photos);
                vh.tvTitle.setText(item.momentUserNickname == null ? item.momentOwner : item.momentUserNickname);
                vh.tvSubTitle.setText(item.message);
                vh.tvDate.setText(CommonUtils.getDate(Double.valueOf(item.createDate)));
                vh.tvCommentNum.setText(item.comments == null ? "0" : String.valueOf(item.comments.size()));
                DebugLog.e("avator address:" + CommonUtils.getAvatorAddress(item.momentOwner));

                CommonUtils.loadAvator(vh.ivAvator, CommonUtils.getAvatorAddress(item.momentOwner), getActivity());

                vh.clickAreaView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(CaseDetailActivity.newIntent(getActivity(), gson.toJson(item)),
                                Constant.ACTIVITY_CODE.REQUEST_CODE_SOCIAL_TO_DETAIL);
                    }
                });
            }
//            else if (getItemViewType(position) == TYPE_LOAD_MORE) {
//                DebugLog.d("on loadmore");
//                loadMore();
//            }
//            else if (getItemViewType(position)== TYPE_LOAD_MORE) {
//             DebugLog.e("on load more");
//                loadMore();
//            }
//            else {
//                if (canLoadMore) {
//                    LoadMoreVH vh = (LoadMoreVH) holder;
//                    vh.tvStatus.setText("正在加载...");
//                    vh.barView.setVisibility(View.VISIBLE);
//                } else {
//                    LoadMoreVH vh = (LoadMoreVH) holder;
//                    vh.tvStatus.setText("最后一条了...");
//                    vh.barView.setVisibility(View.GONE);
//                }
//
//            }
        }


        @Override
        public int getItemCount() {
            if (canLoadMore)
                return datas.size() + 2;
            else
                return datas.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {

            if (position == 0)
                return TYPE_HEADER;
            else if (position == datas.size() + 1)
                return TYPE_LOAD_MORE;
            else
                return TYPE_NORMAL;
        }
    }
}
