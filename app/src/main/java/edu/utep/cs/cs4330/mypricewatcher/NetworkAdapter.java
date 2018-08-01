package edu.utep.cs.cs4330.mypricewatcher;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;


public class NetworkAdapter {

    /** Check to see if app is currently connected to the network*/
    public boolean checkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /** Checks to see if WiFi is enabled*/
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

    /** Must run in async thread*/
    public double createConnection(String url){
        String content = null;
        URLConnection connection;

        try {
            connection =  new URL(url).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
        }  catch (final MalformedURLException e) {
            throw new IllegalStateException("Bad URL: " + url, e);
        } catch (final IOException e) {
            Log.d("connect","Connection unavailable");
        }
        Document document = Jsoup.parse(content); // parses HTML that was retrieved
        Elements element = document.select("span[class=price-characteristic]");// grabs HTML tag
        Double price = Double.parseDouble(element.attr("content"));
        return price;
    }

    /** Connection used to connect to CS 4330 and get currentPrice, Must run in AsyncTask*/
    public double createConnection2(String url){
        String content = null;
        URLConnection connection;
        try {
            connection =  new URL(url).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
        }  catch (final MalformedURLException e) {
            throw new IllegalStateException("Bad URL: " + url, e);
        } catch (final IOException e) {
            Log.d("connect","Connection unavailable");
        }
        Document document = Jsoup.parse(content);
        Elements price = document.select("p");
        String text = price.text();
        Double sub = Double.parseDouble(text.substring(8));
        return sub;
    }

    /** AlertDialog displayed at run to prompt user to turn on WiFi settings*/
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
