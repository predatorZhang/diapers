package com.worldlink.locker.common;

import android.content.Context;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by admin on 2016/3/19.
 */
public class AlarmDetailManager implements Serializable{

    private static final String ALARMDETAIL = "ALARMDETAIL";

    public static AlarmDetail loadAlarmDetail(Context ctx) {
        AlarmDetail data = null;
        File file = new File(ctx.getFilesDir(), ALARMDETAIL);
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(ctx.openFileInput(ALARMDETAIL));
                data = (AlarmDetail) ois.readObject();
                ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (data == null) {
            data = new AlarmDetail();
        }

        return data;
    }

    public static void saveAlarmDetail(Context ctx, AlarmDetail data) {
        File file = new File(ctx.getFilesDir(), ALARMDETAIL);
        if (file.exists()) {
            file.delete();
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(ctx.openFileOutput(ALARMDETAIL, Context.MODE_PRIVATE));
            oos.writeObject(data);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
