package com.worldlink.locker.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.worldlink.locker.LockerApplication;
import com.worldlink.locker.R;
import com.worldlink.locker.common.ImageLoadTool;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;

@EActivity(R.layout.splash)
@Fullscreen
public class SplashActivity extends BaseActivity {

    @ViewById
    ImageView image;
    @ViewById
    TextView title;
    @ViewById
    View foreMask;
   @AnimationRes
    Animation entrance;

    Uri background = null;
    boolean mNeedUpdateUser = false;
    private final String TAG = "SplashActivity";

    private ImageLoadTool imageLoadTool = new ImageLoadTool();

    private boolean exit = false;

    Handler mHandler = new Handler() {
        @Override
                public void handleMessage(Message msg) {
                    // TODO Auto-generated method stub
                    if (msg.what == 0) {
                        foreMask.startAnimation(entrance);
                    } else if (msg.what == 1) {
                        next();
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);


                        switch(msg.arg1){
                            case 1:
                                builder.setMessage(R.string.alert_msg_bluetooth_disabled);
                                break;
                            case 2:
                                builder.setMessage(R.string.alert_msg_location_disabled);
                                break;
                        }
                builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.create().show();
            }
        }
    };


    @AfterViews
    void init() {
       /* float fTemp;
        try {
            byte[] temp = {0x00, (byte)0x88, (byte)0xcb, (byte)0xbf};
            DataInputStream dis = new DataInputStream(new
                    ByteArrayInputStream(temp));
             fTemp = dis.readFloat();

            byte[] temp2 = {(byte)0x41, (byte)0xc6, (byte)0x2d, (byte)0x78};
            DataInputStream dis2 = new DataInputStream(new
                    ByteArrayInputStream(temp2));
            float fTemp2 = dis2.readFloat();
               BigDecimal b =new BigDecimal(fTemp2);
            fTemp2 =b.setScale(1,BigDecimal.ROUND_HALF_UP).floatValue();

            int a = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        ImageSize imageSize = new ImageSize(LockerApplication.sWidthPix, LockerApplication.sHeightPix);
        image.setImageBitmap(imageLoadTool.imageLoader.loadImageSync("drawable://" + R.drawable.logo_hainiu1, imageSize));

        entrance.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(!exit){
                    mHandler.sendEmptyMessageDelayed(1, 200);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        //check bluetooth state
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null){
            if(!mBluetoothAdapter.isEnabled()){
                exit = true;
                foreMask.startAnimation(entrance);
                Message msg = mHandler.obtainMessage(-1);
                msg.arg1 = 1;
                mHandler.sendMessageDelayed(msg, 1500);
            }else{
                //check location state
                boolean gps_enabled = false;
                boolean network_enabled = false;

                try{
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                }catch(Exception e){
                    e.printStackTrace();
                }
                if(!gps_enabled && !network_enabled){
                    exit = true;
                    foreMask.startAnimation(entrance);
                    Message msg = mHandler.obtainMessage(-1);
                    msg.arg1 = 2;
                    mHandler.sendMessageDelayed(msg, 1500);

                }else{
                    mHandler.sendEmptyMessageDelayed(0, 200);
                }
            }
        }


    }

    void next() {

        Intent intent = new Intent(this, MainActivity_.class);
        startActivity(intent);
        overridePendingTransition(R.anim.scroll_in, R.anim.scroll_out);

      /*  Intent intent = new Intent(this, DeviceListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.scroll_in, R.anim.scroll_out);*/
        finish();
    }

}
