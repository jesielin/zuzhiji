package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.zzj.zuzhiji.R;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 17/4/10.
 */

public class ReservCompleteFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private String title;
    private String price;
    private String service_id;
    private String uuid;

    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.price)
    TextView tvPrice;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_reserv_complete, null);
        ButterKnife.bind(this, contentView);

        resolveArgs();

        return contentView;
    }

    @OnClick(R.id.choose_date)
    public void choose_date(View view) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                ReservCompleteFragment.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
    }

    private void resolveArgs() {
        Bundle arguments = getArguments();
        service_id = arguments.getString("ID");
        uuid = arguments.getString("UUID");
        price = arguments.getString("PRICE");
        title = arguments.getString("TITLE");

        tvTitle.setText(title);
        tvPrice.setText(price);

    }

    @OnClick(R.id.back)
    public void back(View view) {
        getActivity().onBackPressed();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

    }
}
