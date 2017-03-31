package com.zzj.zuzhiji;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jaeger.ninegridimageview.NineGridImageView;
import com.jaeger.ninegridimageview.NineGridImageViewAdapter;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.util.ActivityManager;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;
import com.zzj.zuzhiji.util.UIHelper;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by shawn on 2017-03-29.
 */

public class HomePageActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
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
    CircleImageView ivAvator;



    private int mDistanceY;

    private CaseAdapter mAdapter = new CaseAdapter();

    private int scrollHeight;

    private String friendAvator;
    private String friendUuid;
    private String isFriend;
    private String friendType;
    private String friendSummary;
    private String friendNickName;

    private MaterialDialog dialog;

    private boolean isReserv;

    public static Intent newIntent(Context context,
                                   String avator,
                                   String nickName,
                                   String summary,
                                   String type,
                                   String friendUuid,
                                   String isFriend) {
        Intent intent = new Intent(context, HomePageActivity.class);
        intent.putExtra(Constant.CASE_DETAIL_KEYS.FRIEND_AVATOR, avator);
        intent.putExtra(Constant.CASE_DETAIL_KEYS.FRIEND_NICKNAME, nickName);
        intent.putExtra(Constant.CASE_DETAIL_KEYS.FRIEND_SUMMARY, summary);
        intent.putExtra(Constant.CASE_DETAIL_KEYS.FRIEND_TYPE, type);
        intent.putExtra(Constant.CASE_DETAIL_KEYS.FRIEND_UUID, friendUuid);
        intent.putExtra(Constant.CASE_DETAIL_KEYS.IS_FRIEND, isFriend);
        return intent;
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent != null) {

            friendAvator = intent.getStringExtra(Constant.CASE_DETAIL_KEYS.FRIEND_AVATOR) == null ? "" : intent.getStringExtra(Constant.CASE_DETAIL_KEYS.FRIEND_AVATOR);
            friendNickName = intent.getStringExtra(Constant.CASE_DETAIL_KEYS.FRIEND_NICKNAME) == null ? "" : intent.getStringExtra(Constant.CASE_DETAIL_KEYS.FRIEND_NICKNAME);
            friendSummary = intent.getStringExtra(Constant.CASE_DETAIL_KEYS.FRIEND_SUMMARY) == null ? "" : intent.getStringExtra(Constant.CASE_DETAIL_KEYS.FRIEND_SUMMARY);
            friendType = intent.getStringExtra(Constant.CASE_DETAIL_KEYS.FRIEND_TYPE) == null ? "" : intent.getStringExtra(Constant.CASE_DETAIL_KEYS.FRIEND_TYPE);
            friendUuid = intent.getStringExtra(Constant.CASE_DETAIL_KEYS.FRIEND_UUID) == null ? "" : intent.getStringExtra(Constant.CASE_DETAIL_KEYS.FRIEND_UUID);
            isFriend = intent.getStringExtra(Constant.CASE_DETAIL_KEYS.IS_FRIEND) == null ? "" : intent.getStringExtra(Constant.CASE_DETAIL_KEYS.IS_FRIEND);

        }

        if (SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID).equals(friendUuid)) {
            editButton.setVisibility(View.VISIBLE);
            bottomNav.setVisibility(View.GONE);
        } else {
            editButton.setVisibility(View.GONE);
            bottomNav.setVisibility(View.VISIBLE);
        }

        tvTitle.setText(friendNickName);
        tvSummary.setText(friendSummary);
        tvName.setText(friendNickName);

        if (Constant.USER_IS_FRIEND.equals(isFriend)) {
            isReserv = true;
            starLightsUp();
        } else {
            isReserv = false;
            starLightsDown();
        }


    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        ButterKnife.bind(this);
        ActivityManager.getInstance().addActivity(this);

        setupLayout();
        resolveIntent();

    }


    @OnClick(R.id.star)
    public void star() {

        if (!isReserv) {
            Network.getInstance().addFriend(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID), friendUuid)
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            dialog = DialogUtils.showProgressDialog(HomePageActivity.this, "关注", "正在关注，请稍等..."); // 需要在主线程执行
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
                            dismissDialog();
                        }

                        @Override
                        public void onNext(Object o) {
                            isReserv = true;
                            starLightsUp();
                            dismissDialog();
                        }
                    });
        } else {

            //TODO:取消关注
        }
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
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

        scrollHeight = UIHelper.dipToPx(240.0f) - titlebar.getBottom();
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
        ActivityManager.getInstance().finshActivities(getClass());
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
    }

    private String[] IMG_URL_LIST = {
            "http://img3.imgtn.bdimg.com/it/u=1794894692,1423685501&fm=214&gp=0.jpg",
            "https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-480302.jpg",
            "http://ac-QYgvX1CC.clouddn.com/36f0523ee1888a57.jpg", "http://ac-QYgvX1CC.clouddn.com/07915a0154ac4a64.jpg",
            "http://ac-QYgvX1CC.clouddn.com/9ec4bc44bfaf07ed.jpg", "http://ac-QYgvX1CC.clouddn.com/fa85037f97e8191f.jpg",
            "http://ac-QYgvX1CC.clouddn.com/de13315600ba1cff.jpg", "http://ac-QYgvX1CC.clouddn.com/15c5c50e941ba6b0.jpg",
            "http://ac-QYgvX1CC.clouddn.com/10762c593798466a.jpg", "http://ac-QYgvX1CC.clouddn.com/eaf1c9d55c5f9afd.jpg"


    };

    public class CaseVH extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.image_group)
        NineGridImageView nineGridImageView;

        public CaseVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            nineGridImageView.setAdapter(mImageAdapter);
        }
    }

    private class CaseAdapter extends RecyclerView.Adapter<CaseVH> {

        @Override
        public CaseVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CaseVH(View.inflate(parent.getContext(), R.layout.item_case, null));
        }

        @Override
        public void onBindViewHolder(CaseVH holder, int position) {
            holder.nineGridImageView.setImagesData(Arrays.asList(IMG_URL_LIST).subList(0, (position + 2) % 9));
        }

        @Override
        public int getItemCount() {
            return 10;
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
