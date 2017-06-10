package com.zzj.zuzhiji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yayandroid.theactivitymanager.TheActivityManager;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.PaidItem;
import com.zzj.zuzhiji.network.entity.PaidTotal;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;
import com.zzj.zuzhiji.wxapi.WXPayEntryActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by shawn on 2017-06-05.
 */

public class PaidListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.no_data)
    View vNoData;

    private int page = 1;
    private int totalPage;

    private List<PaidItem> datas = new ArrayList<>();
    private PaidAdapter mAdapter = new PaidAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paid_list);
        ButterKnife.bind(this);
        TheActivityManager.getInstance().finishInstance(WXPayEntryActivity.class);
        TheActivityManager.getInstance().finishInstance(ReservationActivity.class);
        setupLayout();


    }

    @OnClick(R.id.back)
    public void back() {
        onBackPressed();
    }

    private void setupLayout() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(mAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });
    }

    @Override
    public void onRefresh() {
        Network.getInstance().getPaidList(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID), page)
                .observeOn(Schedulers.io())
                .map(new Func1<PaidTotal, List<PaidItem>>() {
                    @Override
                    public List<PaidItem> call(PaidTotal paidTotal) {
                        totalPage = paidTotal.totalPage;
                        return paidTotal.items;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<PaidItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        Toast.makeText(PaidListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);

                    }

                    @Override
                    public void onNext(List<PaidItem> paidItems) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (paidItems != null && paidItems.size() > 0) {
                            datas.clear();
                            datas.addAll(paidItems);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            vNoData.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    public class PaidVH extends RecyclerView.ViewHolder {

        @BindView(R.id.orderno)
        TextView tvOrderNo;
        @BindView(R.id.techname)
        TextView tvTechName;
        @BindView(R.id.amount)
        TextView tvAmount;
        @BindView(R.id.payorderno)
        TextView tvPayOrderNo;
        @BindView(R.id.time)
        TextView tvTime;

        public PaidVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class PaidAdapter extends RecyclerView.Adapter<PaidVH> {

        @Override
        public PaidVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PaidVH(View.inflate(parent.getContext(), R.layout.item_paid, null));
        }

        @Override
        public void onBindViewHolder(PaidVH holder, int position) {

            PaidItem item = datas.get(position);
            holder.tvAmount.setText(item.amount + "元");
            holder.tvOrderNo.setText(item.orderNo);
            holder.tvTechName.setText(item.techName);
            holder.tvPayOrderNo.setText(item.ticketNo);
            holder.tvTime.setText(CommonUtils.getDate(Double.valueOf(item.createTime)));
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
}
