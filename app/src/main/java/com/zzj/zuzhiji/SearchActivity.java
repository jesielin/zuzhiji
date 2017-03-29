package com.zzj.zuzhiji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.ApiException;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.Tech;
import com.zzj.zuzhiji.util.ActivityManager;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;
import com.zzj.zuzhiji.util.UIHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by shawn on 2017-03-29.
 */

public class SearchActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {


    @BindView(R.id.search)
    EditText etSearch;
    @BindView(R.id.refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.list)
    RecyclerView recyclerView;


    private int currentPage = 1;

    private List<Tech> datas = new ArrayList<>();

    private SearchAdapter mAdapter = new SearchAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ActivityManager.getInstance().addActivity(this);
        ButterKnife.bind(this);
        setupLayout();
    }

    private void setupLayout() {

        etSearch.setOnKeyListener(new View.OnKeyListener() {

            @Override

            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    // 先隐藏键盘
                    UIHelper.hideInputMethod(etSearch);
                    //进行搜索操作的方法，在该方法中可以加入mEditSearchUser的非空判断
                    refreshLayout.setRefreshing(true);
                    onRefresh();

                }
                return false;
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onRefresh() {
        if (TextUtils.isEmpty(etSearch.getText().toString().trim())) {
            Toast.makeText(this, "搜索不能为空", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            return;
        }

        try {
            Network.getInstance().searchTech(
                    currentPage,
                    Constant.PAGE_SIZE,
                    etSearch.getText().toString().trim(),
                    SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID))
                    .subscribe(new Subscriber<List<Tech>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            refreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onNext(List<Tech> teches) {
                            if (teches != null) {
                                datas.clear();
                                datas.addAll(teches);
                                mAdapter.notifyDataSetChanged();

                            }

                            refreshLayout.setRefreshing(false);

                        }
                    });
        } catch (ApiException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
        }

    }

    @OnClick(R.id.cancel)
    public void cancel(View view) {
        ActivityManager.getInstance().finshActivities(this.getClass());
    }

    public class SearchVH extends RecyclerView.ViewHolder {

        @BindView(R.id.avator)
        ImageView ivAvator;
        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.subtitle)
        TextView tvSubTitle;

        public SearchVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class SearchAdapter extends RecyclerView.Adapter<SearchVH> {

        @Override
        public SearchVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SearchVH(View.inflate(parent.getContext(), R.layout.item_search, null));
        }

        @Override
        public void onBindViewHolder(final SearchVH holder, final int position) {
            holder.tvTitle.setText(datas.get(position).nickName);
            holder.tvSubTitle.setText(datas.get(position).summary);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SearchActivity.this, "aaa", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
}