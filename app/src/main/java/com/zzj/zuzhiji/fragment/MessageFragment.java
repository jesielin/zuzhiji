package com.zzj.zuzhiji.fragment;

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
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.zzj.zuzhiji.ChatActivity;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.MessageResult;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;

/**
 * Created by shawn on 2017-03-29.
 */

public class MessageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private List<MessageResult> datas = new ArrayList<>();
    private MessageAdapter mAdapter = new MessageAdapter();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_message, null);
        ButterKnife.bind(this, contentView);
        loginEm();
        setupLayout();
        return contentView;
    }

    private void setupLayout() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(mAdapter);
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

        String uuid = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID);

            Network.getInstance().getMyFriendship(uuid)
                    .subscribe(new Subscriber<List<MessageResult>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onNext(List<MessageResult> messageResults) {
                            if (messageResults != null) {
                                datas.clear();
                                datas.addAll(messageResults);
                                mAdapter.notifyDataSetChanged();
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    });

    }

    private void loginEm() {
        DebugLog.e("uuid:" + SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID));
        EMClient.getInstance().login(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID), "123456", new EMCallBack() {//回调
            @Override
            public void onSuccess() {
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();
                DebugLog.d("登录聊天服务器成功！");
                SharedPreferencesUtils.getInstance().setEmLogin(true);
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                DebugLog.d("登录聊天服务器失败！");
                SharedPreferencesUtils.getInstance().setEmLogin(false);
            }
        });
    }

    public class MessageVH extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView tvTitle;

        public MessageVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageVH> {

        @Override
        public MessageVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MessageVH(View.inflate(parent.getContext(), R.layout.item_message, null));
        }

        @Override
        public void onBindViewHolder(MessageVH holder, final int position) {
            holder.tvTitle.setText(datas.get(position).nickName);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!SharedPreferencesUtils.getInstance().isEmLogin()) {
                        Toast.makeText(getActivity(), "账户错误！", Toast.LENGTH_SHORT).show();
                        loginEm();
                        return;
                    }
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("UUID", datas.get(position).uuid);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
}
