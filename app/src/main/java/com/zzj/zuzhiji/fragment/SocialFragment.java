package com.zzj.zuzhiji.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
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
import com.zzj.zuzhiji.HomePageActivity;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.UIHelper;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.zzj.zuzhiji.R.id.titlebar;

/**
 * Created by shawn on 2017-03-29.
 */

public class SocialFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private SocialAdapter mAdapter = new SocialAdapter();

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
        Toast.makeText(getActivity(), "发表朋友圈", Toast.LENGTH_SHORT).show();
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
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });
        recyclerView.setAdapter(mAdapter);


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

    public class SocialVH extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.image_group)
        NineGridImageView nineGridImageView;

        public SocialVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            nineGridImageView.setAdapter(mImageAdapter);
        }
    }

    private class SocialAdapter extends RecyclerView.Adapter<SocialVH> {

        @Override
        public SocialVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SocialVH(View.inflate(parent.getContext(), R.layout.item_social, null));
        }

        @Override
        public void onBindViewHolder(SocialVH holder, int position) {
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
