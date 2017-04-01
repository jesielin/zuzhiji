package com.zzj.zuzhiji;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.entity.CommentItem;
import com.zzj.zuzhiji.network.entity.SocialItem;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.KeyboardControlMnanager;
import com.zzj.zuzhiji.view.NestedListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGANinePhotoLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by shawn on 17/3/31.
 */

public class CaseDetailActivity extends AppCompatActivity implements BGANinePhotoLayout.Delegate {

    @BindView(R.id.list)
    NestedListView listView;
    @BindView(R.id.scroll_container)
    ScrollView scrollView;

    @BindView(R.id.avator)
    CircleImageView ivAvator;
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

    private SocialItem item;

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


        listView.setAdapter(mAdapter);
        bgaNinePhotoLayout.setDelegate(this);


        if (item != null) {

            //TODO:
            Glide.with(this)
                    .load(R.drawable.avator)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivAvator);

            tvTitle.setText(item.momentOwner);
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
        KeyboardControlMnanager.observerKeyboardVisibleChange(this, new KeyboardControlMnanager.OnKeyboardStateChangeListener() {
            @Override
            public void onKeyboardChange(int displayHeight, int statusbarHeight, boolean isVisible) {

                int[] contentLocation = new int[2];
                int[] chatLocation = new int[2];

                vContent.getLocationInWindow(contentLocation);
                bottomChat.getLocationInWindow(chatLocation);

//                scrollView.scrollTo(scrollView.getScrollX(),scrollView.getTop()+
//                        ());
                scrollView.scrollBy(0, contentLocation[1] + vContent.getMeasuredHeight() - chatLocation[1]);
            }
        });





    }

    private void setupKeyboardAction() {
        etComment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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

    /**
     * display soft keyboard
     */
    protected void openSoftKeyboard(EditText et) {
        InputMethodManager inputManager = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(et, 0);
    }

    /**
     * close soft keyboard
     */
    protected void closeSoftKeyboard(EditText et) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
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
            ViewHolder holder;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                view = View.inflate(parent.getContext(), R.layout.item_comment, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            CommentItem comment = comments.get(position);
            holder.tvCommenterName.setText(comment.commenterUUID);
            holder.tvSubTitle.setText(comment.message);
            if (comment.targetCommenterUUID == null) {
                holder.tvTextHuiFu.setVisibility(View.GONE);
                holder.tvFriendName.setVisibility(View.GONE);
            } else {
                holder.tvTextHuiFu.setVisibility(View.VISIBLE);
                holder.tvFriendName.setVisibility(View.VISIBLE);
                holder.tvFriendName.setText(comment.targetCommenterUUID);
            }


            holder.clickAreaView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(CaseDetailActivity.this, position + "", Toast.LENGTH_SHORT).show();
                }
            });


            return view;
        }

        public class ViewHolder {
            @BindView(R.id.avator)
            CircleImageView ivAvator;
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
