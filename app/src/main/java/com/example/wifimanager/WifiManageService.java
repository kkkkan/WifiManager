package com.example.wifimanager;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by kkkkan on 2017/12/29.
 */

public class WifiManageService extends Service {
    private final String TAG = "WifiManageService";
    private final int notificationId = 1;
    private final int notificationRequestCode = 2;
    private final String notificationChannelId = "WifiManageService Id";
    private BroadcastReceiver wifiChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, " wifiChangeBroadcastReceiver");

            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

            List<WifiConfiguration> wifiConfigurations = wm.getConfiguredNetworks();

            String action = intent.getAction();
            WifiInfo wifiInfo = wm.getConnectionInfo();

            Log.d(TAG, wifiInfo.getSSID());

            Log.d(TAG, "action is " + action);
            if (action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) { // "android.net.wifi.STATE_CHANGE"
                Log.d(TAG, "action!=null&&action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)");
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState() == null) {
                    Log.d(TAG, "info.getState()==null");
                    return;
                }
                switch (info.getState()) {
                    case DISCONNECTED:
                        Log.d(TAG, " DISCONNECTED");
                        break;
                    case SUSPENDED:
                        Log.d(TAG, "SUSPENDED");
                        break;
                    case CONNECTING:
                        Log.d(TAG, "CONNECTING");
                    case CONNECTED:
                        Log.d(TAG, "CONNECTED");
                        if (wifiConfigurations != null) {
                            for (WifiConfiguration configuration : wifiConfigurations) {
                                if (configuration.SSID.equals(wm.getConnectionInfo().getSSID())) {
                                    return;
                                }
                            }
                        }
                        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                            //サービスを起動する前に必ずactivityでパーミッションを得ているはず
                            wm.disableNetwork(wifiInfo.getNetworkId());
                        }
                        break;
                    case DISCONNECTING:
                        Log.d(TAG, "DISCONNECTING");
                        break;
                    case UNKNOWN:
                        Log.d(TAG, "Wifi connection state is UNKNOWN");
                        break;
                    default:
                        Log.d(TAG, "Wifi connection state is OTHER");
                        break;
                }

            }

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        //wifiのstateが変化したことをwifiChangeBroadcastReceiverが受け取れるように設定
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiChangeBroadcastReceiver, intentFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        //foregrandサービスにする。
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), notificationChannelId);
        builder.setSmallIcon(R.drawable.ic_insert_emoticon_black_24dp);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.notification_text));
        Intent intent1 = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationRequestCode, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(notificationId, notification);

        //強制終了時システムに再起動してもらう
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
