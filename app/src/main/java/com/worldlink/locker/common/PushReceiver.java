package com.worldlink.locker.common;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

/**
 * Created by chaochen on 14-12-5.
 */
public class PushReceiver extends XGPushBaseReceiver {

    public static String sNotify[] = new String[5];
    static int notifyId = 0;
    private static long sLastNotify = 0;
    NotificationCompat.Builder builder;

    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {
        Log.d("", "" + context);
    }

    public void onUnregisterResult(Context context, int i) {
        Log.d("", "" + context);
    }

    public void onSetTagResult(Context context, int i, String s) {
        Log.d("", "" + context);
    }

    public void onDeleteTagResult(Context context, int i, String s) {
        Log.d("", "" + context);
    }

    //�յ���Ϣ
    public void onTextMessage(Context context, XGPushTextMessage message) {
       /* try {
             if (!AccountInfo.getNeedPush(context) || !AccountInfo.isLogin(context)) {
                return;
            }

            JSONObject jsonCustom = new JSONObject(message.getCustomContent());


            if (jsonCustom.has("cancel")) {
                String cancelString = jsonCustom.optString("cancel");
                if (cancelString.equals("message")) {
                    cancel(context, true);
                } else {
                    cancel(context, false);
                }
                return;
            }

            String id = jsonCustom.optString("notification_id", "");
            String url = jsonCustom.optString("param_url", "");

            if (url.isEmpty()) {

                return;
            }

            String title = message.getTitle();
            String msg = message.getContent();

            msg = msg.replaceAll("<img src='(.*?)'/>", "[$1]");

            showNotify(context, title, msg, id, url);

        } catch (Exception e) {
         //   Global.errorLog(e);
        }*/
    }

    private void cancel(Context ctx, boolean cannelMessage) {
       /* String message = URLSpanNoUnderline.PATTERN_URL_MESSAGE;
        Pattern pattern = Pattern.compile(message);
        NotificationManager notificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        for (int i = 0; i < sNotify.length; ++i) {
            if (sNotify[i] == null) {
                continue;
            }

            Matcher matcher = pattern.matcher(sNotify[i]);
            if (matcher.find() == cannelMessage) {
                notificationManager.cancel(i);
            }
        }*/
    }

    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {
        Log.d("", "" + context);

    }

    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {
        Log.d("", "" + context);
    }

    private void showNotify(Context context, String title, String msg, String id, String url) {
       /* builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(msg);

        long time = Calendar.getInstance().getTimeInMillis();
        if (time - sLastNotify <= 10 * 1000) {
            builder.setDefaults(Notification.DEFAULT_LIGHTS);
        } else {
            builder.setDefaults(Notification.DEFAULT_ALL);
        }
        sLastNotify = time;

        Intent resultIntent = new Intent(MyPushReceiver.PushClickBroadcast);
        resultIntent.putExtra("data", url);
        resultIntent.putExtra("id", id);

        notifyId++;
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                context,
                notifyId,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );


        builder.setContentIntent(resultPendingIntent);
        builder.setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notifyIdInt = -1;
        for (int i = 0; i < sNotify.length; ++i) {
            if (url.equals(sNotify[i])) {
                notifyIdInt = i;
                sNotify[notifyIdInt] = url;
                break;
            }
        }

        if (notifyIdInt == -1) {
            notifyIdInt = notifyId % 5;
            sNotify[notifyIdInt] = url;
        }

        Pattern pattern = Pattern.compile(URLSpanNoUnderline.PATTERN_URL_MESSAGE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String globalKey = matcher.group(1);

*//*
            UsersListFragment.receiverMessagePush(globalKey, msg);
*//*
        }

        mNotificationManager.notify(notifyIdInt, builder.build());*/
    }
}