package com.worldlink.locker.common;

import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.worldlink.locker.R;

/**
 * Created by chaochen on 14-9-22.
 */
public class ImageLoadTool {

    public ImageLoader imageLoader = ImageLoader.getInstance();

    public static DisplayImageOptions enterOptions = new DisplayImageOptions
            .Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .build();

    public static DisplayImageOptions options = new DisplayImageOptions
            .Builder()
            .showImageOnLoading(R.drawable.icon)
            .showImageForEmptyUri(R.drawable.icon)
            .showImageOnFail(R.drawable.icon)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .build();

    public ImageLoadTool() {
    }

    public void loadImage(ImageView imageView, String url) {
        imageLoader.displayImage(Global.makeSmallUrl(imageView, url), imageView, options);
    }

    public void loadImage(ImageView imageView, String url, SimpleImageLoadingListener animate) {
        imageLoader.displayImage(Global.makeSmallUrl(imageView, url), imageView, options, animate);
    }

    public void loadImage(ImageView imageView, String url, DisplayImageOptions imageOptions) {
        imageLoader.displayImage(Global.makeSmallUrl(imageView, url), imageView, imageOptions);
    }

    public void loadImage(ImageView imageView, String url, DisplayImageOptions displayImageOptions, SimpleImageLoadingListener animate) {
        imageLoader.displayImage(url, imageView, displayImageOptions, animate);
    }

    public void loadImageFromUrl(ImageView imageView, String url) {
        imageLoader.displayImage(url, imageView, options);
    }

    public void loadImageFromUrl(ImageView imageView, String url, DisplayImageOptions displayImageOptions) {
        imageLoader.displayImage(url, imageView, displayImageOptions);
    }


}

