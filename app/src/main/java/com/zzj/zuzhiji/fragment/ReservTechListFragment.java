package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.Tech;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;
import com.zzj.zuzhiji.util.UIHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by shawn on 17/4/10.
 */

public class ReservTechListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.search)
    EditText etSearch;
    @BindView(R.id.refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.list)
    RecyclerView recyclerView;

    private List<Tech> datas = new ArrayList<>();

    private SearchAdapter mAdapter = new SearchAdapter();

    private boolean isSearch = false;

    private int currentPage = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_reserv_tech_list, null);
        ButterKnife.bind(this, contentView);

        setupLayout();

        return contentView;

    }

    @OnClick(R.id.cancel)
    public void cancel(View view) {
        getActivity().onBackPressed();
    }

    private void setupLayout() {

        etSearch.setOnKeyListener(new View.OnKeyListener() {

            @Override

            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    // 先隐藏键盘
                    UIHelper.hideInputMethod(etSearch);
                    isSearch = true;
                    //进行搜索操作的方法，在该方法中可以加入mEditSearchUser的非空判断
                    doRefresh();

                }
                return false;
            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (CommonUtils.isShowSoftInput(getActivity())) {
                    CommonUtils.hideSoftInput(getActivity(), etSearch);
                }
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        recyclerView.setAdapter(mAdapter);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                doRefresh();
            }
        });
    }

    private void doRefresh() {
        refreshLayout.setRefreshing(true);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        if (isSearch) {
            if (TextUtils.isEmpty(etSearch.getText().toString().trim())) {
                Toast.makeText(getActivity(), "搜索不能为空", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
        } else {
            Network.getInstance().getRecommendTech(Constant.PAGE_SIZE)
                    .subscribe(new Subscriber<List<Tech>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            refreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onNext(List<Tech> teches) {
                            if (teches != null && teches.size() > 0) {
                                datas.clear();
                                datas.addAll(teches);
                                mAdapter.notifyDataSetChanged();
                            }
                            refreshLayout.setRefreshing(false);
                        }
                    });
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
            holder.tvSubTitle.setText(item.summary);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.hideSoftInput(getActivity(), etSearch);
                    Fragment f = new ReservCaseListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("UUID", item.uuid);
                    f.setArguments(bundle);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, f).addToBackStack("second").commit();
                }
            });

            CommonUtils.loadAvator(holder.ivAvator, CommonUtils.getAvatorAddress(item.uuid), getActivity());

        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
}
