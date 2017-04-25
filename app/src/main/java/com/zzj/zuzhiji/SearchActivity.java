package com.zzj.zuzhiji;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.Tech;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
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

public class SearchActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {


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
        ButterKnife.bind(this);
        setupLayout();
    }

    private void setupLayout() {

        etSearch.setOnKeyListener(new View.OnKeyListener() {

            @Override

            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    // 先隐藏键盘
                    UIHelper.hideSoftInput(SearchActivity.this, etSearch);
                    //进行搜索操作的方法，在该方法中可以加入mEditSearchUser的非空判断
                    doRefresh();

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

    private void doRefresh() {
        refreshLayout.setRefreshing(true);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        if (TextUtils.isEmpty(etSearch.getText().toString().trim())) {
            Toast.makeText(this, "搜索不能为空", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            return;
        }


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


    }

    @OnClick(R.id.cancel)
    public void cancel(View view) {
        onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DebugLog.e("result:" + requestCode + "," + resultCode);
        if (requestCode == Constant.ACTIVITY_CODE.REQUEST_CODE_SEARCH_TO_HOME_PAGE && resultCode == Constant.ACTIVITY_CODE.RESULT_CODE_HOME_PAGE_CHANGE_STATUS_BACK_TO_SEARCH) {
            doRefresh();
        }
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
            final Tech item = datas.get(position);
            holder.tvTitle.setText(item.nickName == null ? item.uuid : item.nickName);
            if (TextUtils.isEmpty(item.summary))
                holder.tvSubTitle.setVisibility(View.GONE);
            else {
                holder.tvSubTitle.setText(item.summary);
                holder.tvSubTitle.setVisibility(View.VISIBLE);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(HomePageActivity.newIntent(SearchActivity.this,
                            item.headSculpture,
                            item.nickName,
                            item.summary,
                            item.userType,
                            item.uuid,
                            item.isFriend), Constant.ACTIVITY_CODE.REQUEST_CODE_SEARCH_TO_HOME_PAGE);
                }
            });

            CommonUtils.loadAvator(holder.ivAvator, item.headSculpture, SearchActivity.this);

        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
}
