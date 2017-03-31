package com.zzj.zuzhiji;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zzj.zuzhiji.app.Constant;

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

public class CaseDetailActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, BGANinePhotoLayout.Delegate {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.scroll_container)
    NestedScrollView nestedScrollView;

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

    private CommentAdapter mAdapter = new CommentAdapter();

    private String[] IMG_URL_LIST = {
            "http://img3.imgtn.bdimg.com/it/u=1794894692,1423685501&fm=214&gp=0.jpg",
            "https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-480302.jpg",
            "http://ac-QYgvX1CC.clouddn.com/36f0523ee1888a57.jpg", "http://ac-QYgvX1CC.clouddn.com/07915a0154ac4a64.jpg",
            "http://ac-QYgvX1CC.clouddn.com/9ec4bc44bfaf07ed.jpg", "http://ac-QYgvX1CC.clouddn.com/fa85037f97e8191f.jpg",
            "http://ac-QYgvX1CC.clouddn.com/de13315600ba1cff.jpg", "http://ac-QYgvX1CC.clouddn.com/15c5c50e941ba6b0.jpg",
            "http://ac-QYgvX1CC.clouddn.com/10762c593798466a.jpg"


    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_detail);
        ButterKnife.bind(this);

        setupLayout();
    }

    private void setupLayout() {


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                doRefresh();
            }
        });
        recyclerView.setAdapter(mAdapter);
        bgaNinePhotoLayout.setDelegate(this);
        ArrayList<String> list = new ArrayList();
        for (String url : IMG_URL_LIST) {
            list.add(url);
        }
        bgaNinePhotoLayout.setData(list);


    }

    private void doRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    @Override
    public void onClickNinePhotoItem(BGANinePhotoLayout ninePhotoLayout, View view, int position, String model, List<String> models) {
        photoPreviewWrapper(ninePhotoLayout);
    }

    public class CommentVH extends RecyclerView.ViewHolder {

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

        public CommentVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentVH> {

        @Override
        public CommentVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CommentVH(View.inflate(parent.getContext(), R.layout.item_comment, null));
        }

        @Override
        public void onBindViewHolder(CommentVH holder, final int position) {
            holder.clickAreaView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(CaseDetailActivity.this, position + "", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return 10;
        }
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


}
