package com.worldlink.locker.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.bl.ArrowDirection;
import com.daasuu.bl.BubbleLayout;
import com.daasuu.bl.BubblePopupHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;
import com.worldlink.locker.R;
import com.worldlink.locker.UpdateService;
import com.worldlink.locker.common.BleDeviceInfo;
import com.worldlink.locker.common.BleDeviceManager;
import com.worldlink.locker.common.DevicePicker;
import com.worldlink.locker.common.HeWeather5Bean;
import com.worldlink.locker.common.ImageLoadTool;
import com.worldlink.locker.common.WeatherList;
import com.worldlink.locker.http.ApiClent;
import com.worldlink.locker.services.BluetoothLeService;
import com.worldlink.locker.services.Sensor;
import com.worldlink.locker.services.SensorTag;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.qqtheme.framework.picker.OptionPicker;


@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity {


    //added by Stevens
//    private SinkView sv_progress;
    private int[] bg_ids = {R.drawable.bg_hch0,
            R.drawable.bg_pm25,
            R.drawable.bg_2};

    @ViewById
    public ImageView iv_background;

    @ViewById
    public ImageButton ib_weather;

    @ViewById
    public ImageView iv_emotion;

    @ViewById
    public ImageButton ib_setting;

    @ViewById
    public ImageButton ib_device;

    @ViewById
    public TextView tv_feel;

    @ViewById
    public TextView tv_temp;

    @ViewById
    public TextView tv_temp_eval;

    @ViewById
    public TextView tv_hum;

    @ViewById
    public TextView tv_hum_eval;

    @ViewById
    public TextView tv_pm1;

    @ViewById
    public TextView tv_pm1_eval;


    @ViewById
    public TextView tv_cell;

    @ViewById
    public ImageView ic_battery;

    @ViewById
    public RelativeLayout rl_middle;
    @ViewById
    public RelativeLayout rl_left;
    @ViewById
    public RelativeLayout rl_right;

    @ViewById
    public TextView tv_index_left;
    @ViewById
    public TextView tv_index_right;
    @ViewById
    public TextView tv_index_middle;

    @ViewById
    public TextView tv_title_left;
    @ViewById
    public TextView tv_title_middle;
    @ViewById
    public TextView tv_title_right;


    @ViewById
    public TextView tv_unit_left;
    @ViewById
    public TextView tv_unit_middle;
    @ViewById
    public TextView tv_unit_right;

    @ViewById
    public ImageView iv_circle_left;
    @ViewById
    public ImageView iv_circle_middle;
    @ViewById
    public ImageView iv_circle_right;


    private static final String TAG = "MainActivity";

    //蓝牙相关操作
    private BluetoothLeService mBluetoothLeService = null;
    private volatile boolean mScanning = false;
    private BluetoothAdapter mBtAdapter = null;
    private boolean mInitialised = false;
    private IntentFilter mFilter;
    private boolean mBleSupported = true;
    private static BluetoothManager mBluetoothManager;
    private BleDeviceManager bleDeviceManager;
    private BluetoothDevice mBluetoothDevice = null;
    private static final int REQ_ENABLE_BT = 0;
    private boolean isConnected = false;


    //建立连接后
    private BluetoothGatt mBtGatt = null;
    private boolean mIsReceiving = false;
    private boolean mServicesRdy = false;
    private List<BluetoothGattService> mServiceList = new ArrayList<BluetoothGattService>();
    private static final int GATT_TIMEOUT = 100; // milliseconds
    private List<Sensor> mEnabledSensors = new ArrayList<Sensor>();
/*
    private BluetoothLeService mBluetoothLeService = BluetoothLeService.getInstance();
*/

    private PopupWindow weatherPopupWindow;
    private PopupWindow pm25PopupWindow;
    private String ipAddr;
    private ImageLoadTool imageLoadTool = new ImageLoadTool();

    private ApiClent.ClientCallback netIpCallback = new ApiClent.ClientCallback() {
        @Override
        public void onSuccess(Object data) {

            try {
                String sData = data.toString();
                // 从反馈的结果中提取出IP地址
                int start = sData.indexOf("{");
                int end = sData.indexOf("}");
                String json = sData.substring(start, end + 1);
                if (json != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        ipAddr = jsonObject.optString("cip");
                        ApiClent.getWeather(MainActivity.this,ipAddr,
                                MainActivity.this.weatherCallback);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                //todo list:更新界面内容
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(String message) {

        }

        @Override
        public void onError(Exception e) {
            Log.d(TAG, "" + e);
            showProgressBar(false);
            showMiddleToast("服务器连接失败");

        }
    };
    private ApiClent.ClientCallback weatherCallback = new ApiClent.ClientCallback() {
        @Override
        public void onSuccess(Object data) {

            try {
                String sData = data.toString();
                WeatherList weatherList = WeatherList.fromJson(sData);
                HeWeather5Bean heWeather5 = weatherList.getHeWeather5().get(0);
                if (heWeather5.getStatus().equals("ok")) {
                    String city = heWeather5.getBasic().getCity();
                    String code = heWeather5.getNow().getCond().getCode();
                    String pm25 = heWeather5.getAqi().getCity().getPm25();
                    String pm10 = heWeather5.getAqi().getCity().getPm10();
                    String tmp = heWeather5.getNow().getTmp();
                    String hum = heWeather5.getNow().getHum();
                    String qlt = heWeather5.getAqi().getCity().getQlty();
                    String time = heWeather5.getBasic().getUpdate().getLoc();
                    MainActivity.this.updateCurWeather(city,code,tmp,
                            hum,pm25,pm10,qlt,time);
                }

                //todo list:更新界面内容
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(String message) {

        }

        @Override
        public void onError(Exception e) {
            Log.d(TAG, "" + e);
            showProgressBar(false);
            showMiddleToast("服务器连接失败");

        }
    };


    private void updateCurWeather(String city,String code,
                                  String temp, String humi,
                                  String pm25,String pm10,
                                  String qual,
                                  String time) {
        TextView tvWetrCity = (TextView)this.weatherPopupWindow.getContentView().findViewById(R.id.tv_wetr_city);
        ImageView tvWetrCode =(ImageView) this.weatherPopupWindow.getContentView().findViewById(R.id.iv_wetr_code);
        TextView tvWetrTemp= (TextView)this.weatherPopupWindow.getContentView().findViewById(R.id.tv_wetr_temp);
        TextView tvWetrHumi= (TextView)this.weatherPopupWindow.getContentView().findViewById(R.id.tv_wetr_humi);
        TextView tvWetrPM25 = (TextView)this.weatherPopupWindow.getContentView().findViewById(R.id.tv_wetr_pm25);
        TextView tvWetrPm10 = (TextView)this.weatherPopupWindow.getContentView().findViewById(R.id.tv_wetr_pm10);
        TextView tvWetrAq = (TextView)this.weatherPopupWindow.getContentView().findViewById(R.id.tv_wetr_aq);
        TextView tvWetrTime = (TextView)this.weatherPopupWindow.getContentView().findViewById(R.id.tv_wetr_time);

        tvWetrCity.setText(city);
        imageLoadTool.loadImage(tvWetrCode, "http://files.heweather.com/cond_icon/"
                + code + ".png", (DisplayImageOptions)null);
        imageLoadTool.loadImage(this.ib_weather, "http://files.heweather.com/cond_icon/"
                + code + ".png", (DisplayImageOptions) null);
        tvWetrHumi.setText("湿度: " + humi + "%");
        tvWetrTemp.setText("温度："+temp+"°C");
        tvWetrPM25.setText("PM2.5: "+pm25);
        tvWetrPm10.setText("pm10: "+pm10);
        tvWetrAq.setText("空气指数:"+qual);
        tvWetrTime.setText(time);

    }


    private Animation anim_1;
    private Animation anim_2;
    private boolean transition = false;

    @Click
    public void rl_left() {
        if(transition){
            //animation hasn't finished
            return;
        }
        transition = true;
        this.rl_left.setVisibility(View.INVISIBLE);
        rl_middle.setVisibility(View.INVISIBLE);
        anim_1 = AnimationUtils.loadAnimation(MainActivity.this,
                R.anim.scale_down_right_top);
        Animation anim_tmp = AnimationUtils.loadAnimation(MainActivity.this,
                R.anim.scale_down_left_bottom);
        this.rl_left.startAnimation(anim_1);
        rl_middle.startAnimation(anim_tmp);

        anim_1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                anim_1 = null;
                //switch content
                String str = (String) tv_index_left.getText();
                tv_index_left.setText(tv_index_middle.getText());
                tv_index_middle.setText(str);

                str = (String) tv_title_left.getText();
                tv_title_left.setText(tv_title_middle.getText());
                tv_title_middle.setText(str);

                str = (String) tv_unit_left.getText();
                tv_unit_left.setText(tv_unit_middle.getText());
                tv_unit_middle.setText(str);

               /* int value_left = Integer.valueOf((String) tv_index_left.getText());
                int value_middle=  Integer.valueOf((String) tv_index_middle.getText());

                int offset_left = 255 - value_left;
                int offset_middle = 255 - value_middle;
                GradientDrawable drawable_left = (GradientDrawable) iv_circle_left.getBackground();
                drawable_left.setColor(Color.argb(255, offset_left, value_left, 0));
                GradientDrawable drawable_middle = (GradientDrawable) iv_circle_middle.getBackground();
                drawable_middle.setColor(Color.argb(255, offset_middle, value_middle, 0));
*/
                anim_2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_up_right_top);
                anim_2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        rl_left.setVisibility(View.VISIBLE);
                        rl_middle.setVisibility(View.VISIBLE);
                        anim_2 = null;
                        transition = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                rl_left.startAnimation(anim_2);
                Animation anim_tmp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_up_left_bottom);
                rl_middle.startAnimation(anim_tmp);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });
    }

    @Click
    public void rl_right() {
        if(transition){
            //animation hasn't finished
            return;
        }
        transition = true;
        this.rl_right.setVisibility(View.INVISIBLE);
        rl_middle.setVisibility(View.INVISIBLE);
        anim_1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_down_left_top);
        Animation anim_tmp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_down_right_bottom);
        this.rl_right.startAnimation(anim_1);
        rl_middle.startAnimation(anim_tmp);

        anim_1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                anim_1 = null;
                //switch content
                String str = (String) tv_index_right.getText();
                tv_index_right.setText(tv_index_middle.getText());
                tv_index_middle.setText(str);

                str = (String) tv_title_right.getText();
                tv_title_right.setText(tv_title_middle.getText());
                tv_title_middle.setText(str);

                str = (String) tv_unit_right.getText();
                tv_unit_right.setText(tv_unit_middle.getText());
                tv_unit_middle.setText(str);

                /*
                int value_right = Integer.valueOf((String) tv_index_right.getText());
                int value_middle=  Integer.valueOf((String) tv_index_middle.getText());
                int offset_right = 255 - value_right;
                int offset_middle = 255 - value_middle;
                GradientDrawable drawable_right = (GradientDrawable) iv_circle_right.getBackground();
                drawable_right.setColor(Color.argb(255, offset_right, value_right, 0));
                GradientDrawable drawable_middle = (GradientDrawable) iv_circle_middle.getBackground();
                drawable_middle.setColor(Color.argb(255, offset_middle, value_middle, 0));
*/

                anim_2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_up_left_top);
                anim_2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        rl_right.setVisibility(View.VISIBLE);
                        rl_middle.setVisibility(View.VISIBLE);
                        anim_2 = null;
                        transition = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                rl_right.startAnimation(anim_2);
                Animation anim_tmp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_up_right_bottom);
                rl_middle.startAnimation(anim_tmp);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });
    }


    private void pushInXiaomi() {
        Context context = getApplicationContext();
        Intent service = new Intent(context, XGPushService.class);
        context.startService(service);
    }

    DevicePicker devicePicker;

    @AfterViews
    public void init() {

        //TODO LIST：初始化蓝牙功能
        bleDeviceManager = new BleDeviceManager(this);
        //判断蓝牙设备是否可用
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
            mBleSupported = false;
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to BluetoothAdapter through BluetoothManager.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = mBluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBtAdapter == null) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_LONG).show();
            mBleSupported = false;
        }

        mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);

        if (!mInitialised) {
            // Broadcast receiver
            registerReceiver(mReceiver, mFilter);
            if (mBtAdapter.isEnabled()) {
                startBluetoothLeService();
            } else {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQ_ENABLE_BT);
            }
            mInitialised = true;
        } else {
            ib_device.setImageResource(R.drawable.dv_disconneted);
        }
        ib_device.setImageResource(R.drawable.dv_disconneted);
        XGPushManager.registerPush(this, "*");
        updateNotifyService();
        pushInXiaomi();
        startUpdateService();

        //=============================================================

        ApiClent.getNetIp(this, netIpCallback);

        iv_circle_middle.setBackgroundResource(R.drawable.circle5);
        iv_circle_left.setBackgroundResource(R.drawable.circle5);
        iv_circle_right.setBackgroundResource(R.drawable.circle5);

        iv_background.setImageResource(bg_ids[2]);
        this.showResult((float) 25.5, (float) 20.0, (float) 1, (float) 0, (float) 100,
                (float) 0.01, (float) 2);
        final BubbleLayout bubbleLayout = (BubbleLayout) LayoutInflater.from(this).
                inflate(R.layout.layout_weather_bubble, null);
        weatherPopupWindow = BubblePopupHelper.create(this, bubbleLayout);
        ib_weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                v.getLocationInWindow(location);
                if (ipAddr != null) {
                    ApiClent.getWeather(MainActivity.this, ipAddr, weatherCallback);
                }
                bubbleLayout.setArrowDirection(ArrowDirection.TOP);
                weatherPopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], v.getHeight() + location[1]);
            }
        });

        final BubbleLayout bubbleLayout1 = (BubbleLayout) LayoutInflater.from(this).
                inflate(R.layout.layout_pm25_standard, null);
        pm25PopupWindow = BubblePopupHelper.create(this, bubbleLayout1);
        tv_pm1_eval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                v.getLocationInWindow(location);
                bubbleLayout.setArrowDirection(ArrowDirection.RIGHT);
                pm25PopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], v.getHeight() + location[1]);
            }
        });

        //初始化设备列表
        devicePicker = new DevicePicker(this);
        devicePicker.setAnimationStyle(R.style.Animation_CustomPopup);
        devicePicker.setOffset(1);//偏移量
        devicePicker.setTitleText("设备列表");
        devicePicker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
            @Override
            public void onOptionPicked(String option) {

                BleDeviceInfo bleDeviceInfo = devicePicker.getBluetoothDeviceByAddr(option);
                if (mScanning)
                    stopScan();
                mBluetoothDevice = bleDeviceInfo.getBluetoothDevice();
                if (bleDeviceManager.getmConnIndex() == BleDeviceManager.NO_DEVICE) {
                    bleDeviceManager.setmConnIndex(bleDeviceInfo);
                    onConnect();
                } else {
                    if (bleDeviceManager.getmConnIndex() != BleDeviceManager.NO_DEVICE) {
                        mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
                    }
                }
            }
        });

    }

    private void startUpdateService() {
        Intent intent = new Intent(this, UpdateService.class);
        intent.putExtra(UpdateService.EXTRA_BACKGROUND, true);
        intent.putExtra(UpdateService.EXTRA_WIFI, true);
        intent.putExtra(UpdateService.EXTRA_DEL_OLD_APK, true);
        startService(intent);
    }

    //TODO LIST:仅仅用于测试
    private void updateNotifyService() {
        boolean needPush = true;
        if (needPush) {
            XGPushManager.registerPush(this, "shit");
        } else {
            XGPushManager.registerPush(this, "*");
        }
    }
    @Override
    public void onResume() {

        // TODO Auto-generated method stub
        Log.d(TAG, "onResume");
        if (!mIsReceiving) {
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            mIsReceiving = true;
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        if (mIsReceiving) {
            unregisterReceiver(mGattUpdateReceiver);
            mIsReceiving = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startScan();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy");

        //注销数据监听listenner
        if (mIsReceiving) {
            unregisterReceiver(mGattUpdateReceiver);
            mIsReceiving = false;
        }

        if (mBluetoothDevice!=null && mBluetoothManager.getConnectionState(mBluetoothDevice, BluetoothGatt.GATT) ==
                BluetoothGatt.STATE_CONNECTED) {
            mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
        }

        //注销蓝牙服务
        if (mBluetoothLeService != null) {
            scanLeDevice(false);
            mBluetoothLeService.close();
            unregisterReceiver(mReceiver);
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
        super.onDestroy();
    }

/**
 * =========================bluetoth operation------------------------------
 */

    /**
     * 开启蓝牙服务
     */
    private void startBluetoothLeService() {

        boolean f;
        Intent bindIntent = new Intent(this, BluetoothLeService.class);
        startService(bindIntent);
        f = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (f)
            Log.d(TAG, "BluetoothLeService - success");
        else {
            Toast.makeText(MainActivity.this, "BluetoothLeService-falied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize BluetoothLeService");
                finish();
                return;
            }
            final int n = mBluetoothLeService.numConnectedDevices();
            if (n > 0) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        Log.e(TAG, "Multiple connections!");

                    }
                });
            } else {
                startScan();
                Log.i(TAG, "BluetoothLeService connected");
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.i(TAG, "BluetoothLeService disconnected");
        }
    };

    void onConnect() {
        if (bleDeviceManager.getNumOfDevice() > 0) {
            int connState = mBluetoothManager.getConnectionState(mBluetoothDevice, BluetoothGatt.GATT);
            switch (connState) {
                case BluetoothGatt.STATE_CONNECTED:
                    mBluetoothLeService.disconnect(null);
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    boolean ok = mBluetoothLeService.connect(mBluetoothDevice.getAddress());
                    if (!ok) {
                        Toast.makeText(MainActivity.this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    Toast.makeText(MainActivity.this, "设备繁忙", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /*
	开始扫描
	 */
    private void startScan() {
        // Start device discovery
        if (mBleSupported) {
            bleDeviceManager.clear();
            //TODO LIST:predator
            devicePicker.setDeviceList(null);
            scanLeDevice(true);
            //TODO LIST:修改连接的状态
            // new ConnectDemoTask().execute();
            if (!mScanning) {
                Log.e(TAG, "bluetooth device scanning falied");
            }
        } else {
            Toast.makeText(MainActivity.this, "BLE not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScan() {
        mScanning = false;
        scanLeDevice(false);
    }

    /**
     * 处理三类action
     * ACTION_STATE_CHANGED：蓝牙设备状态变化（开启，关闭）
     * ACTION_GATT_CONNECTED：连接上设备
     * ACTION_GATT_DISCONNECTED：设备断开
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                // Bluetooth adapter state change
                switch (mBtAdapter.getState()) {
                    case BluetoothAdapter.STATE_ON:
                        bleDeviceManager.setmConnIndex(BleDeviceManager.NO_DEVICE);
                        startBluetoothLeService();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(context, "蓝牙已关闭", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Log.w(TAG, "Action STATE CHANGED not processed ");
                        break;
                }
            }
            else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
                if (status == BluetoothGatt.GATT_SUCCESS)
                {
                    isConnected = true;
                    // Create GATT object
                    mBtGatt = BluetoothLeService.getBtGatt();

                    if (!mIsReceiving) {
                        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                        mIsReceiving = true;
                    }

                    // Start service discovery
                    if (!mServicesRdy && mBtGatt != null) {
                        if (mBluetoothLeService.getNumServices() == 0)
                            //TODO LIST：初始化后即开始扫描服务
                            discoverServices();
                        else
                            displayServices();
                    }
                }
                else {
                    Log.e(TAG, "connection failed" + status);
                }
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                // GATT disconnect
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);

                if (status == BluetoothGatt.GATT_SUCCESS)
                {
/*
                    isConnected = false;
*/
                    Log.i(TAG, "Disconnect device:" + mBluetoothDevice.getName());

                } else {
                    Log.e(TAG, "Disconnect failed. Status: " + status);
                }
                bleDeviceManager.setmConnIndex(BleDeviceManager.NO_DEVICE);
                mBluetoothLeService.close();
                ib_device.setImageResource(R.drawable.dv_disconneted);
                Toast.makeText(getApplication(), "设备连接已断开，请重连", Toast.LENGTH_LONG).show();
/*
                startScan();//重新扫描设备
*/

            } else {
                Log.w(TAG,"Unknown action: " + action);
            }
        }
    };


    private Handler mScanHandler = new Handler();
    private int SCAN_PERIOD = 20000;
    private boolean scanLeDevice(boolean enable) {
        if (enable) {
            mScanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBtAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = mBtAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBtAdapter.stopLeScan(mLeScanCallback);
        }
        return mScanning;
    }

    // Device scan callback.
    // NB! Nexus 4 and Nexus 7 (2012) only provide one scan result per scan
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                public void run() {
                    // Filter devices
                    bleDeviceManager.updateOrAdd(device, rssi);
                    devicePicker.setDeviceList(bleDeviceManager.getDevices());

                    if (!devicePicker.isShowing() && devicePicker.deviceList!=null &&
                            devicePicker.deviceList.size() != 0) {
                        devicePicker.show();
                    }
                }

            });
        }
    };



    /**
     * ---------------------------after connection----------------------------
     */

    /**
     * discover the services after connection
     */
    private void discoverServices() {
        if (mBtGatt.discoverServices()) {
            Log.i(TAG, "START SERVICE DISCOVERY");
            mServiceList.clear();
        } else {
            Log.i(TAG, "Faild SERVICE DISCOVERY");
        }
    }

    private void displayServices() {
        mServicesRdy = true;
        try {
            mServiceList = mBluetoothLeService.getSupportedGattServices();
        } catch (Exception e) {
            e.printStackTrace();
            mServicesRdy = false;
        }

        // Characteristics descriptor readout done
        if (mServicesRdy) {
/*
			setStatus("Service discovery complete");s
*/
            //TODO LIST：初始化后，扫描到服务
            enableSensors(true);
            enableNotifications(true);
          // enableNotificationForLock(true);
         //   enableNotificationForAir(true);
            ib_device.setImageResource(R.drawable.dv_connected);
          Toast.makeText(getApplication(), "设备已连接", Toast.LENGTH_LONG).show();

        }
        else {
/*
            setError("Failed to read services");
*/
        }
    }

    private void enableNotifications(boolean enable) {
        for (Sensor sensor : mEnabledSensors) {
            //TODO LIST：data也是一个单独的characteristic
            UUID servUuid = sensor.getService();
            UUID dataUuid = sensor.getData();
            BluetoothGattService serv = mBtGatt.getService(servUuid);
            BluetoothGattCharacteristic charac = serv.getCharacteristic(dataUuid);

            mBluetoothLeService.setCharacteristicNotification(charac, enable);
            mBluetoothLeService.waitIdle(GATT_TIMEOUT);
        }
    }

    private void enableSensors(boolean enable) {
        for (Sensor sensor : mEnabledSensors) {
            UUID servUuid = sensor.getService();
            UUID confUuid = sensor.getConfig();

            // Skip keys
            if (confUuid == null)
                break;

            // Barometer calibration
            if (confUuid.equals(SensorTag.UUID_BAR_CONF) && enable) {
              //  calibrateBarometer();
            }

            BluetoothGattService serv = mBtGatt.getService(servUuid);
            BluetoothGattCharacteristic charac = serv.getCharacteristic(confUuid);

            //TODO LIST：除了Gyroscope：0：disable，7 enable，其余的均是0disable 1 enable
            byte value =  enable ? sensor.getEnableSensorCode() : Sensor.DISABLE_SENSOR_CODE;
            mBluetoothLeService.writeCharacteristic(charac, value);
            mBluetoothLeService.waitIdle(GATT_TIMEOUT);

        }

    }

    private void enableNotificationForAir(boolean enable) {

        //UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
        UUID RX_SERVICE_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
        BluetoothGattService RxService = mBtGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            return;
        }


        UUID TX_CHAR_UUID = UUID.fromString("0000fffa-0000-1000-8000-00805f9b34fb");
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            return;
        }

        mBtGatt.setCharacteristicNotification(TxChar,true);

        UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        if (descriptor == null) {
            return;
        }
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBtGatt.writeDescriptor(descriptor);

    }


    private void enableNotificationForLock(boolean enable) {

        UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
        BluetoothGattService RxService = mBtGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            return;
        }


        UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            return;
        }

        mBtGatt.setCharacteristicNotification(TxChar,true);

        UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        if (descriptor == null) {
            return;
        }
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBtGatt.writeDescriptor(descriptor);

    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //TODO LIST：显示当前的服务列表信息
                    displayServices();
                    //checkOad();
                } else {
                    //  Toast.makeText(getApplication(), "Service discovery failed", Toast.LENGTH_LONG).show();
                    return;
                }
            } else if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
                // Notification
                // char[] dd = intent.getCharArrayExtra(BluetoothLeService.EXTRA_DATA);
               // short[] ds = intent.getShortArrayExtra(BluetoothLeService.EXTRA_DATA);
                final byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                //TODO LIST:注销当前的值
                MainActivity.this.onCharacteristicChanged(uuidStr, value);
                Log.i("进入Notify方法", "ACTION_DATA_NOTIFY");

            } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
                // Data written
                String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                onCharacteristicWrite(uuidStr,status);
                Log.i("进入Write方法", "ACTION_DATA_WRITE");

            } else if (BluetoothLeService.ACTION_DATA_READ.equals(action)) {
                // Data read
                String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                byte  [] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                onCharacteristicsRead(uuidStr,value,status);
                Log.i("进入Read方法", "ACTION_DATA_READ");

            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
            }
        }
    };

    private void onCharacteristicWrite(String uuidStr, int status) {
        Log.d(TAG,"onCharacteristicWrite: " + uuidStr);
    }

    private void onCharacteristicChanged(String uuidStr, final byte[] value) {
        //TODO LIST:更新界面
        this.runOnUiThread(new Runnable() {
            public void run() {
                try {
/*
                    byte temp = value[1];
*/
                    byte[] temp = {value[4], value[3], value[2], value[1]};
                    DataInputStream dis = new DataInputStream(new
                            ByteArrayInputStream(temp));
                    float fTemp = dis.readFloat();
                    BigDecimal b = new BigDecimal(fTemp);
                    fTemp = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                    dis.close();

                    byte humi = value[5];

                    byte hiPm1 = value[6];
                    byte lowPm1 = value[7];
                    float pm1 = hiPm1 * 256 + lowPm1;

                    byte hiPm25 = value[8];
                    byte lowPm25 = value[9];
                    float pm25 = hiPm25 * 256 + lowPm25;

                    byte hiPm10 = value[10];
                    byte lowPm10 = value[11];
                    float pm10 = hiPm10 * 256 + lowPm10;

                    byte hiHcho = value[12];
                    byte lowHcho = value[13];
                    float hcho = (float) ((hiHcho * 256 + lowHcho) / 1000.0);
                    BigDecimal b2 = new BigDecimal(hcho);
                    hcho = b2.setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
                    byte cell = value[14];
                    showResult(fTemp, humi, pm1, pm25, pm10, hcho, cell);
                } catch (Exception e) {
                }
            }
        });
    }

    private void changeBackgroundImage(float hcho, float pm25) {
        if (hcho >= 0.5) {
            iv_background.setImageResource(bg_ids[0]);
        } else if (pm25 >= 115) {
            iv_background.setImageResource(bg_ids[1]);
        } else {
            iv_background.setImageResource(bg_ids[2]);
        }
    }
    private void showResult(float temp, float humi, float pm1,
                            float pm, float pm10,float hcho, float cell) {

        this.changeBackgroundImage(hcho, pm);

        this.tv_cell.setText((int) cell + "%");
        if (cell > 75) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_full);
        } else if (cell <= 75 && cell > 60) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_34);

        } else if (cell <= 60 && cell > 50) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_half);

        } else if (cell <= 50 && cell > 20) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_14);
        } else {
            this.ic_battery.setImageResource(R.drawable.ic_battery_low);
        }
        //甲醛：<0.1 [0.1,0.5),[0.5,0.6),[0.6.. 正常，轻度污染，污染，重度污染
        GradientDrawable drawable = (GradientDrawable) iv_circle_middle.getBackground();
        if (hcho < 0.1) {
            drawable.setColor(Color.argb(255, 0, 255, 0));
            this.tv_unit_middle.setText("正常");
        } else if (hcho >= 0.1 && hcho < 0.5) {
            drawable.setColor(Color.argb(255, 50, 0, 0));
            this.tv_unit_middle.setText("轻度污染");

        } else if (hcho >= 0.5 && hcho < 0.6) {
            drawable.setColor(Color.argb(255, 100, 0, 0));
            this.tv_unit_middle.setText("污染");
        } else {
            drawable.setColor(Color.argb(255, 255, 0, 0));
            this.tv_unit_middle.setText("重度污染");
        }
        this.tv_index_middle.setText(hcho + "");

        /*
        pm10
         */
        this.tv_index_left.setText((int) pm10 + "");
        GradientDrawable drawable1 = (GradientDrawable) iv_circle_left.getBackground();
        if (pm10 < 150) {
            drawable1.setColor(Color.argb(255, 0, 255, 0));
        } else {
            drawable1.setColor(Color.argb(255, 255, 0, 0));
        }

         /*
        pm25
         */
        GradientDrawable drawable0 = (GradientDrawable) iv_circle_right.getBackground();
        this.tv_index_right.setText((int) pm + "");
        if (pm < 35) {
            drawable0.setColor(Color.argb(255, 0, 255, 0));
            this.tv_unit_right.setText("优");
        } else if (pm >= 35 && pm < 75) {
            drawable.setColor(Color.argb(255, 0, 0, 255));
            this.tv_unit_right.setText("良");
        } else if (pm >= 75 && pm < 115) {
            drawable.setColor(Color.argb(255, 50, 0, 0));
            this.tv_unit_right.setText("轻度污染");
        } else if (pm >= 115 && pm < 150) {
            drawable.setColor(Color.argb(255, 100, 0, 0));
            this.tv_unit_right.setText("中度污染");
        } else if (pm >= 150 && pm < 250) {
            drawable.setColor(Color.argb(255, 255, 0, 0));
            this.tv_unit_right.setText("重度污染");
        } else {
            drawable.setColor(Color.argb(255, 255, 0, 0));
            this.tv_unit_right.setText("严重污染");
        }

        /*PM1.0
        0-35 优 35-75良 75-115 轻度污染 115-150 中度污染 150-250 重度污染 250-500 严重污染
         */
        if (pm1 < 35) {
            this.tv_pm1_eval.setText("优");
        } else if (pm1 >= 35 && pm1 < 75) {
            this.tv_pm1_eval.setText("良");
        } else if (pm1 >= 75 && pm1 < 115) {
            this.tv_pm1_eval.setText("轻度污染");
        } else if (pm1 >= 115 && pm1 < 150) {
            this.tv_pm1_eval.setText("中度污染");
        } else if (pm1 >= 150 && pm1 < 250) {
            this.tv_pm1_eval.setText("重度污染");
        } else {
            this.tv_pm1_eval.setText("严重污染");
        }
        this.tv_pm1.setText((int) pm1 + "");


       /* 20 ~ 39 %: 干燥
        40 ~ 70 %: 舒适
        71 ~ 100 %: 潮湿*/
        this.tv_hum.setText((int) humi + "%");
        if (humi < 39) {
            this.tv_hum_eval.setText("干燥");
        } else if (humi >= 39 && humi < 70) {
            this.tv_hum_eval.setText("舒适");
        } else {
            this.tv_hum_eval.setText("潮湿");
        }

           /*    -10 ~ 17度: 冷
        18 ~ 23度: 舒适
        24 ~ 40度: 热
        41 ~ 50度:炙热*/
        this.tv_temp.setText((int) temp + "°C");
        if (temp < 17) {
            this.tv_temp_eval.setText("冷");
        } else if (temp >= 17 && temp < 23) {
            this.tv_temp_eval.setText("舒适");
        } else if (temp >= 23 && temp < 35) {
            this.tv_temp_eval.setText("适宜");
        } else {
            this.tv_temp_eval.setText("炎热");
        }


    }

    private void onCharacteristicsRead(String uuidStr, byte [] value, int status) {
        Log.i(TAG, "onCharacteristicsRead: " + uuidStr);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter fi = new IntentFilter();
        fi.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        fi.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
        fi.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        fi.addAction(BluetoothLeService.ACTION_DATA_READ);
        return fi;
    }

    private boolean sendMsg(String message) {
        byte[] value;
        try {
            //send data to service
            value = message.getBytes("UTF-8");
            //TODO LIST: define the service uuid
            UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
            BluetoothGattService RxService = mBtGatt.getService(RX_SERVICE_UUID);
            if (RxService == null) {
                return false;
            }
            //TODO LIST: define the characteristics uuid
            UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
            BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
            if (RxChar == null) {
                return false;
            }
            RxChar.setValue(value);
            boolean status = mBtGatt.writeCharacteristic(RxChar);
            return status;

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }



    /***
     * added by Stevens
     *
     * used for demo purose only
     */
    private class ProgressDemoTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i <= 255; i++) {
                publishProgress(Integer.valueOf(i));
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
//            sv_progress.setPercent(progress / (float) 100);
            int value = progress;//(int) (progress / (float) 100 * 255.0f);
            int offset = 255 - value;
            GradientDrawable drawable = (GradientDrawable) iv_circle_middle.getBackground();
            drawable.setColor(Color.argb(255, offset, value, 0));
            tv_index_middle.setText(String.valueOf(value));
            if( progress <= 20 ){
                tv_feel.setText("干爽");
            }
            else if(progress > 20 && progress <= 60){
                tv_feel.setText("舒适");
            }else if(progress > 60 && progress <= 100) {
                tv_feel.setText("潮湿");
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            iv_emotion.setVisibility(View.VISIBLE);

        }
    }//end of ProgressDemoTask



}//end of file
