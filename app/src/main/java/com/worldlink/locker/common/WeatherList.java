package com.worldlink.locker.common;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by didik on 2017/2/16.
 */

public class WeatherList {

    private List<HeWeather5Bean> HeWeather5;

    public List<HeWeather5Bean> getHeWeather5() {
        return HeWeather5;
    }

    public void setHeWeather5(List<HeWeather5Bean> HeWeather5) {
        this.HeWeather5 = HeWeather5;
    }

    public static WeatherList fromJson(String json) {

        Gson gson = new Gson();
        WeatherList weatherList = gson.fromJson(json, WeatherList.class);
        return weatherList;
    }
    }
