package com.zzj.zuzhiji.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.jaeger.ninegridimageview.NineGridImageView;
import com.jaeger.ninegridimageview.NineGridImageViewAdapter;
import com.zzj.zuzhiji.PublishActivity;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.ApiException;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.SocialItem;
import com.zzj.zuzhiji.network.entity.SocialTotal;
import com.zzj.zuzhiji.network.entity.UserInfoResult;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by shawn on 2017-03-29.
 */

public class SocialFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int REQUEST_CODE_PUBLISH = 1;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private SocialAdapter mAdapter = new SocialAdapter();

    private int page = 1;
    private int totalPage = 1;

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
    public void publish(View view) {
        startActivityForResult(new Intent(getActivity(), PublishActivity.class),
                REQUEST_CODE_PUBLISH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PUBLISH && requestCode == PublishActivity.RESULT_CODE_PUBLISH_SUCCESS) {
            doRefresh();
        }
    }

    private void setupLayout() {


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
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


    }

    private void doRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
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

        try {
            Network.getInstance().getSocialItems(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID)
                    , page, Constant.PAGE_SIZE)
                    .observeOn(Schedulers.io())
                    .map(new Func1<SocialTotal, List<SocialItem>>() {
                        @Override
                        public List<SocialItem> call(SocialTotal socialTotal) {
                            totalPage = socialTotal.total;
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
                                datas.clear();
                                datas.addAll(socialItems);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        } catch (ApiException ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private String[] IMG_URL_LIST = {
            "http://img3.imgtn.bdimg.com/it/u=1794894692,1423685501&fm=214&gp=0.jpg",
            "https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-480302.jpg",
            "http://ac-QYgvX1CC.clouddn.com/36f0523ee1888a57.jpg", "http://ac-QYgvX1CC.clouddn.com/07915a0154ac4a64.jpg",
            "http://ac-QYgvX1CC.clouddn.com/9ec4bc44bfaf07ed.jpg", "http://ac-QYgvX1CC.clouddn.com/fa85037f97e8191f.jpg",
            "http://ac-QYgvX1CC.clouddn.com/de13315600ba1cff.jpg", "http://ac-QYgvX1CC.clouddn.com/15c5c50e941ba6b0.jpg",
            "http://ac-QYgvX1CC.clouddn.com/10762c593798466a.jpg", "http://ac-QYgvX1CC.clouddn.com/eaf1c9d55c5f9afd.jpg"


    };

    public class SocialVH extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.subtitle)
        TextView tvSubTitle;
        @BindView(R.id.date)
        TextView tvDate;
        @BindView(R.id.comment_num)
        TextView tvCommentNum;
        @BindView(R.id.image_group)
        NineGridImageView nineGridImageView;

        public SocialVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            nineGridImageView.setAdapter(mImageAdapter);
        }
    }

    private void getUserName(final TextView tv, final String ownerId) {
        try {
            Network.getInstance().getUserInfo(ownerId)
                    .subscribe(new Subscriber<UserInfoResult>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(UserInfoResult userInfoResult) {
                            if (userInfoResult != null)
                                tv.setText(userInfoResult.nickName);
                            else {
                                DebugLog.e("user owner:" + ownerId);
                                //TODO:设置不上
                                tv.setText(String.valueOf(ownerId));
                            }

                        }
                    });
        } catch (ApiException ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private class SocialAdapter extends RecyclerView.Adapter<SocialVH> {

        @Override
        public SocialVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SocialVH(View.inflate(parent.getContext(), R.layout.item_social, null));
        }

        @Override
        public void onBindViewHolder(SocialVH holder, int position) {
            SocialItem item = datas.get(position);
            holder.nineGridImageView.setImagesData(item.photos);
            //TODO:
//            getUserName(holder.tvTitle,item.momentOwner);
            holder.tvTitle.setText(item.momentOwner);
            holder.tvSubTitle.setText(item.message);
            holder.tvDate.setText(CommonUtils.getDate(Double.valueOf(item.createDate)));
            holder.tvCommentNum.setText(item.comments == null ? "0" : String.valueOf(item.comments.size()));
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }

    private NineGridImageViewAdapter<String> mImageAdapter = new NineGridImageViewAdapter<String>() {
        @Override
        protected void onDisplayImage(Context context, ImageView imageView, String s) {
            Glide.with(context).load(s)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.md_blue_grey_400)
                    .into(imageView);
        }

        @Override
        protected ImageView generateImageView(Context context) {
            return super.generateImageView(context);
        }

        @Override
        protected void onItemImageClick(Context context, ImageView imageView, int index, List<String> list) {
            super.onItemImageClick(context, imageView, index, list);
//            Intent intent = new Intent(mContext,PhotoReviewActivity.class);
//            intent.putExtra("position",index);
//            intent.putExtra("list", list.toArray(new String[0]));
//            context.startActivity(intent);

            Toast.makeText(context, "posi:" + index, Toast.LENGTH_SHORT).show();
        }
    };
}
