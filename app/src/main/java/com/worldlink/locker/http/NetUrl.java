package com.worldlink.locker.http;

/**
 * Created by admin on 2015/4/29.
 */
public class NetUrl
{

/*
    public static String HOST="http://www.hainiutech.com:2046/iot/";
*/
    public static String HOST="http://www.hainiutech.com:2016/iot/";
    public static String URL_UPDATE_APP;

    static {
        NetUrl.URL_UPDATE_APP = HOST+"rs/app/update/";
    }
}
