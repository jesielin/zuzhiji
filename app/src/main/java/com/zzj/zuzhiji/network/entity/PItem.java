package com.zzj.zuzhiji.network.entity;

import com.bigkoo.pickerview.model.IPickerViewData;

import java.util.List;

/**
 * {
 * "Pid": 0,
 * "Id": 110000,
 * "Name": "北京市"
 * }
 * Created by shawn on 2017-05-05.
 */

public class PItem implements IPickerViewData {
    public String Pid;
    public String Id;
    public String Name;
    public List<CItem> city;

    @Override
    public String getPickerViewText() {
        return Name;
    }
}
