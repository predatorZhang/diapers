package com.worldlink.locker.activity;

import android.content.Intent;
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
import org.androidannotations.annotations.NoTitle;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;

@EActivity(R.layout.splash)
@NoTitle
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


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (msg.what == 0) {
                foreMask.startAnimation(entrance);
            } else if (msg.what == 1) {
                next();
            }
        }
    };


    @AfterViews
    void init() {

        ImageSize imageSize = new ImageSize(LockerApplication.sWidthPix, LockerApplication.sHeightPix);
        image.setImageBitmap(imageLoadTool.imageLoader.loadImageSync("drawable://" + R.drawable.splash, imageSize));

        entrance.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHandler.sendEmptyMessageDelayed(1, 500);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mHandler.sendEmptyMessageDelayed(0, 900);

    }

    void next() {
        Intent intent = new Intent(this, MainActivity_.class);
        startActivity(intent);
        overridePendingTransition(R.anim.scroll_in, R.anim.scroll_out);
        finish();
    }

}
