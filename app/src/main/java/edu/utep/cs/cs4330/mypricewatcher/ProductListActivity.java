package edu.utep.cs.cs4330.mypricewatcher;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ProductListActivity extends AppCompatActivity {
    PriceFinder tracker = new PriceFinder();
    private Button updatePriceButton;
    ProductsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        updatePriceButton = findViewById(R.id.updatePriceButton);
        updatePriceButton.setOnClickListener(this::updatePriceButtonClicked);

        // Construct the data source
        ArrayList<Product> productsList = tracker.products;

        // Create the adapter to convert the array to views
        adapter = new ProductsAdapter(this, productsList);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.mainListView);
        listView.setAdapter(adapter);

    }

    private void updatePriceButtonClicked(View view) {
        Toast.makeText(this, "Update Tapped!", Toast.LENGTH_SHORT).show();
        tracker.updatePrice();
        tracker.calculatePercentageChange();
        adapter.notifyDataSetChanged(); //updates ListView
    }
    //TODO save state
}
