package com.worldlink.locker.activity;

import android.app.Activity;
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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.SnackBar;
import com.rey.material.widget.Spinner;
import com.worldlink.locker.R;
import com.worldlink.locker.services.BleDeviceInfo;
import com.worldlink.locker.services.BleDeviceManager;
import com.worldlink.locker.services.BluetoothLeService;
import com.worldlink.locker.services.Sensor;
import com.worldlink.locker.services.SensorTag;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import cn.qqtheme.framework.picker.NumberPicker;
import cn.qqtheme.framework.picker.OptionPicker;
import rb.popview.PopField;

//import com.rey.material.widget.RelativeLayout;

//@NoTitle
//@Fullscreen
//@EActivity(R.layout.activity_main)
public class MainActivity_Design_3 extends BaseActivity {


//    @ViewById
      ImageButton ib_temp;
//
//    @ViewById
      Spinner spinner_label;
//
//    @ViewById
      SnackBar info_sn;

    //added by Stevens
//    private SinkView sv_progress;
    private int[] bg_ids = {R.drawable.bg_1,R.drawable.bg_1_blur,R.drawable.bg_2, R.drawable.bg_2_blur, R.drawable.bg_3, R.drawable.bg_3_blur, R.drawable.bg_4, R.drawable.bg_4_blur, R.drawable.bg_5,R.drawable.bg_6,R.drawable.bg_7,R.drawable.bg_8,};
    private int[] weather_ids = {R.drawable.ic_weather_04, R.drawable.ic_weather_clear, R.drawable.ic_weather_clear_cloudy, R.drawable.ic_weather_clear_rainy, R.drawable.ic_weather_clear_snowy, R.drawable.ic_weather_clear_storm, R.drawable.ic_weather_clear_windy, R.drawable.ic_weather_heavy_rain};
//    private TextView tv_index;
//    private ImageView iv_circle;
    private ImageView iv_background;
    private ImageButton ib_device;
    private ImageView iv_emotion;
    private ImageButton ib_settings;
    private ImageButton ib_ring;
    private TextView tv_feel;
    private MediaPlayer myMediaPlayer;
    private Vibrator vibrator;
    private boolean isRingOn = true;
    private boolean isConnected = false;
    private boolean unpressed = true;

    //added by Stevens
    //animated index indicator
    //middle widget
    private TextView tv_index_middle;
    private TextView tv_title_middle;
    private TextView tv_unit_middle;
    private RelativeLayout rl_middle;
    private ImageView iv_circle_middle;

    //left widget
    private TextView tv_index_left;
    private TextView tv_title_left;
    private TextView tv_unit_left;
    private RelativeLayout rl_left;
    private ImageView iv_circle_left;
    //right widget
    private TextView tv_index_right;
    private TextView tv_title_right;
    private TextView tv_unit_right;
    private RelativeLayout rl_right;
    private ImageView iv_circle_right;

    //weather report
    private ImageView iv_weather;

    private Animation anim_1;
    private Animation anim_2;
    private boolean transition = false;

    private static final String TAG = "MainActivity";

    //蓝牙相关操作
    private BluetoothLeService mBluetoothLeService = null;
    private boolean mScanning = false;
    private BluetoothAdapter mBtAdapter = null;
    private boolean mInitialised = false;
    private IntentFilter mFilter;
    private boolean mBleSupported = true;
    private static BluetoothManager mBluetoothManager;
    private BleDeviceManager bleDeviceManager;
    private BluetoothDevice mBluetoothDevice = null;
    private static final int REQ_ENABLE_BT = 0;

    //建立连接后
    private BluetoothGatt mBtGatt = null;
    private boolean mIsReceiving = false;
    private boolean mServicesRdy = false;
    private List<BluetoothGattService> mServiceList = new ArrayList<BluetoothGattService>();
    private static final int GATT_TIMEOUT = 100; // milliseconds
    private List<Sensor> mEnabledSensors = new ArrayList<Sensor>();

    private List<BleDeviceInfo> devices=new ArrayList<BleDeviceInfo>();

    //popView
    private PopField popField;

    BaseAdapter adapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            /**
             final LayoutInflater mInflater =getLayoutInflater();
             if (convertView == null) {
             holder = new ViewHolder();
             convertView = mInflater.inflate(R.layout.row_spn, parent, false);
             holder.address = (TextView) convertView.findViewById(R.id.row_spn_tv);
             convertView.setTag(holder);
             } else {
             holder = (ViewHolder) convertView.getTag();
             }
             final BleDeviceInfo data = (BleDeviceInfo) getItem(position);
             holder.address.setText(data.getBluetoothDevice().getAddress());
             return convertView;
             */

            //
            final LayoutInflater mInflater =getLayoutInflater();
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.row_spn, parent, false);
            holder.address = (TextView) convertView.findViewById(R.id.row_spn_tv);
            convertView.setTag(holder);
            final BleDeviceInfo data = (BleDeviceInfo) getItem(position);
            holder.address.setText(data.getBluetoothDevice().getAddress());
            return convertView;
        }
    };
    static class ViewHolder {
        TextView address;
    }

//    @AfterViews
    public void init() {


        //TODO LIST:push功能的初始化


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
            updateMaterialView();
        }

        // adapter = new ArrayAdapter<>(this, R.layout.row_spn);
        // adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        spinner_label.setOnItemClickListener(new Spinner.OnItemClickListener() {
            @Override
            public boolean onItemClick(Spinner spinner, View view, int i, long l) {

                //处理初始化的时候调用
                if (devices.size() == 0) {
                    return true;
                }
                BleDeviceInfo selectedDev = devices.get(i);
                mBluetoothDevice = selectedDev.getBluetoothDevice();
                if (mScanning)
                    stopScan();
                if (bleDeviceManager.getmConnIndex() == BleDeviceManager.NO_DEVICE) {
                    //mScanView.setStatus("Connecting");
                    bleDeviceManager.setmConnIndex(i);
                    onConnect();
                } else {
                    //mScanView.setStatus("Disconnecting");

                    if (bleDeviceManager.getmConnIndex() != BleDeviceManager.NO_DEVICE) {
                        mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
                    }
                }
                return true;
            }
        });

    }

    private void updateMaterialView() {
        devices = bleDeviceManager.getDevices();
        if (devices.size() != 0) {
            spinner_label.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    // Activity result handling
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQ_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bt_on, Toast.LENGTH_SHORT).show();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(this, R.string.bt_not_on, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "Unknown request code");
                break;
        }
    }

//    @Click
    public void ib_temp(){

        // TODO LIST：点击的时候调用对应的蓝牙功能
      /*  if(unpressed){
            ib_temp.setImageResource(R.drawable.unlock);
            unpressed = false;
        }else{
            unpressed = true;
            ib_temp.setImageResource(R.drawable.lock2);
        }*/
    }
//
//    @Click
//    public void ib_info(){
//        //todo list:弹出对应的关于信息
//        if(info_sn.getState() == SnackBar.STATE_SHOWN)
//            info_sn.dismiss();
//        else{
//            info_sn.applyStyle(R.style.SnackBarMultiLine);
//            info_sn.text("更多信息，请访问\nwww.hainiutech.com")
//                    .actionText("CLOSE");
//            info_sn.show();
//        }
//    }

//    @Click
    public void ib_setting(){
        Dialog.Builder builder = null;
        builder = new SimpleDialog.Builder(true ? R.style.SimpleDialogLight : R.style.SimpleDialog){

            @Override
            protected void onBuildDone(Dialog dialog) {
                dialog.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {

                //TODO LIST:显示密码设置成功
               /* EditText et_pass = (EditText)fragment.getDialog().findViewById(R.id.custom_et_password);
                Toast.makeText(mActivity, "Connected. pass=" + et_pass.getText().toString(), Toast.LENGTH_SHORT).show();
               */
                super.onPositiveActionClicked(fragment);
            }

            @Override
            public void onNegativeActionClicked(DialogFragment fragment) {
                //TODO LIST:取消按钮
/*
                Toast.makeText(mActivity, "Cancelled", Toast.LENGTH_SHORT).show();
*/
                super.onNegativeActionClicked(fragment);
            }
        };

        builder.title("密码设置")
                .positiveAction("确定")
                .negativeAction("取消")
                .contentView(R.layout.layout_dialog_setting);

        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(getSupportFragmentManager(), null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_2);
        //link widgets
//        sv_progress = (SinkView) findViewById(R.id.sv_progress);
//        tv_index = (TextView) findViewById(R.id.tv_index);
//        iv_circle = (ImageView) findViewById(R.id.iv_circle);
//        iv_circle.setBackgroundResource(R.drawable.circle5);
        rl_middle = (RelativeLayout) findViewById(R.id.rl_middle);
        tv_index_middle = (TextView) findViewById(R.id.tv_index_middle);
        tv_title_middle = (TextView) findViewById(R.id.tv_title_middle);
        tv_unit_middle = (TextView) findViewById(R.id.tv_unit_middle);
        iv_circle_middle = (ImageView) findViewById(R.id.iv_circle_middle);
        iv_circle_middle.setBackgroundResource(R.drawable.circle5);

        rl_left = (RelativeLayout) findViewById(R.id.rl_left);
        tv_index_left = (TextView) findViewById(R.id.tv_index_left);
        tv_title_left = (TextView) findViewById(R.id.tv_title_left);
        tv_unit_left = (TextView) findViewById(R.id.tv_unit_left);
        iv_circle_left = (ImageView) findViewById(R.id.iv_circle_left);
        iv_circle_left.setBackgroundResource(R.drawable.circle5);

        rl_right = (RelativeLayout) findViewById(R.id.rl_right);
        tv_index_right = (TextView) findViewById(R.id.tv_index_right);
        tv_title_right = (TextView) findViewById(R.id.tv_title_right);
        tv_unit_right = (TextView) findViewById(R.id.tv_unit_right);
        iv_circle_right = (ImageView) findViewById(R.id.iv_circle_right);
        iv_circle_right.setBackgroundResource(R.drawable.circle5);

        rl_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               LayoutInflater inflater = (LayoutInflater) MainActivity_Design_3.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View root = inflater.inflate(R.layout.view_left, null);
                tv_index_left = (TextView) root.findViewById(R.id.tv_index_left);
                tv_index_left.setText("test");
                iv_circle_left = (ImageView) root.findViewById(R.id.iv_circle_left);
                iv_circle_left.setBackgroundResource(R.drawable.circle5);
                root.setLayoutParams(v.getLayoutParams());
                popField.popView(v, root, true);
            }
        });
        rl_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(transition){
                    //animation hasn't finished
                    return;
                }
                transition = true;
                v.setVisibility(View.INVISIBLE);
                rl_middle.setVisibility(View.INVISIBLE);
                anim_1 = AnimationUtils.loadAnimation(MainActivity_Design_3.this, R.anim.scale_down_left_top);
                Animation anim_tmp = AnimationUtils.loadAnimation(MainActivity_Design_3.this, R.anim.scale_down_right_bottom);
                v.startAnimation(anim_1);
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

                        int value_right = Integer.valueOf((String) tv_index_right.getText());
                        int value_middle=  Integer.valueOf((String) tv_index_middle.getText());

                        int offset_right = 255 - value_right;
                        int offset_middle = 255 - value_middle;
                        GradientDrawable drawable_right = (GradientDrawable) iv_circle_right.getBackground();
                        drawable_right.setColor(Color.argb(255, offset_right, value_right, 0));
                        GradientDrawable drawable_middle = (GradientDrawable) iv_circle_middle.getBackground();
                        drawable_middle.setColor(Color.argb(255, offset_middle, value_middle, 0));

                        anim_2 = AnimationUtils.loadAnimation(MainActivity_Design_3.this, R.anim.scale_up_left_top);
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
                        Animation anim_tmp = AnimationUtils.loadAnimation(MainActivity_Design_3.this, R.anim.scale_up_right_bottom);
                        rl_middle.startAnimation(anim_tmp);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                });
            }
        });

        iv_weather = (ImageView) findViewById(R.id.iv_weather);


        SharedPreferences prefs = getSharedPreferences("background", Context.MODE_PRIVATE);
        int index = prefs.getInt("bg_index", 0);
        iv_background = (ImageView) findViewById(R.id.iv_background);
        iv_background.setImageResource(bg_ids[index]);
        index++;
        if(index >= bg_ids.length){
            index = 0;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("bg_index", index);
        editor.commit();
        ib_device = (ImageButton) findViewById(R.id.ib_device);
        ib_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected){
//                    ib_device.setImageResource(R.drawable.icon_diaper_alert);
                    Toast.makeText(MainActivity_Design_3.this, "Device disconnected.", Toast.LENGTH_SHORT).show();
                    isConnected = false;
                }else{
                    isConnected = true;
                    //for demo purpose only
                    Toast.makeText(MainActivity_Design_3.this, "Device Connected.", Toast.LENGTH_SHORT).show();
                    new ProgressDemoTask().execute();
                }
            }
        });
        iv_emotion = (ImageView) findViewById(R.id.iv_emotion);
        iv_emotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_emotion.setVisibility(View.INVISIBLE);
                if(isRingOn){
                    if(myMediaPlayer != null && myMediaPlayer.isPlaying()){
                        myMediaPlayer.stop();
                        myMediaPlayer.release();
                        myMediaPlayer = null;
                    }
                }else{
                    if( vibrator != null){
                        vibrator.cancel();
                        vibrator = null;
                    }
                }

            }
        });
        ib_settings = (ImageButton) findViewById(R.id.ib_setting);
        ib_ring = (ImageButton) findViewById(R.id.ib_ring);
        ib_ring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRingOn = !isRingOn;
                if(isRingOn){
                    ib_ring.setImageResource(R.drawable.icon_ring);
                }else{
                    ib_ring.setImageResource(R.drawable.icon_vibrate);
                }
            }
        });
        tv_feel = (TextView) findViewById(R.id.tv_feel);


        //popview
        popField = PopField.attach2Window(MainActivity_Design_3.this);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy");
        super.onDestroy();

        if(myMediaPlayer != null && myMediaPlayer.isPlaying()){
            myMediaPlayer.stop();
            myMediaPlayer.release();
            myMediaPlayer = null;
        }
        //注销数据监听listenner
        if (mIsReceiving) {
            unregisterReceiver(mGattUpdateReceiver);
            mIsReceiving = false;
        }

        //注销蓝牙服务
        if (mBluetoothLeService != null) {
            scanLeDevice(false);
            mBluetoothLeService.close();
            unregisterReceiver(mReceiver);
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
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
            //CustomToast.middleBottom(this, "Bind to BluetoothLeService failed");
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

                        //TODO LIST:提示存在多个连接
/*
						mThis.setError("Multiple connections!");
*/
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
/*
						setError("Connect failed");
*/
                    }
                    break;
                default:
/*
					setError("Device busy (connecting/disconnecting)");
*/
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
            updateMaterialView();
            scanLeDevice(true);
            //TODO LIST：更新界面为正在扫描的状态
//			mScanView.updateGui(mScanning);
            if (!mScanning) {
//				setError("Device discovery start failed");
//				setBusy(false);
            }
        } else {
//			setError("BLE not supported on this device");
        }

    }

    private void stopScan() {
        mScanning = false;
        //TODO LIST:更新UI
        //mScanView.updateGui(false);
        scanLeDevice(false);
    }

    private boolean scanLeDevice(boolean enable) {
        if (enable) {
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
                    //TODO LIST:更新界面UI
                    updateMaterialView();

                }

            });
        }
    };


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
/*
						getActivity().finish();
*/
                        break;
                    default:
                        Log.w(TAG, "Action STATE CHANGED not processed ");
                        break;
                }
                //TODO LIST:更新UI
                updateMaterialView();
            }
            else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
                if (status == BluetoothGatt.GATT_SUCCESS)
                {
                    //TODO LIST :切换新的fragment

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


					/*setBusy(false);
					startDeviceActivity();*/

                    // Intent mDeviceIntent = new Intent(DeviceListActivity.this, MainActivity_.class);
                    //  mDeviceIntent.putExtra(MainActivity_.EXTRA_DEVICE, mBluetoothDevice);
                    //  startActivityForResult(mDeviceIntent, REQ_DEVICE_ACT);
                }
                else
                {
//					setError("Connect failed. Status: " + status);
                }
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                // GATT disconnect
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);

                //TODO LIST：删除那个activity
                //stopDeviceActivity();

                if (status == BluetoothGatt.GATT_SUCCESS)
                {
//					setBusy(false);
//					mScanView.setStatus(mBluetoothDevice.getName() + " disconnected", STATUS_DURATION);
                } else
                {
//					setError("Disconnect failed. Status: " + status);
                }
                bleDeviceManager.setmConnIndex(BleDeviceManager.NO_DEVICE);
                mBluetoothLeService.close();
            } else {
                Log.w(TAG,"Unknown action: " + action);
            }
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
			setStatus("Service discovery complete");
*/
            //TODO LIST：初始化后，扫描到服务
			enableSensors(true);
			enableNotifications(true);
         //   enableNotificationForLock(true);
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

            mBluetoothLeService.setCharacteristicNotification(charac,enable);
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
/*
                    displayServices();
*/
                    //checkOad();
                } else {
                    //  Toast.makeText(getApplication(), "Service discovery failed", Toast.LENGTH_LONG).show();
                    return;
                }
            } else if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
                // Notification
                final byte  [] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                //TODO LIST:注销当前的值
                //onCharacteristicChanged(uuidStr, value);
               /* getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(value, "UTF-8");
                            tv_state.setText(text);

                        } catch (Exception e) {
                        }
                    }
                });*/

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

    private void onCharacteristicChanged(String uuidStr, byte[] value) {

       /* Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof IndexFragment) {
            updateContent(uuidStr, value);
        }*/
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

    /**
     * Added by Stevens
     *
     * popup number picker
     */
    public void onAnimationStyle(View view) {
        NumberPicker picker = new NumberPicker(this);
        picker.setAnimationStyle(R.style.Animation_CustomPopup);
        picker.setOffset(1);//偏移量
        picker.setRange(10, 60);//数字范围
        picker.setSelectedItem(10);
        picker.setLabel("秒");
        picker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
            @Override
            public void onOptionPicked(String option) {
                Toast.makeText(MainActivity_Design_3.this, "Time interval is " + option, Toast.LENGTH_SHORT).show();
            }
        });
        picker.show();
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

            GradientDrawable drawable_middle = (GradientDrawable) iv_circle_middle.getBackground();
            drawable_middle.setColor(Color.argb(255, offset, value, 0));
            tv_index_middle.setText(String.valueOf(value));

            if(progress % 10 == 0){
                Random r = new Random(System.currentTimeMillis());
                int value_random = Math.abs(r.nextInt());
                GradientDrawable drawable_left = (GradientDrawable) iv_circle_left.getBackground();
                drawable_left.setColor(Color.argb(255, 255 - value_random%255 , value_random%255, 0));
                tv_index_left.setText(String.valueOf(value_random%255));


                int value_random2 = Math.abs(r.nextInt());
                GradientDrawable drawable_right = (GradientDrawable) iv_circle_right.getBackground();
                drawable_right.setColor(Color.argb(255, 255 - value_random2%255 , value_random2%255, 0));
                tv_index_right.setText(String.valueOf(value_random2%255));

                int value_weather = Math.abs(r.nextInt());
                int index = value_weather % weather_ids.length;
                iv_weather.setImageResource(weather_ids[index]);
            }


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

        /***
         * added by Stevens
         * used for demo purpose only
         */
        private class ConnectDemoTask extends AsyncTask<Void, Integer, Void>{
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for(int i=0; i<10; i++){
                    publishProgress(Integer.valueOf(i));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                int signal = values[0];
                if(signal%2 == 0){
//                    ib_device.setImageResource(R.drawable.icon_diaper_light);

                }else{
//                    ib_device.setImageResource(R.drawable.icon_diaper_dark);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
//                ib_device.setImageResource(R.drawable.icon_diaper_light);
                Toast.makeText(MainActivity_Design_3.this, "Device Connected.", Toast.LENGTH_SHORT).show();
                new ProgressDemoTask().execute();
            }
        }//end of ConnectDemoTask

}//end of file
