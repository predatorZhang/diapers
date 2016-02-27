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
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.worldlink.locker.R;
import com.worldlink.locker.services.BleDeviceInfo;
import com.worldlink.locker.services.BleDeviceManager;
import com.worldlink.locker.services.BluetoothLeService;
import com.worldlink.locker.services.Sensor;
import com.worldlink.locker.services.SensorTag;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.NoTitle;
import org.androidannotations.annotations.ViewById;
import org.demo.ballprogress.view.SinkView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.qqtheme.framework.picker.NumberPicker;
import cn.qqtheme.framework.picker.OptionPicker;

@NoTitle
@Fullscreen
@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @ViewById
    public SinkView sv_progress;
    @ViewById
    public ImageButton ib_device;
    @ViewById
    public ImageView iv_emotion;
    @ViewById
    public ImageButton ib_setting;
    @ViewById
    public ImageButton ib_ring;
    @ViewById
    public TextView tv_feel;

    @ViewById
    public TextView tv_temp;

    private MediaPlayer myMediaPlayer;
    private Vibrator vibrator;
    private boolean isRingOn = true;
    private boolean isConnected = false;
    private boolean unpressed = true;

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

            ib_device.setImageResource(R.drawable.icon_diaper_alert);
        }


    }

    private void updateMaterialView() {
       /* devices = bleDeviceManager.getDevices();
        if (devices.size() != 0) {
            spinner_label.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }*/
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

    /**
     * scane the device
     */
   @Click
    public void ib_device(){

       if (mScanning) {
           stopScan();
       } else {
           startScan();
       }

    }

    @Click
    public void iv_emotion(){

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


    @Click
    public void ib_ring(){

        isRingOn = !isRingOn;
        if(isRingOn){
            ib_ring.setImageResource(R.drawable.icon_ring);
        }else{
            ib_ring.setImageResource(R.drawable.icon_vibrate);
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
                    }
                    break;
                default:
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
            scanLeDevice(true);
            //TODO LIST:更新视图状态
            new ConnectDemoTask().execute();
            if (!mScanning) {

            }
        } else {
            Toast.makeText(MainActivity.this, "BLE not supported on this device", Toast.LENGTH_SHORT).show();
        }

    }

    private void stopScan() {
        mScanning = false;
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
                    //TODO LIST:无设备则直接返回
                    if (bleDeviceManager.getDevices().size() == 0) {
                        return;
                    }
                    if (mScanning)
                        stopScan();
                    mBluetoothDevice = ((BleDeviceInfo)bleDeviceManager.getDevices().get(0)).getBluetoothDevice();
                    if (bleDeviceManager.getmConnIndex() == BleDeviceManager.NO_DEVICE) {
                        bleDeviceManager.setmConnIndex(0);
                        //TODO LIST:避免重复连接同一个设备
                        if(mBluetoothManager.getConnectionState(mBluetoothDevice, BluetoothGatt.GATT)==
                                BluetoothGatt.STATE_DISCONNECTED){
                            onConnect();
                        }
                    }
                   /* else {
                        if (bleDeviceManager.getmConnIndex()  != BleDeviceManager.NO_DEVICE) {
                            mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
                        }
                    }
*/
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
            //TODO LIST：初始化后，扫描到服务
			//enableSensors(true);
			//enableNotifications(true);
           enableNotificationForLock(true);
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
                    displayServices();

                } else {
                    //  Toast.makeText(getApplication(), "Service discovery failed", Toast.LENGTH_LONG).show();
                    return;
                }
            } else if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
                // Notification
                final byte  [] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);

                onCharacteristicChanged(uuidStr, value);

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

        this.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    byte temp = value[0];
                    byte humi = value[1];

                    tv_temp.setText(temp+"°C");
                    sv_progress.setPercent(humi / (float) 100);
                    if (humi <= 20) {
                        tv_feel.setText("干爽");
                    } else if (humi > 20 && humi <= 60) {
                        tv_feel.setText("舒适");
                    } else if (humi > 60 && humi <= 100) {
                        tv_feel.setText("潮湿");
                    }
                    MainActivity.this.sendingAlarm(humi);
                } catch (Exception e) {
                }
            }
        });
    }

    private void onCharacteristicsRead(String uuidStr, byte[] value, int status) {
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

    private void sendingAlarm(byte humi) {
        if (humi < 50) {
            return;
        }
        iv_emotion.setVisibility(View.VISIBLE);
        myMediaPlayer = new MediaPlayer();//MediaPlayer.create(this, R.raw.ptt_not);
        try {
            AssetFileDescriptor fd = getResources().openRawResourceFd(R.raw.beep);
            long offset = fd.getStartOffset();
            long length = fd.getLength();
            if (isRingOn) {
                myMediaPlayer.setLooping(true);
                myMediaPlayer.setDataSource(fd.getFileDescriptor(), offset, length);
                myMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                myMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                myMediaPlayer.prepareAsync();
            } else {
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(new long[]{200, 100, 200, 100, 200, 100}, 0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
                Toast.makeText(MainActivity.this, "Time interval is " + option, Toast.LENGTH_SHORT).show();
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
            for (int i = 0; i <= 100; i++) {
                publishProgress(Integer.valueOf(i));
                try {
                    Thread.sleep(100);
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
            sv_progress.setPercent(progress / (float) 100);
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
            iv_emotion.setVisibility(View.VISIBLE);
            myMediaPlayer = new MediaPlayer();//MediaPlayer.create(this, R.raw.ptt_not);
            try {
//				AssetFileDescriptor fd = getResources().openRawResourceFd(R.raw.ptt_not);
                AssetFileDescriptor fd = getResources().openRawResourceFd(R.raw.beep);
                long offset = fd.getStartOffset();
                long length = fd.getLength();
                if(isRingOn){
                    myMediaPlayer.setLooping(true);
                    myMediaPlayer.setDataSource(fd.getFileDescriptor(), offset, length);
                    myMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    myMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                    myMediaPlayer.prepareAsync();
                }else{
                    vibrator  = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(new long[]{200, 100, 200,100, 200,100}, 0);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//end of ProgressDemoTask

        /***
         * added by Stevens
         * used for demo purpose only
         */
        private class ConnectDemoTask extends AsyncTask<Void, Integer, Void>{

            private boolean flag = true;
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (mScanning) {
                    publishProgress(0);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
              /*  for(int i=0; i<10; i++){
                    publishProgress(Integer.valueOf(i));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }*/
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                flag = !flag;
                int imgId = flag ? R.drawable.icon_diaper_light : R.drawable.icon_diaper_dark;
                ib_device.setImageResource(imgId);

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);


                if (mBluetoothLeService == null) {
                    ib_device.setImageResource(R.drawable.icon_diaper_alert);
                    Toast.makeText(MainActivity.this, "设备连接失败", Toast.LENGTH_SHORT).show();
                }

                if (mBluetoothManager.getConnectionState(mBluetoothDevice, BluetoothGatt.GATT) ==
                        BluetoothGatt.STATE_CONNECTED) {
                    ib_device.setImageResource(R.drawable.icon_diaper_light);
                }
                else{
                    ib_device.setImageResource(R.drawable.icon_diaper_alert);
                }

            }
        }//end of ConnectDemoTask

}//end of file
