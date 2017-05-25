package com.zzj.zuzhiji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.StudioItem;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by shawn on 2017-05-25.
 */

public class JoinStudioActivity extends BaseActivity {

    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.no_data)
    View vNoData;
    private List<StudioItem> datas = new ArrayList<>();
    private StudioAdapter mAdapter = new StudioAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_studio);
        ButterKnife.bind(this);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(JoinStudioActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(JoinStudioActivity.this, linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(mAdapter);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Network.getInstance().getUnjoinStudioByUser(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID))
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mDialog = DialogUtils.showProgressDialog(JoinStudioActivity.this, "正在获取工作室列表...");

                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<StudioItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(JoinStudioActivity.this, "获取工作室错误:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        DialogUtils.dismissDialog(mDialog);
                    }

                    @Override
                    public void onNext(final List<StudioItem> studioItems) {
                        DialogUtils.dismissDialog(mDialog);
                        vNoData.setVisibility(View.GONE);


                        if (studioItems != null && studioItems.size() == 0) {
                            vNoData.setVisibility(View.VISIBLE);
                            return;
                        }

                        DebugLog.e("size:" + studioItems.size());


                        datas.clear();
                        datas.addAll(studioItems);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    @OnClick(R.id.back)
    public void back() {
        onBackPressed();
    }

    public class StudioVH extends RecyclerView.ViewHolder {

        @BindView(R.id.avator)
        ImageView ivAvator;
        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.subtitle)
        TextView tvSubtitle;

        public StudioVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class StudioAdapter extends RecyclerView.Adapter<StudioVH> {

        @Override
        public StudioVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new StudioVH(View.inflate(parent.getContext(), R.layout.item_studio, null));
        }

        @Override
        public void onBindViewHolder(StudioVH holder, int position) {
            final StudioItem studioItem = datas.get(position);
            CommonUtils.loadAvator(holder.ivAvator, studioItem.headSculpture, JoinStudioActivity.this);
            holder.tvTitle.setText(studioItem.title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Network.getInstance().joinStudio(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID), studioItem.id)
                            .doOnSubscribe(new Action0() {
                                @Override
                                public void call() {
                                    mDialog = DialogUtils.showProgressDialog(JoinStudioActivity.this, "正在加入工作室...");

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

                                    Toast.makeText(JoinStudioActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    DialogUtils.dismissDialog(mDialog);
                                }

                                @Override
                                public void onNext(Object o) {
                                    Toast.makeText(JoinStudioActivity.this, "加入工作室成功", Toast.LENGTH_SHORT).show();
                                    DialogUtils.dismissDialog(mDialog);
                                    onBackPressed();

                                }
                            });
                }


            });

        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
}
