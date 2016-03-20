package com.worldlink.locker.http;

import android.content.Context;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;


public class ApiClent {
    public final static String message_error = "服务器连接有问题";

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

}
