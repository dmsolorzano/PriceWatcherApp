package edu.utep.cs.cs4330.mypricewatcher;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;


public class NetworkAdapter {

    public boolean checkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public void checkWifi(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()) {
            Toast.makeText(context, "Wifi is enabled", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Wifi is disabled", Toast.LENGTH_SHORT).show();
            createNetErrorDialog(context);
        }
    }

    public boolean checkDestinationAddress(){
        return true;
    }

    private void createNetErrorDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("This application runs better on your WiFi connection. " +
                "Please turn on WiFi in Settings.");
        builder.setTitle("Wifi is disabled");
        builder.setPositiveButton("Settings", (dialog, id) -> {
                    Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    context.startActivity(i);
                });
        builder.setNegativeButton("Cancel",
                (dialog, id) -> {
                    //MainActivity.this.finish();
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
