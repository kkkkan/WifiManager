package com.example.wifimanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final int MY_PERMISSIONS_REQUEST = 1;
    private Intent intent;
    private Button startButton;
    private Button stopButton;
    private Button listButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(this, WifiManageService.class);
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);
        listButton = (Button) findViewById(R.id.wifi_list);

        requestPermissions();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
            setButtonClickListener();
            return;
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CHANGE_NETWORK_STATE},
                MY_PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setButtonClickListener();
                } else {
                    showFinishDialog();
                }
                return;
            }
        }
    }

    private void setButtonClickListener() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "startButton click");
                startService(intent);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "stopButton click");
                stopService(intent);
            }
        });
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWifiListDialog();
            }
        });
    }

    private void showFinishDialog() {
        new AlertDialog.Builder(this).setMessage(getString(R.string.finish_message)).setCancelable(false).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).show();
    }

    private void showWifiListDialog() {
        final AlertDialog.Builder mListDlg = new AlertDialog.Builder(this);


        final WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        final List<WifiConfiguration> wifiConfigurations = wm.getConfiguredNetworks();

        final CharSequence[] lists;
        if (wifiConfigurations != null) {
            lists = new String[wifiConfigurations.size()];
            int i = 0;
            for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
                lists[i++] = wifiConfiguration.SSID;
            }
        } else {
            lists = new CharSequence[0];
        }


        //ダイアログ表示のための準備
        mListDlg.setTitle("登録済みwifi一覧");

        mListDlg.setItems(lists, null);
        mListDlg.setPositiveButton("OK", null);


        mListDlg.create().show();
    }

}
