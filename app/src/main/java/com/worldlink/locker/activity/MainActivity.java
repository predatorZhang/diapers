package com.worldlink.locker.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.worldlink.locker.R;
import com.worldlink.locker.services.BluetoothLeService;
import com.worldlink.locker.services.Sensor;
import com.worldlink.locker.services.SensorTag;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.NoTitle;
import org.androidannotations.annotations.ViewById;

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


    //added by Stevens
//    private SinkView sv_progress;
    private int[] bg_ids = {R.drawable.bg_1,R.drawable.bg_1_blur,R.drawable.bg_2, R.drawable.bg_2_blur, R.drawable.bg_3, R.drawable.bg_3_blur, R.drawable.bg_4, R.drawable.bg_4_blur, R.drawable.bg_5,R.drawable.bg_6,R.drawable.bg_7,R.drawable.bg_8,};

    @ViewById
    public TextView tv_index;

    @ViewById
    public ImageView iv_circle;

    @ViewById
    public ImageView iv_background;

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

    @ViewById
    public TextView tv_temp_eval;

    @ViewById
    public TextView tv_hum;

    @ViewById
    public TextView tv_hum_eval;

    @ViewById
    public TextView tv_pm;

    @ViewById
    public TextView tv_pm_eval;

    @ViewById
    public TextView tv_unit;

    @ViewById
    public TextView tv_cell;

    @ViewById
    public ImageView ic_battery;

    private static final String TAG = "MainActivity";

    //建立连接后
    private BluetoothGatt mBtGatt = null;
    private boolean mIsReceiving = false;
    private boolean mServicesRdy = false;
    private List<BluetoothGattService> mServiceList = new ArrayList<BluetoothGattService>();
    private static final int GATT_TIMEOUT = 100; // milliseconds
    private List<Sensor> mEnabledSensors = new ArrayList<Sensor>();
    private BluetoothLeService mBluetoothLeService = BluetoothLeService.getInstance();


    @AfterViews
    public void init() {

        // Create GATT object
        mBtGatt = BluetoothLeService.getBtGatt();

        if (!mIsReceiving) {
            this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
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

        iv_circle.setBackgroundResource(R.drawable.circle5);
        iv_background.setImageResource(bg_ids[0]);
        this.showResult((float)25.5, (float)20.0, (float)100.0,
                (float)0.2, (float)2);
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


        //注销数据监听listenner
        if (mIsReceiving) {
            unregisterReceiver(mGattUpdateReceiver);
            mIsReceiving = false;
        }

    }



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
                    byte temp = value[0];
                    byte humi = value[1];
                    byte hiPm25 = value[2];
                    byte lowPm25 = value[3];
                    float pm25 = hiPm25 * 256 + lowPm25;
                    byte hiHcho = value[4];
                    byte lowHcho = value[5];
                    float hcho = (float) ((hiHcho * 256 + lowHcho) / 1000.0);
                    byte cell = value[6];
                    showResult(temp, humi, pm25, hcho, cell);
                } catch (Exception e) {
                }
            }
        });
    }

    private void showResult(float temp, float humi, float pm, float hcho, float cell) {

        this.tv_cell.setText((int) cell + "%");
        if (cell > 75) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_full);
        }else if (cell <= 75 && cell > 60) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_34);

        } else if (cell <= 60 && cell > 50) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_half);

        } else if (cell <= 50 && cell > 20) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_14);
        } else {
            this.ic_battery.setImageResource(R.drawable.ic_battery_low);
        }
            //甲醛：<0.1 [0.1,0.5),[0.5,0.6),[0.6.. 正常，轻度污染，污染，重度污染
        GradientDrawable drawable = (GradientDrawable) iv_circle.getBackground();
        if (hcho < 0.1) {
            drawable.setColor(Color.argb(255, 0, 255, 0));
            this.tv_unit.setText("正常");
        } else if (hcho >= 0.1 && hcho < 0.5) {
            drawable.setColor(Color.argb(255, 50, 0, 0));
            this.tv_unit.setText("轻度污染");

        } else if (hcho >= 0.5 && hcho < 0.6) {
            drawable.setColor(Color.argb(255, 100, 0, 0));
            this.tv_unit.setText("污染");
        } else {
            drawable.setColor(Color.argb(255, 255, 0, 0));
            this.tv_unit.setText("重度污染");
        }
        this.tv_index.setText(hcho + "");

        /*PM2.5
        0-35 优 35-75良 75-115 轻度污染 115-150 中度污染 150-250 重度污染 250-500 严重污染
         */
        if (pm < 35) {
            this.tv_pm_eval.setText("优");
        } else if (pm >= 35 && pm < 75) {
            this.tv_pm_eval.setText("良");
        } else if (pm >= 75 && pm < 115) {
            this.tv_pm_eval.setText("轻度污染");
        } else if (pm >= 115 && pm < 150) {
            this.tv_pm_eval.setText("中度污染");
        } else if (pm >= 150 && pm < 250) {
            this.tv_pm_eval.setText("重度污染");
        } else {
            this.tv_pm_eval.setText("严重污染");
        }
        this.tv_pm.setText(pm + "");


       /* 20 ~ 39 %: 干燥
        40 ~ 70 %: 舒适
        71 ~ 100 %: 潮湿*/
        this.tv_hum.setText(humi + "%");
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
        this.tv_temp.setText(temp + "°C");
        if (temp < 17) {
            this.tv_temp_eval.setText("冷");
        } else if (temp >= 17 && temp < 23) {
            this.tv_temp_eval.setText("舒适");
        } else if (temp >= 23 && temp < 40) {
            this.tv_temp_eval.setText("热");
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
            GradientDrawable drawable = (GradientDrawable) iv_circle.getBackground();
            drawable.setColor(Color.argb(255, offset, value, 0));
            tv_index.setText(String.valueOf(value));
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
