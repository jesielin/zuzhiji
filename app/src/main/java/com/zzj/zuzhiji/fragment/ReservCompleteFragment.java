package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.zzj.zuzhiji.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 17/4/10.
 */

public class ReservCompleteFragment extends Fragment {

    private String title;
    private String price;
    private String service_id;
    private String uuid;

    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.price)
    TextView tvPrice;

    SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {
        @Override
        public void onCancelled() {

        }

        @Override
        public void onDateTimeRecurrenceSet(SelectedDate selectedDate,
                                            int hourOfDay, int minute,
                                            SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                            String recurrenceRule) {

//            mSelectedDate = selectedDate;
//            mHour = hourOfDay;
//            mMinute = minute;
//            mRecurrenceOption = recurrenceOption != null ?
//                    recurrenceOption.name() : "n/a";
//            mRecurrenceRule = recurrenceRule != null ?
//                    recurrenceRule : "n/a";
//
//            updateInfoView();
//
//            svMainContainer.post(new Runnable() {
//                @Override
//                public void run() {
//                    svMainContainer.scrollTo(svMainContainer.getScrollX(),
//                            cbAllowDateRangeSelection.getBottom());
//                }
//            });
        }
    };

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
        // DialogFragment to host SublimePicker
        SublimePickerFragment pickerFrag = new SublimePickerFragment();
        pickerFrag.setCallback(mFragmentCallback);

        pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        pickerFrag.show(getChildFragmentManager(), "SUBLIME_PICKER");
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


}
