package com.zzj.zuzhiji;

import android.content.Context;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jaeger.ninegridimageview.NineGridImageView;
import com.jaeger.ninegridimageview.NineGridImageViewAdapter;
import com.zzj.zuzhiji.util.ActivityManager;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.UIHelper;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    private int mDistanceY;

    private CaseAdapter mAdapter = new CaseAdapter();

    private int scrollHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        ButterKnife.bind(this);
        ActivityManager.getInstance().addActivity(this);

        setupLayout();

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

                    titlebar.setBackgroundColor(Color.argb((int) alpha, 245, 222, 132));
                } else {
                    //上述虽然判断了滑动距离与toolbar高度相等的情况，但是实际测试时发现，标题栏的背景色
                    //很少能达到完全不透明的情况，所以这里又判断了滑动距离大于toolbar高度的情况，
                    //将标题栏的颜色设置为完全不透明状态
                    titlebar.setBackgroundResource(R.color.colorPrimary);
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
