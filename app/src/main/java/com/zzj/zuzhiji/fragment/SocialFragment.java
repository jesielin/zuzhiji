package com.zzj.zuzhiji.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.zzj.zuzhiji.CaseDetailActivity;
import com.zzj.zuzhiji.PublishActivity;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.SocialItem;
import com.zzj.zuzhiji.network.entity.SocialTotal;
import com.zzj.zuzhiji.network.entity.UserInfoResult;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.io.File;
import java.lang.reflect.Type;
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
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by shawn on 2017-03-29.
 */

public class SocialFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, BGANinePhotoLayout.Delegate {


    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private SocialAdapter mAdapter = new SocialAdapter();

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
        startActivityForResult(new Intent(getActivity(), PublishActivity.class),
                Constant.ACTIVITY_CODE.REQUEST_CODE_SOCIAL_FRAGMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DebugLog.e("fragment on activity result::" + requestCode + "," + resultCode);
        if ((requestCode == Constant.ACTIVITY_CODE.REQUEST_CODE_SOCIAL_FRAGMENT
                && resultCode == Constant.ACTIVITY_CODE.RESULT_CODE_PUBLISH_SUCCESS)
                || (requestCode == Constant.ACTIVITY_CODE.REQUEST_CODE_SOCIAL_TO_DETAIL
                && resultCode == Constant.ACTIVITY_CODE.RESULT_CODE_DETAIL_CHANGE_STATUS_BACK_TO_SOCIAL)) {
            DebugLog.e("fragment on activity result");
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    doRefresh();
                }
            });
        }
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
                doRefresh();
            }
        });
        recyclerView.setAdapter(mAdapter);


    }

    private void doRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
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


        Network.getInstance().getSocialItems(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID)
                , page, Constant.PAGE_SIZE)
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
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(List<SocialItem> socialItems) {
                        if (socialItems != null) {
                            datas.clear();
                            datas.addAll(socialItems);
                            mAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });

    }

    private void getUserName(final TextView tv, final String ownerId) {

        Network.getInstance().getUserInfo(ownerId)
                .subscribe(new Subscriber<UserInfoResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(UserInfoResult userInfoResult) {
                        if (userInfoResult != null)
                            tv.setText(userInfoResult.nickName);
                        else {
                            DebugLog.e("user owner:" + ownerId);
                            //TODO:设置不上
                            tv.setText(String.valueOf(ownerId));
                        }

                    }
                });

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
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            if (bgaNinePhotoLayout.getItemCount() == 1) {
                // 预览单张图片

                startActivity(BGAPhotoPreviewActivity.newIntent(getActivity(), downloadDir, bgaNinePhotoLayout.getCurrentClickItem()));
            } else if (bgaNinePhotoLayout.getItemCount() > 1) {
                // 预览多张图片

                startActivity(BGAPhotoPreviewActivity.newIntent(getActivity(), downloadDir, bgaNinePhotoLayout.getData(), bgaNinePhotoLayout.getCurrentClickItemPosition()));
            }
        } else {
            EasyPermissions.requestPermissions(this, "图片预览需要以下权限:\n\n1.访问设备上的照片", Constant.REQUEST_CODE_PERMISSION_PHOTO_PREVIEW, perms);
        }
    }

    public class SocialVH extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.subtitle)
        TextView tvSubTitle;
        @BindView(R.id.date)
        TextView tvDate;
        @BindView(R.id.comment_num)
        TextView tvCommentNum;
        @BindView(R.id.image_group)
        BGANinePhotoLayout bgaNinePhotoLayout;
        @BindView(R.id.container)
        View clickAreaView;

        public SocialVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            bgaNinePhotoLayout.setDelegate(SocialFragment.this);
        }
    }

    private class SocialAdapter extends RecyclerView.Adapter<SocialVH> {

        private Gson gson;

        SocialAdapter() {
            GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
            gson = gsonBuilder.create();
        }

        @Override
        public SocialVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SocialVH(View.inflate(parent.getContext(), R.layout.item_social, null));
        }

        @Override
        public void onBindViewHolder(final SocialVH holder, int position) {
            final SocialItem item = datas.get(position);
            holder.bgaNinePhotoLayout.setData(item.photos);
            //TODO:
//            getUserName(holder.tvTitle,item.momentOwner);
            holder.tvTitle.setText(item.momentOwner);
            holder.tvSubTitle.setText(item.message);
            holder.tvDate.setText(CommonUtils.getDate(Double.valueOf(item.createDate)));
            holder.tvCommentNum.setText(item.comments == null ? "0" : String.valueOf(item.comments.size()));


            holder.clickAreaView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(CaseDetailActivity.newIntent(getActivity(), gson.toJson(item)),
                            Constant.ACTIVITY_CODE.REQUEST_CODE_SOCIAL_TO_DETAIL);
                }
            });
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        public class StringConverter implements JsonSerializer<String>, JsonDeserializer<String> {
            public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
                if (src == null) {
                    return new JsonPrimitive("");
                } else {
                    return new JsonPrimitive(src.toString());
                }
            }

            public String deserialize(JsonElement json, Type typeOfT,
                                      JsonDeserializationContext context)
                    throws JsonParseException {
                return json.getAsJsonPrimitive().getAsString();
            }
        }
    }
}
