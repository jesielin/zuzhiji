package com.zzj.zuzhiji.network.entity;

import com.bigkoo.pickerview.model.IPickerViewData;

/**
 * Created by shawn on 2017-05-08.
 */

public class CItem implements IPickerViewData {
    public String Pid;
    public String Id;
    public String Name;

    @Override
    public String getPickerViewText() {
        return Name;
    }
}
