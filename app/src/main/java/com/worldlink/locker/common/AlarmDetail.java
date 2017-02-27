package com.worldlink.locker.common;

import java.io.Serializable;

/**
 * Created by admin on 2016/3/19.
 */
public class AlarmDetail implements Serializable{

    private float alarmHum = 50;
    private float alarmTemp = 40;

    public float getAlarmHum() {
        return alarmHum;
    }

    public void setAlarmHum(float alarmHum) {
        this.alarmHum = alarmHum;
    }

    public float getAlarmTemp() {
        return alarmTemp;
    }

    public void setAlarmTemp(float alarmTemp) {
        this.alarmTemp = alarmTemp;
    }
}
