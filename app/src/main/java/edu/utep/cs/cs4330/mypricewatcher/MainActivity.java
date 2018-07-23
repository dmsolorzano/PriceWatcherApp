package edu.utep.cs.cs4330.mypricewatcher;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private PriceFinder tracker;
    private TextView productNameDisplay;
    private TextView initialPriceDisplay;
    private TextView currentPriceDisplay;
    private TextView percentageChangeDisplay;


    private Button updatePriceButton;
    private Button viewListButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tracker = new PriceFinder();

        // Restore state
        if (savedInstanceState != null) {
            tracker.product1.setProductName(savedInstanceState.getString("name"));
            tracker.product1.setInitialPrice(savedInstanceState.getDouble("initialPrice"));
            tracker.product1.setCurrentPrice(savedInstanceState.getDouble("currentPrice"));
            tracker.setPercentageChange(savedInstanceState.getInt("percent"));
        }

        productNameDisplay = findViewById(R.id.productNameDisplay);
        initialPriceDisplay = findViewById(R.id.initialPriceDisplay);
        currentPriceDisplay = findViewById(R.id.currentPriceDisplay);
        percentageChangeDisplay = findViewById(R.id.percentageChangeDisplay);



        updatePriceButton = findViewById(R.id.updatePriceButton);
        updatePriceButton.setOnClickListener(this::updatePriceClicked);
        viewListButton = findViewById(R.id.viewListButton);
        viewListButton.setOnClickListener(this::listButtonClicked);
        displayPrice();

    }

    public void listButtonClicked(View view) {
        Intent i = new Intent(this, ProductListActivity.class);

        /*Bundle args = new Bundle();
        args.putSerializable("ARRAYLIST",(Serializable)tracker.products);
        i.putExtra("BUNDLE",args);*/

        startActivity(i);
    }

    public void displayPrice(){
        productNameDisplay.setText(tracker.product1.getProductName());
        initialPriceDisplay.setText(String.format(Locale.getDefault(),
                "Initial Price: %.2f",tracker.product1.getInitialPrice()));
        currentPriceDisplay.setText(String.format(Locale.getDefault(),
                "Current Price: %.2f",tracker.product1.getCurrentPrice()));
        percentageChangeDisplay.setText(String.format(Locale.getDefault(),
                        "%d %% drop in price",tracker.getPercentageChange()));
    }

    public void updatePriceClicked(View view){
        Toast.makeText(this, "Update Tapped!", Toast.LENGTH_SHORT).show();
        tracker.updatePrice();// randomize current price from PriceFinder class
        tracker.calculatePercentageChange();
        displayPrice();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save our own state now
        outState.putString("name", tracker.product1.getProductName());
        outState.putDouble("initialPrice",tracker.product1.getInitialPrice());
        outState.putDouble("currentPrice",tracker.product1.getCurrentPrice());
        outState.putInt("percent", tracker.getPercentageChange());
    }

}