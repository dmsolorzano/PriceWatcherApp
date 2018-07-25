package edu.utep.cs.cs4330.mypricewatcher;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

public class ProductDetail extends AppCompatActivity {
    TextView productName;
    TextView initialPrice;
    TextView currentPrice;
    TextView percentageDecrease;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        productName = findViewById(R.id.name);
        initialPrice = findViewById(R.id.initialPrice);
        currentPrice = findViewById(R.id.currentPrice);
        percentageDecrease = findViewById(R.id.percentageDrop);

        Intent i = getIntent();
        WebView wv = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = wv.getSettings();
        webSettings.setBuiltInZoomControls(true);

        Bundle bundle = i.getExtras();
        initialPrice.setText(String.format("Initial Price: $%.2f",(bundle.getDouble("INITIAL_PRICE"))));
        //currentPrice.setText(String.format("Current Price: $%.2f",(bundle.getDouble("CURRENT_PRICE"))));
        percentageDecrease.setText(String.format("Percentage Drop: %d%%",(bundle.getInt("PERCENTAGE"))));
        if (URLUtil.isValidUrl(bundle.getString("URL"))) {
            wv.loadUrl(bundle.getString("URL"));
        }
        else
            showErrorDialog();
    }
    public void showErrorDialog(){
        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(this);
        //LayoutInflater inflater = this.getLayoutInflater();
        //View dialogView = inflater.inflate(R.layout.delete_dialog, null);
        //dialogBuilder.setView (dialogView);
        dialogBuilder.setTitle("ERROR");
        dialogBuilder.setMessage("Link for product is not valid. \nWeb page will not load");
        dialogBuilder.setPositiveButton("OK", (DialogInterface dialog, int whichButton) -> {
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
}
