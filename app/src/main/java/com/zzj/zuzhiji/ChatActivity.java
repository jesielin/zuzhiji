package com.zzj.zuzhiji;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.KeyboardControlMnanager;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;
import com.zzj.zuzhiji.util.UIHelper;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by shawn on 2017-04-02.
 */

public class ChatActivity extends BaseActivity implements EMMessageListener {


    public static final int TYPE_FROM = 1;
    public static final int TYPE_TO = 2;

    // 显示内容的 TextView
//    private TextView mContentText;
    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.list)
    ListView listView;
    @BindView(R.id.ec_layout_input)
    View bottomChat;
    // 聊天信息输入框
    private EditText mInputEdit;
    // 发送按钮
    private Button mSendBtn;
    //    @BindView(R.id.scroll_container)
//    ScrollView scrollView;
    // 消息监听器
    private EMMessageListener mMessageListener;
    // 当前聊天的 ID
    private String mChatId;
    // 当前会话对象
    private EMConversation mConversation;
    private List<EMMessage> messages = new LinkedList<>();
    private ChatMessageAdapter mAdapter = new ChatMessageAdapter();
    /**
     * 自定义实现Handler，主要用于刷新UI操作
     */
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    EMMessage message = (EMMessage) msg.obj;
                    // 这里只是简单的demo，也只是测试文字消息的收发，所以直接将body转为EMTextMessageBody去获取内容
                    EMTextMessageBody body = (EMTextMessageBody) message.getBody();
                    // 将新的消息内容和时间加入到下边

//                    DebugLog.e("list:"+mContentText.getText() + "\n接收：" + body.getMessage() + " - time: " + message.getMsgTime());
//                    mContentText.setText(mContentText.getText() + "\n接收：" + body.getMessage() + " - time: " + message.getMsgTime());

                    messages.add(message);
                    mAdapter.notifyDataSetChanged();


                    break;
            }
        }
    };
    private String title;
    private String mAvatorUrl;
    private String friendAvatorUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        // 获取当前会话的username(如果是群聊就是群id)
        mChatId = getIntent().getStringExtra("UUID");
        title = getIntent().getStringExtra("TITLE");
        friendAvatorUrl = getIntent().getStringExtra("AVATOR");
        tvTitle.setText(title);

        mAvatorUrl = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.AVATOR);

        mMessageListener = this;
        DebugLog.e("chat uuid:" + mChatId);

        initView();
        initConversation();
    }

    /**
     * 初始化界面
     */
    private void initView() {

//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(mAdapter);

        listView.setAdapter(mAdapter);

        mInputEdit = (EditText) findViewById(R.id.ec_edit_message_input);
        mSendBtn = (Button) findViewById(R.id.ec_btn_send);

        // 设置发送按钮的点击事件
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mInputEdit.getText().toString().trim();
                if (!TextUtils.isEmpty(content)) {
                    mInputEdit.setText("");
                    // 创建一条新消息，第一个参数为消息内容，第二个为接受者username
                    EMMessage message = EMMessage.createTxtSendMessage(content, mChatId);
                    // 将新的消息内容和时间加入到下边
//                    mContentText.setText(mContentText.getText() + "\n发送：" + content + " - time: " + message.getMsgTime());
                    messages.add(message);

                    mAdapter.notifyDataSetChanged();


                    // 调用发送消息的方法
                    EMClient.getInstance().chatManager().sendMessage(message);
                    // 为消息设置回调
                    message.setMessageStatusCallback(new EMCallBack() {
                        @Override
                        public void onSuccess() {
                            // 消息发送成功，打印下日志，正常操作应该去刷新ui
                            Log.i("lzan13", "send message on success");
                        }

                        @Override
                        public void onError(int i, String s) {
                            // 消息发送失败，打印下失败的信息，正常操作应该去刷新ui
                            Log.i("lzan13", "send message on error " + i + " - " + s);
                        }

                        @Override
                        public void onProgress(int i, String s) {
                            // 消息发送进度，一般只有在发送图片和文件等消息才会有回调，txt不回调
                        }
                    });
                }
            }
        });


        KeyboardControlMnanager.observerKeyboardVisibleChange(this, new KeyboardControlMnanager.OnKeyboardStateChangeListener() {
            @Override
            public void onKeyboardChange(int displayHeight, int statusbarHeight, boolean isVisible) {

                int[] contentLocation = new int[2];
                int[] chatLocation = new int[2];
                int itemHeight;


                listView.getLocationInWindow(contentLocation);
                bottomChat.getLocationInWindow(chatLocation);
                itemHeight = listView.getMeasuredHeight();


                listView.scrollBy(0, contentLocation[1] + itemHeight - chatLocation[1]);
            }
        });


        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (UIHelper.isShowSoftInput(ChatActivity.this))
                        UIHelper.hideSoftInput(ChatActivity.this, mInputEdit);

                }
                return false;
            }
        });
    }

    @OnClick(R.id.back)
    public void back(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        setResult(Constant.ACTIVITY_CODE.RESULT_CODE_CHAT_BACK_TO_MESSAGE);
        finish();
    }

    /**
     * 初始化会话对象，并且根据需要加载更多消息
     */
    private void initConversation() {

        /**
         * 初始化会话对象，这里有三个参数么，
         * 第一个表示会话的当前聊天的 useranme 或者 groupid
         * 第二个是绘画类型可以为空
         * 第三个表示如果会话不存在是否创建
         */
        mConversation = EMClient.getInstance().chatManager().getConversation(mChatId, null, true);


        int now_count = mConversation.getAllMessages().size();
        int unread_count = mConversation.getUnreadMsgCount();

        if (now_count < unread_count)
            mConversation.loadMoreMsgFromDB(mConversation.getAllMessages().get(0).getMsgId(), unread_count - now_count);

        messages.addAll(mConversation.getAllMessages());
        mAdapter.notifyDataSetChanged();


        // 设置当前会话未读数为 0
        mConversation.markAllMessagesAsRead();
//        mConversation.loadMoreMsgFromDB()
//        int count = mConversation.getAllMessages().size();
//        if (count < mConversation.getAllMsgCount() && count < 20) {
//            // 获取已经在列表中的最上边的一条消息id
//            String msgId = mConversation.getAllMessages().get(0).getMsgId();
//            // 分页加载更多消息，需要传递已经加载的消息的最上边一条消息的id，以及需要加载的消息的条数
//            mConversation.loadMoreMsgFromDB(msgId, 20 - count);
//        }
//        // 打开聊天界面获取最后一条消息内容并显示
//        if (mConversation.getAllMessages().size() > 0) {
//            EMMessage messge = mConversation.getLastMessage();
//
//            EMTextMessageBody body = (EMTextMessageBody) messge.getBody();
//            // 将消息内容和时间显示出来
//
////            mContentText.setText("聊天记录：" + body.getMessage() + " - time: " + mConversation.getLastMessage().getMsgTime());
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 添加消息监听
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
        DebugLog.e("设置消息监听");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 移除消息监听
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
        DebugLog.e("移除消息监听");
    }

    /**
     * 收到新消息
     *
     * @param list 收到的新消息集合
     */
    @Override
    public void onMessageReceived(List<EMMessage> list) {
        DebugLog.e("收到消息");
        // 循环遍历当前收到的消息
        for (EMMessage message : list) {
            DebugLog.e("收到消息：" + message.getBody());
            DebugLog.e("message from:" + message.getFrom());
            DebugLog.e("message to:" + mChatId);

            if (message.getFrom().equals(mChatId.toLowerCase())) {
                // 设置消息为已读
                mConversation.markMessageAsRead(message.getMsgId());

                DebugLog.e("from:" + message.getFrom());

                // 因为消息监听回调这里是非ui线程，所以要用handler去更新ui
                Message msg = mHandler.obtainMessage();
                msg.what = 0;
                msg.obj = message;
                mHandler.sendMessage(msg);
            } else {
                // 如果消息不是当前会话的消息发送通知栏通知
            }
        }
    }

    /**
     * 收到新的 CMD 消息
     *
     * @param list
     */
    @Override
    public void onCmdMessageReceived(List<EMMessage> list) {
        DebugLog.e("透传消息");
        for (int i = 0; i < list.size(); i++) {
            // 透传消息
            EMMessage cmdMessage = list.get(i);
            EMCmdMessageBody body = (EMCmdMessageBody) cmdMessage.getBody();
            DebugLog.e("透传消息：" + body.action());
        }
    }


    /**
     * --------------------------------- Message Listener -------------------------------------
     * 环信消息监听主要方法
     */

    /**
     * 收到新的已读回执
     *
     * @param list 收到消息已读回执
     */
    @Override
    public void onMessageRead(List<EMMessage> list) {

    }

    /**
     * 收到新的发送回执
     * TODO 无效 暂时有bug
     *
     * @param list 收到发送回执的消息集合
     */
    @Override
    public void onMessageDelivered(List<EMMessage> list) {

    }

    /**
     * 消息的状态改变
     *
     * @param message 发生改变的消息
     * @param object  包含改变的消息
     */
    @Override
    public void onMessageChanged(EMMessage message, Object object) {
    }

    public class ChatMessageVH {

        @BindView(R.id.avator_from)
        ImageView ivAvatorFrom;
        @BindView(R.id.message_from)
        TextView tvMessageFrom;
        @BindView(R.id.from_area)
        View vAreaFrom;

        @BindView(R.id.avator_to)
        ImageView ivAvatorTo;
        @BindView(R.id.message_to)
        TextView tvMessageTo;
        @BindView(R.id.to_area)
        View vAreaTo;

        public ChatMessageVH(View itemView) {

            ButterKnife.bind(this, itemView);
        }
    }

    private class ChatMessageAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public Object getItem(int position) {
            return messages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ChatMessageVH holder;
            if (convertView != null) {
                holder = (ChatMessageVH) convertView.getTag();
            } else {
                convertView = View.inflate(parent.getContext(), R.layout.item_chat_message, null);
                holder = new ChatMessageVH(convertView);
                convertView.setTag(holder);
            }

            EMMessage message = (EMMessage) getItem(position);
            EMTextMessageBody body = (EMTextMessageBody) message.getBody();
            String messageText = body.getMessage();
            switch (getItemViewType(position)) {
                case TYPE_FROM:
                    holder.vAreaFrom.setVisibility(View.VISIBLE);
                    holder.vAreaTo.setVisibility(View.GONE);
                    holder.tvMessageFrom.setText(messageText);
                    CommonUtils.loadAvator(holder.ivAvatorFrom, friendAvatorUrl, ChatActivity.this);
//                    Glide.with(ChatActivity.this)
//                            .load(friendAvatorUrl)
//                            .asBitmap()
//                            .centerCrop()
//                            .error(R.color.avator_place_holder)
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .into(holder.ivAvatorFrom);
                    break;
                case TYPE_TO:
                    holder.vAreaFrom.setVisibility(View.GONE);
                    holder.vAreaTo.setVisibility(View.VISIBLE);
                    holder.tvMessageTo.setText(messageText);
                    CommonUtils.loadAvator(holder.ivAvatorTo, mAvatorUrl, ChatActivity.this);
//                    Glide.with(ChatActivity.this)
//                            .load(mAvatorUrl)
//                            .asBitmap()
//                            .centerCrop()
//                            .error(R.color.avator_place_holder)
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .into(holder.ivAvatorTo);
                    break;
            }

            return convertView;
        }


        @Override
        public int getItemViewType(int position) {
            EMMessage emMessage = messages.get(position);
            if (emMessage.getFrom().equals(mChatId.toLowerCase()))
                return TYPE_FROM;
            else
                return TYPE_TO;

        }
    }
}
