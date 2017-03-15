package com.worldlink.locker.common;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;


import com.worldlink.locker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2015/8/29.
 */
public class BleDeviceManager {

    private List<BleDeviceInfo> mDeviceInfoList = new ArrayList<>();

    //TODO LIST：初始化从string.xml中获得过滤列表
    private String[] mDeviceFilter;

    public static final int NO_DEVICE = -1;

    //设置正在连接的设备序号
    private int mConnIndex = NO_DEVICE;

    private Context context;


    public BleDeviceManager(Context context) {

        Resources res = context.getResources();
        mDeviceFilter = res.getStringArray(R.array.device_filter);
    }

    private boolean checkDeviceFilter(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        int n = mDeviceFilter.length;
        if (n > 0) {
            boolean found = false;
            for (int i = 0; i < n && !found; i++) {
                found = device.getName().equals(mDeviceFilter[i]);
            }
            return found;
        } else {
            return true;
        }
    }

    private boolean deviceInfoExists(String address) {
        for (int i = 0; i < mDeviceInfoList.size(); i++) {
            if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress().equals(address)) {
                return true;
            }
        }
        return false;
    }

    public void updateOrAdd(BluetoothDevice device, int rssi) {

        if (checkDeviceFilter(device)) {
            if (!deviceInfoExists(device.getAddress())) {
                BleDeviceInfo dev = new BleDeviceInfo(device, rssi);
                mDeviceInfoList.add(dev);
            } else {
                BleDeviceInfo deviceInfo = findDeviceInfo(device);
                deviceInfo.updateRssi(rssi);
            }

        }

    }

    public List getDevices() {
        return mDeviceInfoList;
    }

    public int getNumOfDevice() {
        return mDeviceInfoList.size();
    }

    private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
        for (int i = 0; i < mDeviceInfoList.size(); i++) {
            if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress().equals(device.getAddress())) {
                return mDeviceInfoList.get(i);
            }
        }
        return null;
    }

    public void clear() {
        mDeviceInfoList.clear();
    }

    public int getmConnIndex() {
        return mConnIndex;
    }

    public void setmConnIndex(int mConnIndex) {
        this.mConnIndex = mConnIndex;
    }

    public void setmConnIndex(BleDeviceInfo bleDeviceInfo) {
        for (int i = 0; i < mDeviceInfoList.size(); i++) {
            if (bleDeviceInfo == mDeviceInfoList.get(i)) {
                this.mConnIndex = i;
            }
        }

    }
}
