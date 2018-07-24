package edu.utep.cs.cs4330.mypricewatcher;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class ProductDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Intent i = getIntent();
        WebView wv = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = wv.getSettings();
        webSettings.setBuiltInZoomControls(true);
        // hardcoded get this from intent
        //wv.loadUrl("https://www.amazon.com/Wilson-Composite-High-School-Football/dp/B0009KF4SE/");
        wv.loadUrl(i.getStringExtra("url"));

    }
}
