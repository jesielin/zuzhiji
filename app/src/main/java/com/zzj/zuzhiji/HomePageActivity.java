package com.zzj.zuzhiji;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.SocialItem;
import com.zzj.zuzhiji.network.entity.SocialTotal;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;
import com.zzj.zuzhiji.util.UIHelper;

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
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by shawn on 2017-03-29.
 */

public class HomePageActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, BGANinePhotoLayout.Delegate {
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.scroll_container)
    NestedScrollView nestedScrollView;
    @BindView(R.id.titlebar)
    View titlebar;

    @BindView(R.id.back)
    ImageView btnBack;
    @BindView(R.id.star_text)
    TextView tvStar;
    @BindView(R.id.star_icon)
    ImageView ivStar;

    @BindView(R.id.bottom_nav)
    View bottomNav;

    @BindView(R.id.edit)
    View editButton;
    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.summary)
    TextView tvSummary;
    @BindView(R.id.nickname)
    TextView tvName;
    @BindView(R.id.avator)
    ImageView ivAvator;
    @BindView(R.id.no_data)
    TextView tvNoData;


    private int mDistanceY;

    private CaseAdapter mAdapter = new CaseAdapter();

    private int scrollHeight;

    private String friendAvator;
    private String friendUuid;
    private String isFriend;
    private String friendType;
    private String friendSummary;
    private String friendNickName;

    private boolean isMe = false;


    private boolean isReserv;
    private int page = 1;
    private int totalPage = 1;

    private List<SocialItem> datas = new ArrayList<>();
    private String[] IMG_URL_LIST = {
            "http://img3.imgtn.bdimg.com/it/u=1794894692,1423685501&fm=214&gp=0.jpg",
            "https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-480302.jpg",
            "http://ac-QYgvX1CC.clouddn.com/36f0523ee1888a57.jpg", "http://ac-QYgvX1CC.clouddn.com/07915a0154ac4a64.jpg",
            "http://ac-QYgvX1CC.clouddn.com/9ec4bc44bfaf07ed.jpg", "http://ac-QYgvX1CC.clouddn.com/fa85037f97e8191f.jpg",
            "http://ac-QYgvX1CC.clouddn.com/de13315600ba1cff.jpg", "http://ac-QYgvX1CC.clouddn.com/15c5c50e941ba6b0.jpg",
            "http://ac-QYgvX1CC.clouddn.com/10762c593798466a.jpg", "http://ac-QYgvX1CC.clouddn.com/eaf1c9d55c5f9afd.jpg"


    };

    public static Intent newIntent(Context context,
                                   String avator,
                                   String nickName,
                                   String summary,
                                   String type,
                                   String friendUuid
    ) {
        Intent intent = new Intent(context, HomePageActivity.class);
        intent.putExtra(Constant.HOME_PAGE_KEYS.FRIEND_AVATOR, avator);
        intent.putExtra(Constant.HOME_PAGE_KEYS.FRIEND_NICKNAME, nickName);
        intent.putExtra(Constant.HOME_PAGE_KEYS.FRIEND_SUMMARY, summary);
        intent.putExtra(Constant.HOME_PAGE_KEYS.FRIEND_TYPE, type);
        intent.putExtra(Constant.HOME_PAGE_KEYS.FRIEND_UUID, friendUuid);
//        intent.putExtra(Constant.HOME_PAGE_KEYS.IS_FRIEND, isFriend);
        return intent;
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent != null) {


            friendNickName = intent.getStringExtra(Constant.HOME_PAGE_KEYS.FRIEND_NICKNAME) == null ? "" : intent.getStringExtra(Constant.HOME_PAGE_KEYS.FRIEND_NICKNAME);
            friendSummary = intent.getStringExtra(Constant.HOME_PAGE_KEYS.FRIEND_SUMMARY) == null ? "" : intent.getStringExtra(Constant.HOME_PAGE_KEYS.FRIEND_SUMMARY);
            friendType = intent.getStringExtra(Constant.HOME_PAGE_KEYS.FRIEND_TYPE) == null ? "" : intent.getStringExtra(Constant.HOME_PAGE_KEYS.FRIEND_TYPE);
            friendUuid = intent.getStringExtra(Constant.HOME_PAGE_KEYS.FRIEND_UUID) == null ? "" : intent.getStringExtra(Constant.HOME_PAGE_KEYS.FRIEND_UUID);
//            isFriend = intent.getStringExtra(Constant.HOME_PAGE_KEYS.IS_FRIEND) == null ? "" : intent.getStringExtra(Constant.HOME_PAGE_KEYS.IS_FRIEND);
            friendAvator = intent.getStringExtra(Constant.HOME_PAGE_KEYS.FRIEND_AVATOR);


        }

        if (SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID).equals(friendUuid)) {
            editButton.setVisibility(View.VISIBLE);
            bottomNav.setVisibility(View.GONE);
            swipeRefreshLayout.setPadding(0, 0, 0, 0);
            isMe = true;
        } else {
            isMe = false;
            editButton.setVisibility(View.GONE);
            bottomNav.setVisibility(View.VISIBLE);
            final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                    new int[]{android.R.attr.actionBarSize});
            int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
            swipeRefreshLayout.setPadding(0, 0, 0, mActionBarSize);
        }

//        tvTitle.setText(friendNickName);
        tvSummary.setText(friendSummary);
        tvName.setText(friendNickName);




        CommonUtils.loadAvator(ivAvator, friendAvator, this);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        ButterKnife.bind(this);

        setupLayout();
        resolveIntent();

    }


    @OnClick(R.id.reservation)
    public void reserv() {
        Intent intent = new Intent(HomePageActivity.this, ReservationActivity.class);
        intent.putExtra(ReservationActivity.KEY_FROM, ReservationActivity.FROM_TECH);
        intent.putExtra("TECH_ID", friendUuid);
        startActivity(intent);
    }

    @OnClick(R.id.star)
    public void star() {

        if (!isReserv) {
            Network.getInstance().addFriend(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID), friendUuid)
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            mDialog = DialogUtils.showProgressDialog(HomePageActivity.this, "正在关注，请稍等..."); // 需要在主线程执行
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
                            Toast.makeText(HomePageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            DialogUtils.dismissDialog(mDialog);
                        }

                        @Override
                        public void onNext(Object o) {
                            isReserv = true;
                            starLightsUp();
                            DialogUtils.dismissDialog(mDialog);
                        }
                    });
        } else {

            Network.getInstance().delFriend(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID), friendUuid)
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            mDialog = DialogUtils.showProgressDialog(HomePageActivity.this, "取消关注，请稍等..."); // 需要在主线程执行
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
                            Toast.makeText(HomePageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            DialogUtils.dismissDialog(mDialog);
                        }

                        @Override
                        public void onNext(Object o) {
                            isReserv = false;
                            starLightsDown();
                            DialogUtils.dismissDialog(mDialog);
                        }
                    });
        }
    }

    private void setResultStatusChange() {
        if ((Constant.USER_IS_FRIEND.equals(isFriend) && !isReserv) || (Constant.USER_NOT_FRIEND.equals(isFriend) && isReserv))
            setResult(Constant.ACTIVITY_CODE.RESULT_CODE_HOME_PAGE_CHANGE_STATUS_BACK_TO_SEARCH);
    }

    @Override
    public void onBackPressed() {
        setResultStatusChange();
        super.onBackPressed();

    }

    public void starLightsUp() {
        tvStar.setText("已关注");
        ivStar.setImageResource(R.drawable.star_pressed);

    }

    public void starLightsDown() {
        tvStar.setText("关注");
        ivStar.setImageResource(R.drawable.star_nor);
    }

    private void setupLayout() {


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });
        recyclerView.setAdapter(mAdapter);

        scrollHeight = UIHelper.dipToPx(240.0f, HomePageActivity.this) - titlebar.getBottom();
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int dy = scrollY - oldScrollY;
                //滑动的距离
                mDistanceY += dy;
                //toolbar的高度
                DebugLog.e("scroll height:" + scrollHeight);

                //当滑动的距离 <= toolbar高度的时候，改变Toolbar背景色的透明度，达到渐变的效果
                if (mDistanceY <= scrollHeight) {
                    float scale = (float) mDistanceY / scrollHeight;
                    float alpha = scale * 255;
                    tvTitle.setText("");
                    btnBack.setImageResource(R.drawable.back_white);
                    titlebar.setBackgroundColor(Color.argb((int) alpha, 245, 222, 132));
                } else {
                    //上述虽然判断了滑动距离与toolbar高度相等的情况，但是实际测试时发现，标题栏的背景色
                    //很少能达到完全不透明的情况，所以这里又判断了滑动距离大于toolbar高度的情况，
                    //将标题栏的颜色设置为完全不透明状态
                    titlebar.setBackgroundResource(R.color.colorPrimary);
                    tvTitle.setText(friendNickName);
                    btnBack.setImageResource(R.drawable.back);

                }
            }
        });

    }

    @OnClick(R.id.back)
    public void back(View view) {
        if ((Constant.USER_IS_FRIEND.equals(isFriend) && !isReserv) || (Constant.USER_NOT_FRIEND.equals(isFriend) && isReserv))
            setResult(Constant.ACTIVITY_CODE.RESULT_CODE_HOME_PAGE_CHANGE_STATUS_BACK_TO_SEARCH);
        finish();
    }

    @Override
    public void onRefresh() {

        Network.getInstance().isFriend(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID),
                friendUuid).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

                Toast.makeText(HomePageActivity.this, "无法获取关注信息", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNext(String s) {

                isFriend = s;

                if (Constant.USER_IS_FRIEND.equals(isFriend)) {
                    isReserv = true;
                    starLightsUp();
                } else {
                    isReserv = false;
                    starLightsDown();
                }
            }
        });

        CommonUtils.loadAvator(ivAvator, friendAvator, this);

        Network.getInstance().getUserSocialItems(friendUuid, page, Constant.PAGE_SIZE)
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
                        Toast.makeText(HomePageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(List<SocialItem> socialItems) {
                        if (socialItems != null) {
                            datas.clear();
                            datas.addAll(socialItems);
                            mAdapter.notifyDataSetChanged();
                            if (datas.size() == 0) {
                                tvNoData.setVisibility(View.VISIBLE);
                            } else {
                                tvNoData.setVisibility(View.GONE);
                            }
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @OnClick(R.id.edit)
    public void editInfo(View view) {
        startActivity(new Intent(this, UserInfoSettingActivity.class));
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

    public class CaseVH extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.image_group)
        BGANinePhotoLayout bgaNinePhotoLayout;
        @BindView(R.id.subtitle)
        TextView tvSubTitle;
        @BindView(R.id.date)
        TextView tvDate;
        @BindView(R.id.comment_num)
        TextView tvNum;
        @BindView(R.id.del)
        TextView tvDel;

        public CaseVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            bgaNinePhotoLayout.setDelegate(HomePageActivity.this);

        }
    }

    private class CaseAdapter extends RecyclerView.Adapter<CaseVH> {

        @Override
        public CaseVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CaseVH(View.inflate(parent.getContext(), R.layout.item_case, null));
        }

        @Override
        public void onBindViewHolder(CaseVH holder, int position) {

            final SocialItem item = datas.get(position);
            if (isMe) {
                holder.tvDel.setVisibility(View.VISIBLE);
                holder.tvDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        DialogUtils.showAlterDialog(HomePageActivity.this, "删除", "确定要删除此条案例？", "取消", "确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
//
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        mDialog = DialogUtils.showProgressDialog(HomePageActivity.this, "正在删除...");
                                        mDialog.setCancelable(false);
                                        Network.getInstance().del(item.momentOwner, item.momentsID)
                                                .subscribe(new Subscriber<Object>() {
                                                    @Override
                                                    public void onCompleted() {

                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {

                                                        DialogUtils.dismissDialog(mDialog);

                                                        DebugLog.e("error:" + e.getMessage());
                                                    }

                                                    @Override
                                                    public void onNext(Object o) {

                                                        DialogUtils.dismissDialog(mDialog);
                                                        DebugLog.e("");
                                                        onRefresh();

                                                    }
                                                });
                                    }
                                }, true);


                    }
                });
            }


            holder.bgaNinePhotoLayout.setData(item.photos);
            holder.tvTitle.setText(friendNickName);
            holder.tvDate.setText(CommonUtils.getDate(Double.valueOf(item.createDate)));
            holder.tvNum.setText(item.comments == null ? "0" : String.valueOf(item.comments.size()));
            holder.tvSubTitle.setText(item.message);
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
}
