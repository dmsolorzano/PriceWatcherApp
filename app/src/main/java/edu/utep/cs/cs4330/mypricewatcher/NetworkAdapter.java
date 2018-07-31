package edu.utep.cs.cs4330.mypricewatcher;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class NetworkAdapter {

    public void checkConnection(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            String content = null;
            URLConnection connection = null;
            try {
                connection =  new URL(urls[0]).openConnection();
                Scanner scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter("\\Z");
                content = scanner.next();
            }catch ( Exception ex ) {
                ex.printStackTrace();
            }
            Document document = null;
            document = Jsoup.parse(content); // connects and parses HTML of url
            Elements price = document.select("span[class=price-characteristic]");// grabs HTML tag
            //return Double.parseDouble(price.attr("content"));
            return content;
        }
        protected void onPostExecute(String result) {
            //ImageView img = (ImageView) findViewById(R.id.imageView);
            //img.setImageBitmap(result);
            //Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
//https://www.walmart.com/ip/Modway-Outdoor-Patio-Pillow-for-Indoor-Outdoor-Use-Multiple-Colors/54278157