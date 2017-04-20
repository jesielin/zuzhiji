package com.zzj.zuzhiji;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.CommentItem;
import com.zzj.zuzhiji.network.entity.SocialItem;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.KeyboardControlMnanager;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;
import com.zzj.zuzhiji.util.UIHelper;
import com.zzj.zuzhiji.view.NestedListView;

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
import rx.functions.Action0;

/**
 * Created by shawn on 17/3/31.
 */

public class CaseDetailActivity extends BaseActivity implements BGANinePhotoLayout.Delegate {

    @BindView(R.id.list)
    NestedListView listView;
    @BindView(R.id.scroll_container)
    ScrollView scrollView;

    @BindView(R.id.avator)
    ImageView ivAvator;
    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.subtitle)
    TextView tvSubTitle;
    @BindView(R.id.date)
    TextView tvDate;

    @BindView(R.id.message)
    TextView tvMessage;
    @BindView(R.id.image_group)
    BGANinePhotoLayout bgaNinePhotoLayout;

    @BindView(R.id.bottom_chat)
    View bottomChat;
    @BindView(R.id.comment)
    EditText etComment;
    @BindView(R.id.send_comment)
    TextView tvSendComment;

    @BindView(R.id.content)
    View vContent;

    View targetCommentView;

    private SocialItem item;

    private boolean isChangeComment = false;

    private boolean isChatThis = true;

    private String targetFriendId = "";
    private String targetFriendNickName = "";


    private CommentAdapter mAdapter = new CommentAdapter();
    private List<CommentItem> comments = new ArrayList<>();

    public static Intent newIntent(Context context, String itemJson) {
        Intent intent = new Intent(context, CaseDetailActivity.class);

        intent.putExtra(Constant.CASE_DETAIL_KEYS.ITEM_JSON, itemJson);

        return intent;
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        String itemJson = intent.getStringExtra(Constant.CASE_DETAIL_KEYS.ITEM_JSON);
        if (TextUtils.isEmpty(itemJson)) {
            Toast.makeText(this, "intent error", Toast.LENGTH_SHORT).show();
            return;
        }

        item = new Gson().fromJson(itemJson, SocialItem.class);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_detail);
        ButterKnife.bind(this);

        resolveIntent();
        setupLayout();
    }

    @OnClick(R.id.content)
    public void content(View view) {
        Toast.makeText(this, "评论", Toast.LENGTH_SHORT).show();
    }


    private void setupLayout() {

        targetCommentView = vContent;


        listView.setAdapter(mAdapter);
        bgaNinePhotoLayout.setDelegate(this);


        if (item != null) {

            //TODO:
            CommonUtils.loadAvator(ivAvator,CommonUtils.getAvatorAddress(item.momentOwner),this);

            tvTitle.setText(item.momentUserNickname == null ? item.momentOwner : item.momentUserNickname);
            tvMessage.setText(item.message);
            tvDate.setText(CommonUtils.getDate(Double.valueOf(item.createDate)));


            if (item.photos != null)
                bgaNinePhotoLayout.setData(item.photos);

            if (item.comments != null && item.comments.size() > 0) {
                comments.clear();
                comments.addAll(item.comments);
                mAdapter.notifyDataSetChanged();
            }
        }

        setupKeyboardAction();

        vContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setEditableState(true);
                isChatThis = true;
                targetFriendId = "";
                targetFriendNickName = "";
                etComment.setHint("添加评论:");
                targetCommentView = vContent;
                UIHelper.showSoftInput(CaseDetailActivity.this, etComment);
            }
        });

        KeyboardControlMnanager.observerKeyboardVisibleChange(this, new KeyboardControlMnanager.OnKeyboardStateChangeListener() {
            @Override
            public void onKeyboardChange(int displayHeight, int statusbarHeight, boolean isVisible) {

                int[] contentLocation = new int[2];
                int[] chatLocation = new int[2];
                int itemHeight;


                targetCommentView.getLocationInWindow(contentLocation);
                bottomChat.getLocationInWindow(chatLocation);
                itemHeight = targetCommentView.getMeasuredHeight();


                scrollView.scrollBy(0, contentLocation[1] + itemHeight - chatLocation[1]);
            }
        });

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (UIHelper.isShowSoftInput(CaseDetailActivity.this))
                        UIHelper.hideSoftInput(CaseDetailActivity.this, etComment);

                }
                return false;

            }
        });



        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s))
                    enableSendButton();
                else
                    disableSendButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tvSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UIHelper.isShowSoftInput(CaseDetailActivity.this))
                    UIHelper.hideSoftInput(CaseDetailActivity.this, etComment);

                Network.getInstance().sendComment(item.momentsID, item.momentOwner, SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID),
                        targetFriendId, etComment.getText().toString(), SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.NICK_NAME), targetFriendNickName)
                        .doOnSubscribe(new Action0() {
                            @Override
                            public void call() {
                                mDialog = DialogUtils.showProgressDialog(CaseDetailActivity.this, "评论中，请稍等..."); // 需要在主线程执行
                            }
                        })
                        .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Object>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(CaseDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                DialogUtils.dismissDialog(mDialog);
                            }

                            @Override
                            public void onNext(Object o) {
                                isChangeComment = true;
                                //TODO:
                                comments.add(new CommentItem(
                                        SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID),
                                        SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.NICK_NAME),
                                        targetFriendId,
                                        targetFriendNickName,
                                        etComment.getText().toString()
                                ));
                                mAdapter.notifyDataSetChanged();
                                etComment.setText("");
                                DialogUtils.dismissDialog(mDialog);
                            }
                        });

            }
        });

    }

    @Override
    public void onBackPressed() {
        setResultStatusChange();
        super.onBackPressed();
    }

    private void setResultStatusChange() {
        if (isChangeComment)
            setResult(Constant.ACTIVITY_CODE.RESULT_CODE_DETAIL_CHANGE_STATUS_BACK_TO_SOCIAL);
    }

    private void enableSendButton() {
        tvSendComment.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        tvSendComment.setClickable(true);
    }


    private void disableSendButton() {
        tvSendComment.setTextColor(getResources().getColor(R.color.text_hint));
        tvSendComment.setClickable(false);
    }

    private void setupKeyboardAction() {
        etComment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                DebugLog.e("event:" + event);
                if (!etComment.isFocused()) {

                    etComment.setFocusable(true);
                    etComment.setFocusableInTouchMode(true);
                }
                return false;
            }
        });
        etComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    setEditableState(true);
                } else {
                    setEditableState(false);
                }
            }
        });
    }


    private void setEditableState(boolean b) {
        if (b) {

            etComment.setFocusable(true);
            etComment.setFocusableInTouchMode(true);
            etComment.requestFocus();
        } else {
            etComment.setFocusable(false);
            etComment.setFocusableInTouchMode(false);
        }
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
        if (EasyPermissions.hasPermissions(this, perms)) {
            if (bgaNinePhotoLayout.getItemCount() == 1) {
                // 预览单张图片

                startActivity(BGAPhotoPreviewActivity.newIntent(this, downloadDir, bgaNinePhotoLayout.getCurrentClickItem()));
            } else if (bgaNinePhotoLayout.getItemCount() > 1) {
                // 预览多张图片

                startActivity(BGAPhotoPreviewActivity.newIntent(this, downloadDir, bgaNinePhotoLayout.getData(), bgaNinePhotoLayout.getCurrentClickItemPosition()));
            }
        } else {
            EasyPermissions.requestPermissions(this, "图片预览需要以下权限:\n\n1.访问设备上的照片", Constant.REQUEST_CODE_PERMISSION_PHOTO_PREVIEW, perms);
        }
    }

    @OnClick(R.id.back)
    public void back() {
        setResultStatusChange();
        finish();
    }

    public class CommentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return comments.size();
        }

        @Override
        public Object getItem(int position) {
            return comments.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            final ViewHolder holder;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                view = View.inflate(parent.getContext(), R.layout.item_comment, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            final CommentItem comment = comments.get(position);
            holder.tvCommenterName.setText(comment.commenterNickname == null ? comment.commenterUUID : comment.commenterNickname);
            holder.tvSubTitle.setText(comment.message);
            if (TextUtils.isEmpty(comment.targetCommenterUUID)) {
                holder.tvTextHuiFu.setVisibility(View.GONE);
                holder.tvFriendName.setVisibility(View.GONE);
            } else {
                holder.tvTextHuiFu.setVisibility(View.VISIBLE);
                holder.tvFriendName.setVisibility(View.VISIBLE);
                holder.tvFriendName.setText(comment.targetCommenterNickname == null ? comment.targetCommenterUUID : comment.targetCommenterNickname);
            }

            CommonUtils.loadAvator(holder.ivAvator,CommonUtils.getAvatorAddress(comment.commenterUUID),CaseDetailActivity.this);


            holder.clickAreaView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setEditableState(true);
                    targetFriendNickName = comment.commenterNickname;
                    targetFriendId = comment.commenterUUID;
                    isChatThis = false;
                    targetCommentView = holder.clickAreaView;
                    etComment.setHint("回复" + comment.commenterUUID + ":");

                    UIHelper.showSoftInput(CaseDetailActivity.this, etComment);

                    Toast.makeText(CaseDetailActivity.this, position + "", Toast.LENGTH_SHORT).show();
                }
            });


            return view;
        }


        public class ViewHolder {
            @BindView(R.id.avator)
            ImageView ivAvator;
            @BindView(R.id.commenter_name)
            TextView tvCommenterName;
            @BindView(R.id.text_huifu)
            TextView tvTextHuiFu;
            @BindView(R.id.friend_name)
            TextView tvFriendName;
            @BindView(R.id.date)
            TextView tvDate;
            @BindView(R.id.subtitle)
            TextView tvSubTitle;
            @BindView(R.id.container)
            View clickAreaView;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }


}
