package com.worldlink.locker.common;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.worldlink.locker.services.BleDeviceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cn.qqtheme.framework.picker.OptionPicker;

/**
 * 星座选择器
 *
 * @author 李玉江[QQ :1032694760]
 * @version 2015 /12/15
 */
public class DevicePicker extends OptionPicker {

    /**
     * Instantiates a new Constellation picker.
     *
     * @param activity the activity
     */


    public List<BleDeviceInfo> deviceList;

    public void setDeviceList(List<BleDeviceInfo> devs) {
        Log.i("zhangfan", Thread.currentThread().getName());
        this.options.clear();
        this.deviceList = null;
        if (devs == null || devs.size() == 0) {
            return;
        }
        List<BleDeviceInfo> dst = Arrays.asList(new BleDeviceInfo[devs
                .size()]);
        Collections.copy(dst, devs);
        this.deviceList = dst;
        String[] devAddress = new String[devs.size()];
        for (int i = 0; i < deviceList.size(); i++) {
            devAddress[i] = deviceList.get(i).getBluetoothDevice().getAddress();
        }
        this.options.addAll(Arrays.asList(devAddress));
    }

    public BleDeviceInfo getBluetoothDeviceByAddr(String addr) {
        if (deviceList == null) {
            return null;
        }
        for (BleDeviceInfo device : deviceList) {
            if (device.getBluetoothDevice().getAddress() == addr) {
                return device;
            }
        }
        return null;
    }


    public DevicePicker(Activity activity) {

        super(activity, new String[]{""});
      /*  super(activity, new String[]{
                "水瓶",
                "双鱼",
                "白羊",
                "金牛",
                "双子",
                "巨蟹",
                "狮子",
                "处女",
                "天秤",
                "天蝎",
                "射手",
                "摩羯",
        });
        setLabel("座");*/
    }

}
