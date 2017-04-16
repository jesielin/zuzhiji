package com.zzj.zuzhiji.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.zzj.zuzhiji.ChatActivity;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.MessageResult;
import com.zzj.zuzhiji.test.ECMainActivity;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.GlideCircleTransform;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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




        setupLayout();

        DebugLog.e("my uuid:"+SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID));


        // 判断sdk是否登录成功过，并没有退出和被踢，否则跳转到登陆界面
        if (!EMClient.getInstance().isLoggedInBefore()) {
            DebugLog.e("未登录");
            signIn();
        }else {
            DebugLog.e("已登录");
        }




        return contentView;
    }




    // 弹出框
    private ProgressDialog mDialog;
    /**
     * 登录方法
     */
    private void signIn() {
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage("正在登陆，请稍后...");
        mDialog.show();
        String username = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID);
        String password = "123456";
        
        EMClient.getInstance().login(username, password, new EMCallBack() {
            /**
             * 登陆成功的回调
             */
            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();

                        // 加载所有会话到内存
                        EMClient.getInstance().chatManager().loadAllConversations();
                        // 加载所有群组到内存，如果使用了群组的话
                        // EMClient.getInstance().groupManager().loadAllGroups();

                        // 登录成功跳转界面
                        DebugLog.e("登录成功");
                    }
                });
            }

            /**
             * 登陆错误的回调
             * @param i
             * @param s
             */
            @Override
            public void onError(final int i, final String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        Log.d("lzan13", "登录失败 Error code:" + i + ", message:" + s);
                        /**
                         * 关于错误码可以参考官方api详细说明
                         * http://www.easemob.com/apidoc/android/chat3.0/classcom_1_1hyphenate_1_1_e_m_error.html
                         */
                        switch (i) {
                            // 网络异常 2
                            case EMError.NETWORK_ERROR:
                                Toast.makeText(getActivity(), "网络错误 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无效的用户名 101
                            case EMError.INVALID_USER_NAME:
                                Toast.makeText(getActivity(), "无效的用户名 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无效的密码 102
                            case EMError.INVALID_PASSWORD:
                                Toast.makeText(getActivity(), "无效的密码 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 用户认证失败，用户名或密码错误 202
                            case EMError.USER_AUTHENTICATION_FAILED:
                                Toast.makeText(getActivity(), "用户认证失败，用户名或密码错误 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 用户不存在 204
                            case EMError.USER_NOT_FOUND:
                                Toast.makeText(getActivity(), "用户不存在 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无法访问到服务器 300
                            case EMError.SERVER_NOT_REACHABLE:
                                Toast.makeText(getActivity(), "无法访问到服务器 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 等待服务器响应超时 301
                            case EMError.SERVER_TIMEOUT:
                                Toast.makeText(getActivity(), "等待服务器响应超时 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 服务器繁忙 302
                            case EMError.SERVER_BUSY:
                                Toast.makeText(getActivity(), "服务器繁忙 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 未知 Server 异常 303 一般断网会出现这个错误
                            case EMError.SERVER_UNKNOWN_ERROR:
                                Toast.makeText(getActivity(), "未知的服务器异常 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(getActivity(), "ml_sign_in_failed code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.ACTIVITY_CODE.REQUEST_CODE_MESSAGE_TO_CHAT && resultCode == Constant.ACTIVITY_CODE.RESULT_CODE_CHAT_BACK_TO_MESSAGE)
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                    onRefresh();
                }
            });
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



    public class MessageVH extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.avator)
        ImageView ivAvator;
        @BindView(R.id.subtitle)
        TextView tvSubTitle;
        @BindView(R.id.date)
        TextView tvDate;
        @BindView(R.id.unreadnum)
        TextView tvUnreadNum;

        @BindView(R.id.unread_area)
        View vUnreadArea;
        @BindView(R.id.clickArea)
        View vClickArea;
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
            final MessageResult item = datas.get(position);
            holder.tvTitle.setText(item.nickName);
            holder.vClickArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (EMClient.getInstance().isLoggedInBefore()) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("UUID", item.uuid);
                        intent.putExtra("TITLE",item.nickName);
                        startActivityForResult(intent,Constant.ACTIVITY_CODE.REQUEST_CODE_MESSAGE_TO_CHAT);
                    }
                }
            });

            Glide.with(getActivity())
                    .load(CommonUtils.getAvatorAddress(item.uuid))
                    .asBitmap()
                    .centerCrop()
//                    .placeholder(R.color.text_hint)
                    .error(R.color.avator_place_holder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
//                    .priority(Priority.IMMEDIATE)


                    .into(holder.ivAvator);


            EMConversation conversation = EMClient.getInstance().chatManager().getConversation(item.uuid, null, true);
            int unreadMsgCount = conversation.getUnreadMsgCount();
            if (unreadMsgCount == 0){
                holder.vUnreadArea.setVisibility(View.GONE);
            }else {
                holder.vUnreadArea.setVisibility(View.VISIBLE);
                holder.tvUnreadNum.setText(String.valueOf(unreadMsgCount));
            }
            EMMessage lastMessage = conversation.getLastMessage();

            if (lastMessage == null || lastMessage.getBody() == null || TextUtils.isEmpty(lastMessage.getBody().toString())){
                holder.tvSubTitle.setText("");
            }
            else  {
                EMTextMessageBody body = (EMTextMessageBody) lastMessage.getBody();
                holder.tvSubTitle.setText(body.getMessage());
            }


            if (lastMessage == null)
                holder.tvDate.setText("");
            else {
                long msgTime = lastMessage.getMsgTime();
                holder.tvDate.setText(CommonUtils.getDate(msgTime));
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
}
