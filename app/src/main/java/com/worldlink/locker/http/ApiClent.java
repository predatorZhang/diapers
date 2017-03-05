package com.worldlink.locker.http;

import android.content.Context;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;


public class ApiClent {
    public final static String message_error = "服务器连接有问题";
    public final static String heFengApiKey = "edeabc98d08844fd801d4b9b967b6c48";
    public final static String netIp = "http://pv.sohu.com/cityjson?ie=utf-8";

    public interface ClientCallback {
        abstract void onSuccess(Object data);

        abstract void onFailure(String message);

        abstract void onError(Exception e);
    }

    //获取当前用户的信息
    public static void updateApp(Context appContext, final ClientCallback callback) {

        Ion.with(appContext)
                .load(NetUrl.URL_UPDATE_APP)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            callback.onError(e);

                            return;
                        }
                        callback.onSuccess(result);
                    }
                });
    }

    //TODO LIST：服务器端API开发
    public static void getWeather(Context appContext, String ip,final ClientCallback callback) {

        Ion.with(appContext)
                .load(NetUrl.URL_HEFENG_WEATHER)
                .setBodyParameter("key", heFengApiKey)
                .setBodyParameter("city", ip)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            callback.onError(e);

                            return;
                        }
                        callback.onSuccess(result);
                    }
                });
    }

    public static void getNetIp(Context appContext, final ClientCallback callback) {

        Ion.with(appContext)
                .load(netIp)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            callback.onError(e);
                            return;
                        }
                        callback.onSuccess(result);
                    }
                });
    }
}
