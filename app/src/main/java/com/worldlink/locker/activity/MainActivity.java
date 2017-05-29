package com.worldlink.locker.activity;


import android.app.ActionBar;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import com.zaaach.citypicker.CityPickerActivity;
import com.zaaach.citypicker.utils.StringUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.math.BigDecimal;


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


    private static final String TAG = "MainActivity";


    private PopupWindow weatherPopupWindow;
    private PopupWindow pm25PopupWindow;
    private ImageLoadTool imageLoadTool = new ImageLoadTool();

    private static final int REQUEST_CODE_PICK_CITY = 0;

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
        imageLoadTool.loadImage(this.ib_weather, "http://files.heweather.com/cond_icon/"
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
      /*   params_tv_left =new ViewGroup.LayoutParams(tv_index_left.getLayoutParams());
         params_tv_title_left = new ViewGroup.LayoutParams(tv_title_left.getLayoutParams());
         params_tv_unit_left = new ViewGroup.LayoutParams(tv_unit_left.getLayoutParams());

         params_tv_right =  new ViewGroup.LayoutParams(tv_index_right.getLayoutParams());
         params_tv_title_right =  new ViewGroup.LayoutParams(tv_title_right.getLayoutParams());
         params_tv_unit_right =  new ViewGroup.LayoutParams(tv_unit_right.getLayoutParams());

        params_tv_middle =  new ViewGroup.LayoutParams(tv_index_middle.getLayoutParams());
        params_tv_title_middle =  new ViewGroup.LayoutParams(tv_title_middle.getLayoutParams());
        params_tv_unit_middle =  new ViewGroup.LayoutParams(tv_unit_middle.getLayoutParams());

        params_iv_circle_left =  new ViewGroup.LayoutParams(iv_circle_left.getLayoutParams());
        params_iv_circle_left_inner =  new ViewGroup.LayoutParams(iv_circle_left_inner.getLayoutParams());
        params_iv_circle_left_outer =  new ViewGroup.LayoutParams(iv_circle_left_outer.getLayoutParams());

        params_iv_circle_middle =  new ViewGroup.LayoutParams(iv_circle_middle.getLayoutParams());
        params_iv_circle_middle_inner =  new ViewGroup.LayoutParams(iv_circle_middle_inner.getLayoutParams());
        params_iv_circle_middle_outer = new ViewGroup.LayoutParams( iv_circle_middle_outer.getLayoutParams());

        params_iv_circle_right =  new ViewGroup.LayoutParams(iv_circle_right.getLayoutParams());
        params_iv_circle_right_inner =  new ViewGroup.LayoutParams(iv_circle_right_inner.getLayoutParams());
        params_iv_circle_right_outer =  new ViewGroup.LayoutParams(iv_circle_right_outer.getLayoutParams());

        params_rl_left =  new ViewGroup.LayoutParams(rl_left.getLayoutParams());
        params_rl_middle =  new ViewGroup.LayoutParams(rl_middle.getLayoutParams());
        params_rl_right =  new ViewGroup.LayoutParams(rl_right.getLayoutParams());*/

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
    }

    private boolean flagLeft = false;

    private RelativeLayout hideLayout;

    View.OnClickListener onLeftClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(transition){
                //animation hasn't finished
                return;
            }
            final RelativeLayout tmpHidenLayout = hideLayout;

            transition = true;
            flagLeft = !flagLeft;
            flagRight = false;
            rl_right.setOnClickListener(null);
            rl_middle.setOnClickListener(null);
            rl_right.setOnClickListener(onRightClick);
            v.setVisibility(View.INVISIBLE);
            hideLayout.setVisibility(View.INVISIBLE);
            anim_1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_left_out);
            Animation anim_tmp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_middle_trans_left);
            v.startAnimation(anim_1);
            hideLayout.startAnimation(anim_tmp);

            anim_1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    anim_1 = null;
                    try {
                        if (flagLeft) {
                            tv_index_left.setLayoutParams(params_tv_middle);
                            tv_index_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                            tv_index_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            tv_index_middle.setLayoutParams(params_tv_left);
                            tv_index_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            tv_index_right.setLayoutParams(params_tv_right);

                            tv_title_left.setLayoutParams(params_tv_title_middle);
                            tv_title_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            tv_title_middle.setLayoutParams(params_tv_title_left);
                            tv_title_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                            tv_title_right.setLayoutParams(params_tv_title_middle);
                            tv_title_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                            tv_unit_left.setLayoutParams(params_tv_unit_middle);
                            tv_unit_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            tv_unit_middle.setLayoutParams(params_tv_unit_left);
                            tv_unit_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                            tv_unit_right.setLayoutParams(params_tv_unit_middle);
                            tv_unit_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                            iv_circle_left.setLayoutParams(params_iv_circle_middle);
                            iv_circle_left_inner.setLayoutParams(params_iv_circle_middle_inner);
                            iv_circle_left_outer.setLayoutParams(params_iv_circle_middle_outer);

                            iv_circle_middle.setLayoutParams(params_iv_circle_left);
                            iv_circle_middle_inner.setLayoutParams(params_iv_circle_left_inner);
                            iv_circle_middle_outer.setLayoutParams(params_iv_circle_left_outer);

                            iv_circle_right.setLayoutParams(params_iv_circle_right);
                            iv_circle_right_inner.setLayoutParams(params_iv_circle_right_inner);
                            iv_circle_right_outer.setLayoutParams(params_iv_circle_right_outer);

                            rl_left.setLayoutParams(params_rl_middle);
                            rl_middle.setLayoutParams(params_rl_left);
                            rl_right.setLayoutParams(params_rl_right);

                            rl_left.setOnClickListener(null);
                            rl_middle.setOnClickListener(onLeftClick);

                            hideLayout = rl_left;

                        } else {
                            tv_index_left.setLayoutParams(params_tv_left);
                            tv_index_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            tv_index_middle.setLayoutParams(params_tv_middle);
                            tv_index_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                            tv_index_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            tv_index_right.setLayoutParams(params_tv_right);

                            tv_title_left.setLayoutParams(params_tv_title_left);
                            tv_title_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                            tv_title_middle.setLayoutParams(params_tv_title_middle);
                            tv_title_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            tv_title_right.setLayoutParams(params_tv_title_middle);
                            tv_title_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                            tv_unit_left.setLayoutParams(params_tv_unit_left);
                            tv_unit_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                            tv_unit_middle.setLayoutParams(params_tv_unit_middle);
                            tv_unit_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            tv_unit_right.setLayoutParams(params_tv_unit_middle);
                            tv_unit_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                            iv_circle_left.setLayoutParams(params_iv_circle_left);
                            iv_circle_left_inner.setLayoutParams(params_iv_circle_left_inner);
                            iv_circle_left_outer.setLayoutParams(params_iv_circle_left_outer);

                            iv_circle_middle.setLayoutParams(params_iv_circle_middle);
                            iv_circle_middle_inner.setLayoutParams(params_iv_circle_middle_inner);
                            iv_circle_middle_outer.setLayoutParams(params_iv_circle_middle_outer);

                            iv_circle_right.setLayoutParams(params_iv_circle_right);
                            iv_circle_right_inner.setLayoutParams(params_iv_circle_right_inner);
                            iv_circle_right_outer.setLayoutParams(params_iv_circle_right_outer);

                            rl_left.setLayoutParams(params_rl_left);
                            rl_middle.setLayoutParams(params_rl_middle);
                            rl_right.setLayoutParams(params_rl_right);

                            rl_middle.setOnClickListener(null);
                            rl_left.setOnClickListener(onLeftClick);

                            hideLayout = rl_middle;
                        }

                        anim_2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_middle_in);
                        anim_2.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {

                                rl_left.setVisibility(View.VISIBLE);
                                rl_right.setVisibility(View.VISIBLE);
                                rl_middle.setVisibility(View.VISIBLE);
                                anim_2 = null;
                                transition = false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        tmpHidenLayout.startAnimation(anim_2);

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


    private boolean flagRight = false;
    View.OnClickListener onRightClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(transition){
                //animation hasn't finished
                return;
            }
            flagLeft = false;
            rl_left.setOnClickListener(null);
            rl_middle.setOnClickListener(null);
            rl_left.setOnClickListener(onLeftClick);
            transition = true;
            flagRight = !flagRight;
            final RelativeLayout tmpHidenLayout = hideLayout;
            v.setVisibility(View.INVISIBLE);
            hideLayout.setVisibility(View.INVISIBLE);

            anim_1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_right_out);
            Animation anim_tmp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_middle_trans_right);
            v.startAnimation(anim_1);
            hideLayout.startAnimation(anim_tmp);

            anim_1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    anim_1 = null;

                    if (flagRight) {
                        tv_index_right.setLayoutParams(params_tv_middle);
                        tv_index_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                        tv_index_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        tv_index_middle.setLayoutParams(params_tv_right);
                        tv_index_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        tv_index_left.setLayoutParams(params_tv_left);

                        tv_title_right.setLayoutParams(params_tv_title_middle);
                        tv_title_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        tv_title_middle.setLayoutParams(params_tv_title_right);
                        tv_title_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                        tv_title_left.setLayoutParams(params_tv_title_left);
                        tv_title_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);


                        tv_unit_right.setLayoutParams(params_tv_unit_middle);
                        tv_unit_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        tv_unit_middle.setLayoutParams(params_tv_unit_right);
                        tv_unit_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                        tv_unit_left.setLayoutParams(params_tv_unit_left);
                        tv_unit_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                        iv_circle_right.setLayoutParams(params_iv_circle_middle);
                        iv_circle_right_inner.setLayoutParams(params_iv_circle_middle_inner);
                        iv_circle_right_outer.setLayoutParams(params_iv_circle_middle_outer);


                        iv_circle_middle.setLayoutParams(params_iv_circle_right);
                        iv_circle_middle_inner.setLayoutParams(params_iv_circle_right_inner);
                        iv_circle_middle_outer.setLayoutParams(params_iv_circle_right_outer);

                        iv_circle_left.setLayoutParams(params_iv_circle_left);
                        iv_circle_left_inner.setLayoutParams(params_iv_circle_left_inner);
                        iv_circle_left_outer.setLayoutParams(params_iv_circle_left_outer);

                        rl_right.setLayoutParams(params_rl_middle);
                        rl_middle.setLayoutParams(params_rl_right);
                        rl_left.setLayoutParams(params_rl_left);

                        rl_right.setOnClickListener(null);
                        rl_middle.setOnClickListener(onRightClick);
                        hideLayout = rl_right;
                    } else {
                        tv_index_right.setLayoutParams(params_tv_right);
                        tv_index_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        tv_index_middle.setLayoutParams(params_tv_middle);
                        tv_index_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                        tv_index_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        tv_index_left.setLayoutParams(params_tv_left);

                        tv_title_right.setLayoutParams(params_tv_title_right);
                        tv_title_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                        tv_title_middle.setLayoutParams(params_tv_title_middle);
                        tv_title_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        tv_title_left.setLayoutParams(params_tv_title_left);
                        tv_title_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                        tv_unit_right.setLayoutParams(params_tv_unit_right);
                        tv_unit_right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                        tv_unit_middle.setLayoutParams(params_tv_unit_middle);
                        tv_unit_middle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        tv_unit_left.setLayoutParams(params_tv_unit_left);
                        tv_unit_left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                        iv_circle_right.setLayoutParams(params_iv_circle_right);
                        iv_circle_right_inner.setLayoutParams(params_iv_circle_right_inner);
                        iv_circle_right_outer.setLayoutParams(params_iv_circle_right_outer);

                        iv_circle_middle.setLayoutParams(params_iv_circle_middle);
                        iv_circle_middle_inner.setLayoutParams(params_iv_circle_middle_inner);
                        iv_circle_middle_outer.setLayoutParams(params_iv_circle_middle_outer);

                        iv_circle_left.setLayoutParams(params_iv_circle_left);
                        iv_circle_left_inner.setLayoutParams(params_iv_circle_left_inner);
                        iv_circle_left_outer.setLayoutParams(params_iv_circle_left_outer);


                        rl_right.setLayoutParams(params_rl_right);
                        rl_middle.setLayoutParams(params_rl_middle);

                        rl_middle.setOnClickListener(null);
                        rl_right.setOnClickListener(onRightClick);
                        rl_left.setLayoutParams(params_rl_left);

                        hideLayout = rl_middle;

                    }

                    anim_2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_middle_in);
                    anim_2.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            rl_left.setVisibility(View.VISIBLE);
                            rl_right.setVisibility(View.VISIBLE);
                            rl_middle.setVisibility(View.VISIBLE);
                            anim_2 = null;
                            transition = false;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    tmpHidenLayout.startAnimation(anim_2);

                  /*  if (flagRight) {
                        rl_right.setVisibility(View.VISIBLE);
                        tmpHidenLayout.startAnimation(anim_2);
                    } else {
                        tmpHidenLayout.setVisibility(View.VISIBLE);
                        rl_right.startAnimation(anim_2);
                    }*/
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

            });
        }
    };

    /*
        @Click
    */
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

    public BleManager bleManager;
    public final String DEVICE_NAME = "HN_PM2.5_CH2O";
    public final String UUID_SERVICE = "0000fff1-0000-1000-8000-00805f9b34fb";
    public final String UUID_NOTIFY = "0000fffa-0000-1000-8000-00805f9b34fb";

    public boolean mBleSupported = false;

    @AfterViews
    public void init() {

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
        startScan();

        ib_device.setImageResource(R.drawable.dv_disconneted);
        XGPushManager.registerPush(this, "*");
        updateNotifyService();
        pushInXiaomi();
        startUpdateService();

        startPostion();

        iv_circle_middle.setBackgroundResource(R.drawable.circle5);
        iv_circle_left.setBackgroundResource(R.drawable.circle5);
        iv_circle_right.setBackgroundResource(R.drawable.circle5);

        iv_background.setImageResource(bg_ids[2]);
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

    }

    private AMapLocationClient mLocationClient;

    private void startPostion() {
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
        if (!bleManager.isConnectingOrConnected()) {
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
                                ib_device.setImageResource(R.drawable.dv_disconneted);
                                Toast.makeText(MainActivity.this.getApplication(), "未找到甲醛检测仪", Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onConnectSuccess(final BluetoothGatt gatt, int status) {
                        Log.i(TAG, "连接成功！");
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gatt.discoverServices();
                                Toast.makeText(MainActivity.this.getApplication(), "设备连接已连接", Toast.LENGTH_LONG).show();
                                ib_device.setImageResource(R.drawable.dv_connected);

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

                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    Toast.makeText(getApplication(), "设备连接已断开，请重连", Toast.LENGTH_LONG).show();
                                    ib_device.setImageResource(R.drawable.dv_disconneted);
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
        if (bleManager.isConnectingOrConnected()) {
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
                            float pm, float pm10, float hcho, float cell) {

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


}//end of file
