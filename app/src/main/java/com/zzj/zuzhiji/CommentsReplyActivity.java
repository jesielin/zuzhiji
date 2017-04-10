package com.zzj.zuzhiji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.ReplyItem;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;

/**
 * Created by shawn on 17/4/10.
 */

public class CommentsReplyActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.list)
    RecyclerView recyclerView;

    private List<ReplyItem> datas = new ArrayList<>();
    private CommentsAdapter mAdapter = new CommentsAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_reply);
        ButterKnife.bind(this);

        setupLayout();

    }

    private void setupLayout() {


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        recyclerView.setAdapter(mAdapter);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                doRefresh();
            }
        });
    }

    private void doRefresh() {
        refreshLayout.setRefreshing(true);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        getData();
    }

    public class CommentsVH extends RecyclerView.ViewHolder {

        @BindView(R.id.avator)
        CircleImageView ivAvator;
        @BindView(R.id.commenter_name)
        TextView tvCommenterName;
        @BindView(R.id.text_huifu)
        TextView tvTextHuiFu;
        @BindView(R.id.friend_name)
        TextView tvFriendName;
        @BindView(R.id.subtitle)
        TextView tvSubTitle;
        @BindView(R.id.container)
        View clickAreaView;

        public CommentsVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class CommentsAdapter extends RecyclerView.Adapter<CommentsVH> {

        @Override
        public CommentsVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CommentsVH(View.inflate(parent.getContext(), R.layout.item_comment, null));
        }

        @Override
        public void onBindViewHolder(CommentsVH holder, int position) {
            ReplyItem item = datas.get(position);
            if (TextUtils.isEmpty(item.targetCommentUUID)) {
                holder.tvTextHuiFu.setVisibility(View.GONE);
                holder.tvFriendName.setVisibility(View.GONE);
            } else {
                holder.tvTextHuiFu.setVisibility(View.VISIBLE);
                holder.tvFriendName.setVisibility(View.VISIBLE);
                holder.tvFriendName.setText(item.targetCommentNickname);
            }

            holder.tvCommenterName.setText(item.commenterNickname);
            holder.tvSubTitle.setText(item.message);
            Glide.with(CommentsReplyActivity.this)
                    .load(CommonUtils.getAvatorAddress(item.commenterUUID))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.ivAvator);
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }

    private void getData() {
        Network.getInstance().queryMyReplyComments(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID))
                .subscribe(
                        new Subscriber<List<List<ReplyItem>>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(CommentsReplyActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                refreshLayout.setRefreshing(false);
                            }

                            @Override
                            public void onNext(List<List<ReplyItem>> lists) {

                                if (lists != null && lists.size() > 0) {
                                    for (List<ReplyItem> l : lists) {
                                        if (l != null && l.size() > 0) {
                                            datas.clear();
                                            datas.addAll(l);
                                            mAdapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                }

                                refreshLayout.setRefreshing(false);
                            }
                        }
                );
    }

    @OnClick(R.id.back)
    public void back(View view) {
        onBackPressed();
    }
}
