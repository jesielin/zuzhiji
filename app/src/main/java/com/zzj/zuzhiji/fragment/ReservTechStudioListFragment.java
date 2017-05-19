package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
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

import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.StudioItem;
import com.zzj.zuzhiji.util.DebugLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by shawn on 2017-05-18.
 */

public class ReservTechStudioListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.no_data)
    View tvNoData;
    private String tech_uuid;
    private StudioAdapter mAdapter = new StudioAdapter();
    private List<StudioItem> datas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_reserv_tech_studio_list, null);
        ButterKnife.bind(this, contentView);


        Bundle arguments = getArguments();
        tech_uuid = arguments.getString("TECH_ID");

        DebugLog.e("bundle:" + arguments.toString());


        setupLayout();

        return contentView;

    }

    private void setupLayout() {
        tvNoData.setVisibility(View.GONE);
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
        Network.getInstance().getStudioByUser(tech_uuid)
                .subscribe(new Subscriber<List<StudioItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        refreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        tvNoData.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNext(List<StudioItem> studioItems) {
                        refreshLayout.setRefreshing(false);
                        if (studioItems != null && studioItems.size() > 0) {
                            datas.clear();
                            datas.addAll(studioItems);
                            mAdapter.notifyDataSetChanged();
                            tvNoData.setVisibility(View.GONE);
                        } else {
                            tvNoData.setVisibility(View.VISIBLE);
                        }

                    }
                });
    }

    @OnClick(R.id.back)
    public void back() {
        getActivity().onBackPressed();
    }

    class StudioVH extends RecyclerView.ViewHolder {


        @BindView(R.id.clickArea)
        View clickArea;
        @BindView(R.id.title)
        TextView tvTitle;

        public StudioVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class StudioAdapter extends RecyclerView.Adapter<StudioVH> {

        @Override
        public StudioVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new StudioVH(View.inflate(parent.getContext(), R.layout.item_recommend_studio, null));
        }

        @Override
        public void onBindViewHolder(final StudioVH holder, final int position) {

            holder.tvTitle.setText(datas.get(position).title);
            holder.clickArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment f = new ReservCaseListFragment();
                    Bundle bundle = getArguments();
                    bundle.putString("STUDIO_ID", datas.get(position).id);
                    f.setArguments(bundle);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, f).addToBackStack("third").commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
}
