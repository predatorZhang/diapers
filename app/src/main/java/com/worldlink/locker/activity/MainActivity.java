package com.worldlink.locker.activity;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.clj.fastble.BleManager;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.conn.BleGattCallback;
import com.clj.fastble.data.ScanResult;
import com.clj.fastble.exception.BleException;
import com.daasuu.bl.ArrowDirection;
import com.daasuu.bl.BubbleLayout;
import com.daasuu.bl.BubblePopupHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;
import com.worldlink.locker.R;
import com.worldlink.locker.UpdateService;
import com.worldlink.locker.common.HeWeather5Bean;
import com.worldlink.locker.common.ImageLoadTool;
import com.worldlink.locker.common.WeatherList;
import com.worldlink.locker.http.ApiClent;
import com.zaaach.citypicker.CheckPermissionsListener;
import com.zaaach.citypicker.CityPickerActivity;
import com.zaaach.citypicker.utils.StringUtils;


import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;


@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    private static final String LOG_TAG = "MainActivity";
    private static final int ID_HCHO = 1001;
    private static final int ID_PM25 = 1002;
    //alpha value
    private static final int ALPHA = 180;
    //added by Stevens
//    private SinkView sv_progress;
    private int[] bg_ids = {
            R.drawable.bg_nightsky
    };

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

    @ViewById
    public ImageView iv_circle_left_inner;

    @ViewById
    public ImageView iv_circle_left_outer;

    @ViewById
    public ImageView iv_circle_middle_outer;

    @ViewById
    public ImageView iv_circle_middle_inner;

    @ViewById
    public ImageView iv_circle_right_outer;

    @ViewById
    public ImageView iv_circle_right_inner;

    @ViewById
    public ImageView ib_share;

    private static final String TAG = "MainActivity";


    private PopupWindow weatherPopupWindow;
    private PopupWindow pm25PopupWindow;
    private ImageLoadTool imageLoadTool = new ImageLoadTool();

    private static final int REQUEST_CODE_PICK_CITY = 0;


    //added by StevenT
    private boolean DEMO = false;
    private boolean shouldNotifyUser = true;
    private boolean shouldNotifyUser_pm25 = true;
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
                    MainActivity.this.updateCurWeather(city, code, tmp,
                            hum, pm25, pm10, qlt, time);
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


    private void updateCurWeather(String city, String code,
                                  String temp, String humi,
                                  String pm25, String pm10,
                                  String qual,
                                  String time) {

        TextView tvWetrCity = (TextView) this.findViewById(R.id.wetrtv_city);
        TextView tvWetrAq = (TextView) this.findViewById(R.id.wetrtv_quality);
        TextView tvWetrTemp = (TextView) this.findViewById(R.id.wetr_tv_temp);
        TextView tvWetrHumi = (TextView) this.findViewById(R.id.wetr_tv_humid);
        TextView tvWetrPM25 = (TextView) this.findViewById(R.id.wetr_tv_pm25);
        TextView tvWetrPm10 = (TextView) this.findViewById(R.id.wetr_tv_pm10);

        TextView tvWetrTime = (TextView) this.findViewById(R.id.wetrtv_time);

        tvWetrCity.setText(city);
        imageLoadTool.loadImage(this.ib_weather, "http://cdn.heweather.com/cond_icon/"
                + code + ".png", (DisplayImageOptions) null);
        tvWetrHumi.setText("湿度:" + humi + "%");
        tvWetrTemp.setText("温度:" + temp + "°C");
        tvWetrPM25.setText("PM2.5: " + pm25);
        tvWetrPm10.setText("pm10:" + pm10);
        tvWetrAq.setText("空气质量:" + qual);
        tvWetrTime.setText(time);
    }


    private Animation anim_1;
    private Animation anim_2;
    private boolean transition = false;

    //TODO LIST:需要判断谁在中间

    private ViewGroup.LayoutParams params_tv_left,params_tv_title_left, params_tv_unit_left;
    private ViewGroup.LayoutParams params_tv_right,params_tv_title_right, params_tv_unit_right;
    private ViewGroup.LayoutParams params_tv_middle,params_tv_title_middle, params_tv_unit_middle;
    private ViewGroup.LayoutParams params_iv_circle_left,params_iv_circle_left_inner, params_iv_circle_left_outer;

    private ViewGroup.LayoutParams params_iv_circle_middle,params_iv_circle_middle_inner, params_iv_circle_middle_outer;
    private ViewGroup.LayoutParams params_iv_circle_right,params_iv_circle_right_inner, params_iv_circle_right_outer;
    private ViewGroup.LayoutParams params_rl_left,params_rl_middle, params_rl_right;

    private void initLayoutParam() {

        params_tv_left =(tv_index_left.getLayoutParams());
        params_tv_title_left = (tv_title_left.getLayoutParams());
        params_tv_unit_left =(tv_unit_left.getLayoutParams());

        params_tv_right =(tv_index_right.getLayoutParams());
        params_tv_title_right =  (tv_title_right.getLayoutParams());
        params_tv_unit_right = (tv_unit_right.getLayoutParams());

        params_tv_middle = (tv_index_middle.getLayoutParams());
        params_tv_title_middle =  (tv_title_middle.getLayoutParams());
        params_tv_unit_middle =  (tv_unit_middle.getLayoutParams());

        params_iv_circle_left =  (iv_circle_left.getLayoutParams());
        params_iv_circle_left_inner = (iv_circle_left_inner.getLayoutParams());
        params_iv_circle_left_outer = (iv_circle_left_outer.getLayoutParams());

        params_iv_circle_middle = (iv_circle_middle.getLayoutParams());
        params_iv_circle_middle_inner =(iv_circle_middle_inner.getLayoutParams());
        params_iv_circle_middle_outer = ( iv_circle_middle_outer.getLayoutParams());

        params_iv_circle_right = (iv_circle_right.getLayoutParams());
        params_iv_circle_right_inner =  (iv_circle_right_inner.getLayoutParams());
        params_iv_circle_right_outer = (iv_circle_right_outer.getLayoutParams());

        params_rl_left =(rl_left.getLayoutParams());
        params_rl_middle =  (rl_middle.getLayoutParams());
        params_rl_right =  (rl_right.getLayoutParams());

        layeroutArray[0] = rl_left;
        layeroutArray[1] = rl_middle;
        layeroutArray[2] = rl_right;

        indexViewArray[0] = tv_index_left;
        indexViewArray[1] = tv_index_middle;
        indexViewArray[2] = tv_index_right;

        titleViewArray[0] = tv_title_left;
        titleViewArray[1] = tv_title_middle;
        titleViewArray[2] = tv_title_right;

        unitViewArray[0] = tv_unit_left;
        unitViewArray[1] = tv_unit_middle;
        unitViewArray[2] = tv_unit_right;

        circleViewArray[0] = iv_circle_left;
        circleViewArray[1] = iv_circle_middle;
        circleViewArray[2] = iv_circle_right;

        circleInnerViewArray[0] = iv_circle_left_inner;
        circleInnerViewArray[1] = iv_circle_middle_inner;
        circleInnerViewArray[2] = iv_circle_right_inner;

        circleOutterViewArray[0] = iv_circle_left_outer;
        circleOutterViewArray[1] = iv_circle_middle_outer;
        circleOutterViewArray[2] = iv_circle_right_outer;

    }

    private boolean flagLeft = false;


    private RelativeLayout[] layeroutArray = new RelativeLayout[3];
    private TextView[] indexViewArray = new TextView[3];
    private TextView[] titleViewArray = new TextView[3];
    private TextView[] unitViewArray = new TextView[3];
    private ImageView[] circleViewArray = new ImageView[3];
    private ImageView[] circleInnerViewArray = new ImageView[3];
    private ImageView[] circleOutterViewArray = new ImageView[3];


    private void swapLeft() {
        RelativeLayout tmp;
        tmp = layeroutArray[0];
        layeroutArray[0] = layeroutArray[1];
        layeroutArray[1] = tmp;

        TextView tmpIndex;
        tmpIndex = indexViewArray[0];
        indexViewArray[0] = indexViewArray[1];
        indexViewArray[1] = tmpIndex;

        TextView tmpTitle;
        tmpTitle = titleViewArray[0];
        titleViewArray[0] = titleViewArray[1];
        titleViewArray[1] = tmpTitle;

        TextView tmpUnit;
        tmpUnit = unitViewArray[0];
        unitViewArray[0] = unitViewArray[1];
        unitViewArray[1] = tmpUnit;

        ImageView tmpCirle;
        tmpCirle = circleViewArray[0];
        circleViewArray[0] = circleViewArray[1];
        circleViewArray[1] = tmpCirle;

        ImageView tmpInnerCirle;
        tmpInnerCirle = circleInnerViewArray[0];
        circleInnerViewArray[0] = circleInnerViewArray[1];
        circleInnerViewArray[1] = tmpInnerCirle;

        ImageView tmpOutterCirle;
        tmpOutterCirle = circleOutterViewArray[0];
        circleOutterViewArray[0] = circleOutterViewArray[1];
        circleOutterViewArray[1] = tmpOutterCirle;

    }

    private void swapRight() {
        RelativeLayout tmp;
        tmp = layeroutArray[1];
        layeroutArray[1] = layeroutArray[2];
        layeroutArray[2] = tmp;

        TextView tmpIndex;
        tmpIndex = indexViewArray[1];
        indexViewArray[1] = indexViewArray[2];
        indexViewArray[2] = tmpIndex;

        TextView tmpTitle;
        tmpTitle = titleViewArray[1];
        titleViewArray[1] = titleViewArray[2];
        titleViewArray[2] = tmpTitle;

        TextView tmpUnit;
        tmpUnit = unitViewArray[1];
        unitViewArray[1] = unitViewArray[2];
        unitViewArray[2] = tmpUnit;

        ImageView tmpCirle;
        tmpCirle = circleViewArray[1];
        circleViewArray[1] = circleViewArray[2];
        circleViewArray[2] = tmpCirle;

        ImageView tmpInnerCirle;
        tmpInnerCirle = circleInnerViewArray[1];
        circleInnerViewArray[1] = circleInnerViewArray[2];
        circleInnerViewArray[2] = tmpInnerCirle;

        ImageView tmpOutterCirle;
        tmpOutterCirle = circleOutterViewArray[1];
        circleOutterViewArray[1] = circleOutterViewArray[2];
        circleOutterViewArray[2] = tmpOutterCirle;

    }
    View.OnClickListener onLeftClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(transition){
                return;
            }

            transition = true;
            //TODO LIST:清除监听器
            layeroutArray[0].setOnClickListener(null);
            layeroutArray[1].setOnClickListener(null);
            layeroutArray[2].setOnClickListener(null);

            v.setVisibility(View.INVISIBLE);
            layeroutArray[1].setVisibility(View.INVISIBLE);
            anim_1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_left_out);
            Animation anim_tmp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_middle_trans_left);
            v.startAnimation(anim_1);
            layeroutArray[1].startAnimation(anim_tmp);

            anim_1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    anim_1 = null;
                    try {

                        indexViewArray[0].setLayoutParams(params_tv_middle);
                        indexViewArray[0].setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                        indexViewArray[1].setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        indexViewArray[1].setLayoutParams(params_tv_left);

                        titleViewArray[0].setLayoutParams(params_tv_title_middle);
                        titleViewArray[0].setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        titleViewArray[1].setLayoutParams(params_tv_title_left);
                        titleViewArray[1].setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                        unitViewArray[0].setLayoutParams(params_tv_unit_middle);
                        unitViewArray[0].setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        unitViewArray[1].setLayoutParams(params_tv_unit_left);
                        unitViewArray[1].setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                        circleViewArray[0].setLayoutParams(params_iv_circle_middle);
                        circleInnerViewArray[0].setLayoutParams(params_iv_circle_middle_inner);
                        circleOutterViewArray[0].setLayoutParams(params_iv_circle_middle_outer);

                        circleViewArray[1].setLayoutParams(params_iv_circle_left);
                        circleInnerViewArray[1].setLayoutParams(params_iv_circle_left_inner);
                        circleOutterViewArray[1].setLayoutParams(params_iv_circle_left_outer);

                        layeroutArray[0].setLayoutParams(params_rl_middle);
                        layeroutArray[1].setLayoutParams(params_rl_left);

                        swapLeft();
                        layeroutArray[0].setOnClickListener(onLeftClick);
                        layeroutArray[1].setOnClickListener(null);
                        layeroutArray[2].setOnClickListener(onRightClick);

                        anim_2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_middle_in);
                        anim_2.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                layeroutArray[0].setVisibility(View.VISIBLE);
                                layeroutArray[1].setVisibility(View.VISIBLE);
                                layeroutArray[2].setVisibility(View.VISIBLE);
                                anim_2 = null;
                                transition = false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        layeroutArray[1].startAnimation(anim_2);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

            });
        }
    };


    private RelativeLayout hideLayout;




    private boolean flagRight = false;
    View.OnClickListener onRightClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(transition){
                return;
            }
            transition = true;
            layeroutArray[0].setOnClickListener(null);
            layeroutArray[1].setOnClickListener(null);
            layeroutArray[2].setOnClickListener(null);

            v.setVisibility(View.INVISIBLE);
            layeroutArray[1].setVisibility(View.INVISIBLE);
            anim_1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_right_out);
            Animation anim_tmp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_middle_trans_right);
            v.startAnimation(anim_1);
            layeroutArray[1].startAnimation(anim_tmp);

            anim_1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    anim_1 = null;
                    try {

                        indexViewArray[2].setLayoutParams(params_tv_middle);
                        indexViewArray[2].setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                        indexViewArray[1].setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        indexViewArray[1].setLayoutParams(params_tv_right);

                        titleViewArray[2].setLayoutParams(params_tv_title_middle);
                        titleViewArray[2].setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        titleViewArray[1].setLayoutParams(params_tv_title_right);
                        titleViewArray[1].setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                        unitViewArray[2].setLayoutParams(params_tv_unit_middle);
                        unitViewArray[2].setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        unitViewArray[1].setLayoutParams(params_tv_unit_right);
                        unitViewArray[1].setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                        circleViewArray[2].setLayoutParams(params_iv_circle_middle);
                        circleInnerViewArray[2].setLayoutParams(params_iv_circle_middle_inner);
                        circleOutterViewArray[2].setLayoutParams(params_iv_circle_middle_outer);

                        circleViewArray[1].setLayoutParams(params_iv_circle_right);
                        circleInnerViewArray[1].setLayoutParams(params_iv_circle_right_inner);
                        circleOutterViewArray[1].setLayoutParams(params_iv_circle_right_outer);

                        layeroutArray[2].setLayoutParams(params_rl_middle);
                        layeroutArray[1].setLayoutParams(params_rl_right);

                        swapRight();
                        layeroutArray[0].setOnClickListener(onLeftClick);
                        layeroutArray[1].setOnClickListener(null);
                        layeroutArray[2].setOnClickListener(onRightClick);

                        anim_2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_middle_in);
                        anim_2.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                layeroutArray[0].setVisibility(View.VISIBLE);
                                layeroutArray[1].setVisibility(View.VISIBLE);
                                layeroutArray[2].setVisibility(View.VISIBLE);
                                anim_2 = null;
                                transition = false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        layeroutArray[1].startAnimation(anim_2);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

            });
        }
    };

    public void rl_right() {
        if (transition) {
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

    public BleManager bleManager;
    public final String DEVICE_NAME = "HN_PM2.5_CH2O";
    public final String UUID_SERVICE = "0000fff1-0000-1000-8000-00805f9b34fb";
    public final String UUID_NOTIFY = "0000fffa-0000-1000-8000-00805f9b34fb";

    public boolean mBleSupported = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @AfterViews
    public void init() {

        requestPermissions(this, neededPermissions,null);

        bleManager = new BleManager(this);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
            mBleSupported = false;
        }
        if (!bleManager.isSupportBle()) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_LONG).show();
            mBleSupported = false;
        }
        bleManager.enableBluetooth();
        //connect to device
        if(!DEMO){
            startScan();
        }


        ib_device.setImageResource(R.drawable.icon_disconnect);
//        ib_device.setImageResource(R.drawable.icon_bluetooth_red);
        XGPushManager.registerPush(this, "*");

        updateNotifyService();
        pushInXiaomi();
        startUpdateService();
        startPosition();
        //restore widgets state
        iv_circle_middle.setBackgroundResource(R.drawable.circle5);
        GradientDrawable drawable = (GradientDrawable) iv_circle_middle.getBackground();
        drawable.setColor(getResources().getColor(R.color.tpi_unchecked));
        iv_circle_left.setBackgroundResource(R.drawable.circle5);
        drawable = (GradientDrawable) iv_circle_left.getBackground();
        drawable.setColor(getResources().getColor(R.color.tpi_unchecked));
        iv_circle_right.setBackgroundResource(R.drawable.circle5);
        drawable = (GradientDrawable) iv_circle_right.getBackground();
        drawable.setColor(getResources().getColor(R.color.tpi_unchecked));

        ic_battery.setImageResource(R.drawable.ic_battery_low);
        this.tv_cell.setText("--%");

        iv_background.setImageResource(R.drawable.bg_image1080p);

        final BubbleLayout bubbleLayout = (BubbleLayout) LayoutInflater.from(this).
                inflate(R.layout.layout_weather_bubble, null);
        weatherPopupWindow = BubblePopupHelper.create(this, bubbleLayout);
        ib_weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, CityPickerActivity.class),
                        REQUEST_CODE_PICK_CITY);
            }
        });

        ib_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View mView = getWindow().getDecorView().getRootView();
                mView.setDrawingCacheEnabled(true);
                Bitmap bmp = Bitmap.createBitmap(mView.getDrawingCache());
                mView.setDrawingCacheEnabled(false);
                ScreenshotTask task = new ScreenshotTask();
                task.execute(bmp);
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

        rl_left.setOnClickListener(onLeftClick);
        rl_right.setOnClickListener(onRightClick);
        hideLayout = this.rl_middle;
        this.initLayoutParam();


        if(DEMO){
            debugHandler.sendEmptyMessageDelayed(0, 2000);
            debugHandler.sendEmptyMessageDelayed(1, 12000);
            debugHandler.sendEmptyMessageDelayed(2, 32000);
        }


    }

    //added by Steven.T
    //for debugging purpose only
    private Handler debugHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            ib_device.setImageResource(R.drawable.icon_bluetooth);
            ib_device.setImageResource(R.drawable.icon_connect);
            switch(msg.what){
                case 0:
                    showResult(23, 10, 180, 252, 130, 0.55f, 60);
                    break;
                case 1:
                    showResult(23, 10, 180, 150, 130, 1.55f, 60);
                    break;

                case 2:
                    showResult(23, 10, 180, 100, 130, 0.85f, 60);
                    break;
            }

            super.handleMessage(msg);
        }
    };

    private AMapLocationClient mLocationClient;

    private void startPosition() {
        this.mLocationClient = new AMapLocationClient(this);
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setOnceLocation(true);
        this.mLocationClient.setLocationOption(option);
        this.mLocationClient.setLocationListener(new AMapLocationListener() {
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        String city = aMapLocation.getCity();
                        String district = aMapLocation.getDistrict();
                        String location = StringUtils.extractLocation(city, district);
                        ApiClent.getWeather(MainActivity.this, city, weatherCallback);
                    } else {
//                        CityPickerActivity.this.mCityAdapter.updateLocateState(666, (String) null);
                    }
                }

            }
        });
        this.mLocationClient.startLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_CITY && resultCode == RESULT_OK) {
            if (data != null) {
                String city = data.getStringExtra(CityPickerActivity.KEY_PICKED_CITY);
                ApiClent.getWeather(MainActivity.this, city, weatherCallback);
            }
        }
    }

    //高德定位所需要的权限
    protected final String[] neededPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    private static final int REQUEST_CODE = 2333;

//    private CheckPermissionsListener mListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE:
                List<String> deniedPermissions = new ArrayList<>();
                int length = grantResults.length;
                for (int i = 0; i < length; i++){
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        //该权限被拒绝了
                        deniedPermissions.add(permissions[i]);
                    }
                }
              /*  if (deniedPermissions.size() > 0){
                    mListener.onDenied(deniedPermissions);
                }else{
                    mListener.onGranted();
                }*/
                break;
            default:
                break;
        }
    }


    public void requestPermissions(Activity activity, String[] permissions, CheckPermissionsListener listener){
        if (activity == null) return;
//        mListener = listener;
        List<String> deniedPermissions = findDeniedPermissions(activity, permissions);
        if (!deniedPermissions.isEmpty()){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        }else{
            //所有权限都已经同意了
//            mListener.onGranted();
        }
    }

    private List<String> findDeniedPermissions(Activity activity, String... permissions){
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions){
            if (ActivityCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED){
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
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
            XGPushManager.registerPush(this, "test");
        } else {
            XGPushManager.registerPush(this, "*");
        }
    }

    @Override
    public void onResume() {

        // TODO Auto-generated method stub
        super.onResume();
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


    private void startScan() {

        if (bleManager != null && !bleManager.isBlueEnable()) {
            bleManager.enableBluetooth();
        }

        if (bleManager != null && !bleManager.isConnectingOrConnected()) {
        bleManager.scanNameAndConnect(
                DEVICE_NAME,
                10000,
                false,
                new BleGattCallback() {
                    @Override
                    public void onNotFoundDevice() {
                        Log.i(TAG, "未发现设备！");
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                ib_device.setImageResource(R.drawable.icon_bluetooth_red);
                                ib_device.setImageResource(R.drawable.icon_disconnect);
                                //restore widgets state
                                iv_circle_middle.setBackgroundResource(R.drawable.circle5);
                                GradientDrawable drawable = (GradientDrawable) iv_circle_middle.getBackground();
                                drawable.setColor(getResources().getColor(R.color.tpi_unchecked));
                                iv_circle_left.setBackgroundResource(R.drawable.circle5);
                                drawable = (GradientDrawable) iv_circle_left.getBackground();
                                drawable.setColor(getResources().getColor(R.color.tpi_unchecked));
                                iv_circle_right.setBackgroundResource(R.drawable.circle5);
                                drawable = (GradientDrawable) iv_circle_right.getBackground();
                                drawable.setColor(getResources().getColor(R.color.tpi_unchecked));

                                ic_battery.setImageResource(R.drawable.ic_battery_low);
                                tv_cell.setText("--%");
                                Toast.makeText(MainActivity.this.getApplication(), "未找到甲醛检测仪", Toast.LENGTH_LONG).show();
                            }
                        });

                        //reconnect to device every 60s
                        connectHandler.sendEmptyMessageDelayed(0, 90000);
                    }

                    @Override
                    public void onFoundDevice(ScanResult scanResult) {

                    }

                    @Override
                    public void onConnectSuccess(final BluetoothGatt gatt, int status) {
                        Log.i(TAG, "连接成功！");
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gatt.discoverServices();
                                Toast.makeText(MainActivity.this.getApplication(), "设备连接已连接", Toast.LENGTH_LONG).show();
//                                ib_device.setImageResource(R.drawable.icon_bluetooth);
                                ib_device.setImageResource(R.drawable.icon_connect);

                            }
                        });
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        Log.i(TAG, "服务被发现！");
                        bleManager.getBluetoothState();

                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    bleManager.notify(
                                            UUID_SERVICE,
                                            UUID_NOTIFY,
                                            new BleCharacterCallback() {
                                                @Override
                                                public void onSuccess(BluetoothGattCharacteristic characteristic) {
                                                    MainActivity.this.onCharacteristicChanged("", characteristic.getValue());
                                                }

                                                @Override
                                                public void onFailure(BleException exception) {
                                                    bleManager.handleException(exception);
                                                }
                                            });
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                    }

                    @Override
                    public void onConnectFailure(BleException exception) {
                        Log.i(TAG, "连接失败或连接中断：" + exception.toString());
                        bleManager.handleException(exception);
                        //reconnect to device every 15s
                        connectHandler.sendEmptyMessageDelayed(0, 15000);

                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    Toast.makeText(getApplication(), "设备连接已断开，请重连", Toast.LENGTH_LONG).show();
//                                    ib_device.setImageResource(R.drawable.icon_bluetooth_red);
                                    ib_device.setImageResource(R.drawable.icon_disconnect);
                                    //restore widgets state
                                    iv_circle_middle.setBackgroundResource(R.drawable.circle5);
                                    GradientDrawable drawable = (GradientDrawable) iv_circle_middle.getBackground();
                                    drawable.setColor(getResources().getColor(R.color.tpi_unchecked));
                                    iv_circle_left.setBackgroundResource(R.drawable.circle5);
                                    drawable = (GradientDrawable) iv_circle_left.getBackground();
                                    drawable.setColor(getResources().getColor(R.color.tpi_unchecked));
                                    iv_circle_right.setBackgroundResource(R.drawable.circle5);
                                    drawable = (GradientDrawable) iv_circle_right.getBackground();
                                    drawable.setColor(getResources().getColor(R.color.tpi_unchecked));

                                    ic_battery.setImageResource(R.drawable.ic_battery_low);
                                    tv_cell.setText("--%");
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        });
                    }

                }
        );
        }
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy");
        if (bleManager != null && bleManager.isConnectingOrConnected()) {
            bleManager.stopNotify(UUID_SERVICE, UUID_NOTIFY);
            bleManager.closeBluetoothGatt();
            bleManager = null;
        }
        super.onDestroy();
    }

    private void onCharacteristicChanged(String uuidStr, final byte[] srcValue) {
        //TODO LIST:更新界面
        this.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    int[] value = new int[srcValue.length];
                    for (int i = 0; i < srcValue.length; i++) {
                        value[i] = srcValue[i] & 0xff;
                    }
                    byte[] temp = {(byte)value[4], (byte)value[3], (byte)value[2], (byte)value[1]};
                    DataInputStream dis = new DataInputStream(new
                            ByteArrayInputStream(temp));
                    float fTemp = dis.readFloat();
                    BigDecimal b = new BigDecimal(fTemp);
                    fTemp = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                    dis.close();

                    Log.i("温度:", fTemp + "字节码" + temp.toString());


                    int humi = value[5];
                    Log.i("湿度:", humi + "字节码" + humi);


                    int hiPm1 = value[6];
                    int lowPm1 = value[7];
                    float pm1 = hiPm1 * 256 + lowPm1;
                    Log.i("pm1:", pm1 + "字节码高位：" + hiPm1 + "低位:" + lowPm1);


                    int hiPm25 = value[8];
                    int lowPm25 = value[9];
                    float pm25 = hiPm25 * 256 + lowPm25;
                    Log.i("pm25:", pm25 + "字节码高位：" + hiPm25 + "低位:" + lowPm25);


                    int hiPm10 = value[10];
                    int lowPm10 = value[11];
                    float pm10 = hiPm10 * 256 + lowPm10;
                    Log.i("pm10:", pm10 + "字节码高位：" + hiPm10 + "低位:" + lowPm10);


                    int hiHcho = value[12];
                    int lowHcho = value[13];
                    float hcho = (float) ((hiHcho * 256 + lowHcho) / 1000.0);
                    BigDecimal b2 = new BigDecimal(hcho);
                    hcho = b2.setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
                    Log.i("甲醛:", hcho + "字节码高位：" + hiHcho + "低位:" + lowHcho);

                    int cell = value[14];
                    showResult(fTemp, humi, pm1, pm25, pm10, hcho, cell);
                } catch (Exception e) {
                }
            }
        });
    }

    /**
     *
     * @param temp
     * @param humi
     * @param pm1
     * @param pm
     * @param pm10
     * @param hcho
     * @param cell
     */
    private void showResult(float temp, float humi, float pm1,
                            float pm, float pm10, float hcho, float cell) {

//        this.changeBackgroundImage(hcho, pm);

        this.tv_cell.setText((int) cell + "%");
        if (cell > 75) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_full);
        } else if (cell <= 75 && cell > 60) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_34);

        } else if (cell <= 60 && cell > 50) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_half);

        } else if (cell <= 50 && cell > 10) {
            this.ic_battery.setImageResource(R.drawable.ic_battery_14);
        } else {
            this.ic_battery.setImageResource(R.drawable.ic_battery_low);
        }
        //甲醛：<0.1 [0.1,0.5),[0.5,0.6),[0.6.. 正常，轻度污染，污染，重度污染
        GradientDrawable drawable = (GradientDrawable) iv_circle_middle.getBackground();
        if (hcho < 0.1) {
            drawable.setColor(Color.argb(ALPHA, 0, 255, 0));
            this.tv_unit_middle.setText("正常");
            if(!shouldNotifyUser){
                shouldNotifyUser = true;
                //dismiss notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(ID_HCHO);
            }
        } else if (hcho >= 0.1 && hcho < 0.5) {
//            drawable.setColor(Color.argb(255, 50, 0, 0));
            float red = (float) (127.0 * (hcho/0.5));
            float green = 255 - red;
            drawable.setColor(Color.argb(ALPHA, (int)red, (int)green, 0));
            this.tv_unit_middle.setText("轻度污染");
            if(shouldNotifyUser){
                //user has been notified
                shouldNotifyUser = false;
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
                builder.setSmallIcon(R.drawable.logo_hainiu_32);
                builder.setContentTitle(getString(R.string.notification_title));
                builder.setContentText(getString(R.string.notification_text));
                builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                Intent i = new Intent(this, MainActivity_.class);
                PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(pi);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(ID_HCHO, builder.build());
            }

        } else if (hcho >= 0.5 && hcho < 0.6) {
//            drawable.setColor(Color.argb(255, 100, 0, 0));
            float red = (float) (175.0 * (hcho/0.6));
            float green = 255 - red;
            drawable.setColor(Color.argb(ALPHA, (int)red, (int)green, 0));
            this.tv_unit_middle.setText("污染");
            if(shouldNotifyUser){
                //user has been notified
                shouldNotifyUser = false;
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
                builder.setSmallIcon(R.drawable.logo_hainiu_32);
                builder.setContentTitle(getString(R.string.notification_title));
                builder.setContentText(getString(R.string.notification_text));
                builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                Intent i = new Intent(this, MainActivity_.class);
                PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(pi);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(ID_HCHO, builder.build());
            }
        } else {
            drawable.setColor(Color.argb(ALPHA, 255, 0, 0));
            this.tv_unit_middle.setText("重度污染");
            if(shouldNotifyUser){
                //user has been notified
                shouldNotifyUser = false;
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
                builder.setSmallIcon(R.drawable.logo_hainiu_32);
                builder.setContentTitle(getString(R.string.notification_title));
                builder.setContentText(getString(R.string.notification_text));
                builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                Intent i = new Intent(this, MainActivity_.class);
                PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(pi);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(ID_HCHO, builder.build());
            }
        }
        //test
        DecimalFormat dFormat = new DecimalFormat("0.000");
        this.tv_index_middle.setText(dFormat.format(hcho));
//        this.tv_index_middle.setText(hcho + "");

        /*
        pm10
         */
        this.tv_index_left.setText((int) pm10 + "");
        GradientDrawable drawable1 = (GradientDrawable) iv_circle_left.getBackground();
        if (pm10 < 150) {
            float red = (float) (255 * (pm10/150.0));
            float green = 255 - red;
            drawable1.setColor(Color.argb(ALPHA, (int)red, (int)green, 0));
        } else {
            drawable1.setColor(Color.argb(ALPHA, 255, 0, 0));
        }

         /*
        pm25
         */
        GradientDrawable drawable0 = (GradientDrawable) iv_circle_right.getBackground();
        this.tv_index_right.setText((int) pm + "");
        if(pm < 250){
            float red = 255 * (pm / 250);
            float green = 255 -  red;
            drawable0.setColor(Color.argb(ALPHA, (int)red, (int)green, 0));
        }else{
            drawable0.setColor(Color.argb(ALPHA, 255, 0, 0));
        }
        if (pm < 35) {
//            drawable0.setColor(Color.argb(255, 0, 255, 0));
            this.tv_unit_right.setText("优");
            if(!shouldNotifyUser_pm25){
                //pm2.5 index has reduced to normal level, dismiss notification
                shouldNotifyUser_pm25 = true;
                //dismiss notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(ID_PM25);
            }
        } else if (pm >= 35 && pm < 75) {
//            drawable0.setColor(Color.argb(255, 0, 0, 255));
            if(!shouldNotifyUser_pm25){
                //pm2.5 index has reduced to normal level, dismiss notification
                shouldNotifyUser_pm25 = true;
                //dismiss notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(ID_PM25);
            }
            this.tv_unit_right.setText("良");
        } else if (pm >= 75 && pm < 115) {
//            drawable0.setColor(Color.argb(255, 50, 0, 0));
            if(!shouldNotifyUser_pm25){
                //pm2.5 index has reduced to normal level, dismiss notification
                shouldNotifyUser_pm25 = true;
                //dismiss notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(ID_PM25);
            }
            this.tv_unit_right.setText("轻度污染");
        } else if (pm >= 115 && pm < 150) {
//            drawable0.setColor(Color.argb(255, 100, 0, 0));
            if(!shouldNotifyUser_pm25){
                //pm2.5 index has reduced to normal level, dismiss notification
                shouldNotifyUser_pm25 = true;
                //dismiss notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(ID_PM25);
            }
            this.tv_unit_right.setText("中度污染");
        } else if (pm >= 150 && pm < 250) {
//            drawable0.setColor(Color.argb(255, 255, 0, 0));
            this.tv_unit_right.setText("重度污染");
            if(shouldNotifyUser_pm25){
                //notify only once
                shouldNotifyUser_pm25 = false;
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
                builder.setSmallIcon(R.drawable.logo_hainiu_32);
                builder.setContentTitle(getString(R.string.notification_title));
                builder.setContentText(getString(R.string.notification_text_pm25));
                builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                Intent i = new Intent(this, MainActivity_.class);
                PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(pi);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(ID_PM25, builder.build());
            }

        } else {
//            drawable0.setColor(Color.argb(255, 255, 0, 0));
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
            this.tv_pm1_eval.setText("轻度");
        } else if (pm1 >= 115 && pm1 < 150) {
            this.tv_pm1_eval.setText("中度");
        } else if (pm1 >= 150 && pm1 < 250) {
            this.tv_pm1_eval.setText("重度");
        } else {
            this.tv_pm1_eval.setText("严重");
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

    private static final String SCREENSHOT_PATH = "/sdcard/";
    private class ScreenshotTask extends AsyncTask<Bitmap, Void, String> {

        @Override
        protected String doInBackground(Bitmap... params) {

            Bitmap bmp = params[0];
            String root_path = MainActivity.this.getFilesDir().getPath() + File.separator;//SCREENSHOT_PATH;


            root_path += "images" + File.separator;
            File testPath = new File(root_path);
            if(!testPath.exists()){
                testPath.mkdir();
            }
            String filename = MainActivity.this.getString(R.string.share_file_name) + ".jpg";
            File target = new File(root_path + filename);
            if(!target.exists()){
                try {
                    target.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            File target = new File(SCREENSHOT_PATH + filename);
            try {
                FileOutputStream fos = new FileOutputStream(target);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }


            return filename;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s == null){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.share_error_title);
                builder.setMessage(R.string.share_error_fail_to_get_screenshot);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create();
                builder.show();

            }else{
//                showShare("www.hainiutech.com", SCREENSHOT_PATH + s, getString(R.string.share_message));
                //share screenshot
                File imagePath = new File(MainActivity.this.getFilesDir(), "images");
                File newFile = new File(imagePath, s);
                Uri contentUri = FileProvider.getUriForFile(MainActivity.this, "com.worldlink.locker.fileprovider", newFile);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, "Share to ..."));
            }
            super.onPostExecute(s);
        }
    }

    private void showShare(String url, String image_path, String msg) {
        OnekeyShare oks = new OnekeyShare();

        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Log.d(LOG_TAG, "onComplete");
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {

            }

            @Override
            public void onCancel(Platform platform, int i) {
                Log.d(LOG_TAG, "onCancel");
            }
        });
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // 分享时Notification的图标和文字  2.5.9以后的版本不     调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.share));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(url);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(getString(R.string.share_message));
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImagePath(image_path);//确保SDcard下面存在此张图片
//        oks.setViewToShare(getWindow().getDecorView().getRootView());
        // url仅在微信（包括好友和朋友圈）中使用
//        oks.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment(msg);
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(url);
        // 启动分享GUI
        oks.show(this);
    }


    //connection handler
    private Handler connectHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            Toast.makeText(MainActivity.this, getString(R.string.toast_msg_reconnecting), Toast.LENGTH_SHORT).show();
            startScan();
            super.handleMessage(msg);
        }
    };

}//end of file
