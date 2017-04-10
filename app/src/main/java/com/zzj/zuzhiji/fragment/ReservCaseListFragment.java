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
import com.zzj.zuzhiji.network.entity.ServiceItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by shawn on 17/4/10.
 */

public class ReservCaseListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.refresh)
    SwipeRefreshLayout swipeRefreshLayout;


    private List<ServiceItem> datas = new ArrayList<>();

    private ServiceAdapter mAdapter = new ServiceAdapter();

    private String uuid;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_reserv_case_list, null);
        ButterKnife.bind(this, contentView);

        Bundle arguments = getArguments();
        uuid = arguments.getString("UUID");


        setupLayout();

        return contentView;
    }

    private void getDatas() {
        Network.getInstance().getService(uuid)
                .subscribe(new Subscriber<List<ServiceItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(List<ServiceItem> serviceItems) {

                        if (serviceItems != null && serviceItems.size() > 0) {
                            datas.clear();
                            datas.addAll(serviceItems);
                            mAdapter.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void setupLayout() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), linearLayoutManager
                .getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(mAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                doRefresh();
            }
        });

    }

    public class ServiceVH extends RecyclerView.ViewHolder {


        @BindView(R.id.clickArea)
        View clickArea;
        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.price)
        TextView tvPrice;

        public ServiceVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class ServiceAdapter extends RecyclerView.Adapter<ServiceVH> {

        @Override
        public ServiceVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ServiceVH(View.inflate(parent.getContext(), R.layout.item_service, null));
        }

        @Override
        public void onBindViewHolder(ServiceVH holder, int position) {

            final ServiceItem item = datas.get(position);

            holder.tvPrice.setText(item.price);
            holder.tvTitle.setText(item.title);
            holder.clickArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Fragment f = new ReservCompleteFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("ID", item.id);
                    bundle.putString("PRICE", item.price);
                    bundle.putString("TITLE", item.title);
                    bundle.putString("UUID", uuid);
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

    private void doRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
    }

    @OnClick(R.id.back)
    public void back(View view) {
        getActivity().onBackPressed();
    }

    @Override
    public void onRefresh() {
        getDatas();
    }
}
