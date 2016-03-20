package com.worldlink.locker;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.worldlink.locker.activity.UpdateTipActivity;
import com.worldlink.locker.common.Global;
import com.worldlink.locker.common.RestResponse;
import com.worldlink.locker.http.ApiClent;

import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;

/*
 * 该服务的作用是检查是否有新版本程序，有的话就升级
 */
public class UpdateService extends Service {

    public static final String EXTRA_BACKGROUND = "EXTRA_BACKGROUND";
    public static final String EXTRA_WIFI = "EXTRA_WIFI";
    public static final String EXTRA_DEL_OLD_APK = "EXTRA_DEL_OLD_APK";

    public static final int PARAM_INSTALL_APK = 1;
    public static final int PARAM_STOP_SELF = 2;
    public static final int PARAM_START_DOWNLOAD = 3;
    UpdateService.UpdateInfo mUpdateInfo;
    private boolean isChecking = false;
    private CompleteReceiver completeReceiver;
    private Dialog noticeDialog;
    private DownloadManager downloadManager;
    private long enqueue = 0;
    private String TAG = "UpdateService";
    private boolean background;

    public UpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        int param = intent.getIntExtra("data", 0);
        if (param == PARAM_INSTALL_APK) {
            installApk();
            return START_REDELIVER_INTENT;
        } else if (param == PARAM_STOP_SELF) {
            stopSelf();
            return START_REDELIVER_INTENT;
        } else if (param == PARAM_START_DOWNLOAD) {
            downloadApp();
            return START_REDELIVER_INTENT;
        }

        if (intent.getBooleanExtra(EXTRA_DEL_OLD_APK, false)) {
            deleteOldApk();
        }

        run(intent);

        return START_REDELIVER_INTENT;
    }

    private void downloadApp() {
        try {
            if (enqueue == 0) {

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mUpdateInfo.url))
                        .setTitle("IDiapers")
                        .setDescription("下载IDiapers" + mUpdateInfo.versionName)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mUpdateInfo.apkName())
                        .setVisibleInDownloadsUi(false);
                enqueue = downloadManager.enqueue(request);
            }
        } catch (Exception e) {
/*
            Toast.makeText(this, R.string.no_system_download_service, Toast.LENGTH_LONG).show();
*/
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        completeReceiver = new CompleteReceiver();

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        registerReceiver(completeReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(completeReceiver);
        super.onDestroy();
    }

    private void deleteOldApk() {
        deleteFile(getVersion());

        SharedPreferences share = getSharedPreferences("version", Context.MODE_PRIVATE);
        int jumpVersion = share.getInt("jump", 0);

        if (jumpVersion != 0) {
            deleteFile(jumpVersion);
        }
    }

    private void deleteFile(int version) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = UpdateInfo.makeFileName(version);
        File file = new File(path, fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    private boolean checkNetwork(Intent intent) {
        if (!Global.isConnected(this)) {
            return false;
        }

        return !(intent.getBooleanExtra(EXTRA_WIFI, false) &&
                !Global.isWifiConnected(this));

    }

    private ApiClent.ClientCallback updateAppCallback = new ApiClent.ClientCallback() {
        @Override
        public void onSuccess(Object data) {

            String sData = data.toString();
            RestResponse resp = RestResponse.parseJson(sData);
            if (resp == null) {
                stopSelf();
                return;
            }
            String message = resp.getMessage();

            if (resp.isSuccess()) {

                mUpdateInfo = UpdateInfo.parseJson(message);
                int versionCode = getVersion();
                if (mUpdateInfo.newVersion > versionCode) {

                    SharedPreferences share = getSharedPreferences("version", Context.MODE_PRIVATE);
                    int jumpVersion = share.getInt("jump", 0);

                    if (mUpdateInfo.newVersion > jumpVersion) {
                        Intent intentUpdateTipActivity = new Intent(UpdateService.this, UpdateTipActivity.class);
                        intentUpdateTipActivity.putExtra("data", mUpdateInfo);
                        intentUpdateTipActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentUpdateTipActivity);
                    }
                } else {
                    if (!background) {
                        Toast.makeText(UpdateService.this, "你的软件已经是最新版本", Toast.LENGTH_LONG).show();
                    }
                    stopSelf();
                }

            }
        }

        @Override
        public void onFailure(String message) {
            stopSelf();
        }

        @Override
        public void onError(Exception e) {
            Log.d(TAG, "" + e);
            stopSelf();
        }
    };

    private void run(Intent intent) {
        if (isChecking) {
            stopSelf();
            return;
        }
        isChecking = true;

        background = intent.getBooleanExtra(EXTRA_BACKGROUND, false);
        if (!checkNetwork(intent)) {
            if (!background) {
                Toast.makeText(this, "没有网络连接", Toast.LENGTH_LONG).show();
            }

            isChecking = false;
            stopSelf();
            return;
        }

        //TODO LIST:请求服务器端的获得当前最新的版本信息
        ApiClent.updateApp(this,updateAppCallback);
    }

    private int getVersion() {
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

        }

        return 1;
    }

    private boolean isDownload() {
        return mUpdateInfo.apkFile().exists();
    }

    private void installApk() {
        File apkfile = mUpdateInfo.apkFile();
        if (!apkfile.exists()) {
            stopSelf();
            return;
        }

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        startActivity(i);

        stopSelf();
    }

    public static class UpdateInfo implements Serializable {
        public int newVersion;
        public String newMessage;
        public int required;
        public String url;
        public String versionName;

        public UpdateInfo(JSONObject response) {
            newVersion = response.optInt("build");
            newMessage = response.optString("message");
            required = response.optInt("required");
            url = response.optString("url");
            versionName = response.optString("version");
        }

        public static UpdateInfo parseJson(String json)
        {
            Gson gson = new Gson();
            UpdateInfo updateInfo = gson.fromJson(json, UpdateInfo.class);
            return updateInfo;
        }

        public static String makeFileName(int version) {
            return "IDiapers" + version + ".apk";
        }

        public String apkName() {
            return makeFileName(newVersion);
        }

        public File apkFile() {
            return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apkName());
        }
    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(enqueue);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int culumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(culumnIndex)) {
                    installApk();
                }
            }

            stopSelf();
        }
    }
}
