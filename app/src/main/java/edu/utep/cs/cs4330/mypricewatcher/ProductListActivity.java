package edu.utep.cs.cs4330.mypricewatcher;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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

        registerForContextMenu(listView);

        // Responds to single click within listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product product = tracker.products.get((int) id); // product that created
                Toast.makeText(getApplicationContext(), "Clicked: " + product.getCurrentPrice(), Toast.LENGTH_LONG).show();
                send(product); //to webview activity
            }
        });
    }

    public void send(Product product){
        Intent i = new Intent(this, ProductDetail.class);
        i.putExtra("url",product.getUrl());
        startActivity(i);
    }

    // Context Menu methods
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        menu.add(0, v.getId(), 0, "Edit");
        menu.add(0, v.getId(), 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getTitle()=="Edit") {
            Toast.makeText(getApplicationContext(), "Edit Clicked", Toast.LENGTH_LONG).show();
        }
        if(item.getTitle()=="Delete")
            Toast.makeText(getApplicationContext(), "Delete Clicked", Toast.LENGTH_LONG).show();
        return true;
    }

    // Menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0,0,0,"Add new product");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return menuChoice(item);
    }

    private void updatePriceButtonClicked(View view) {
        Toast.makeText(this, "Update Tapped!", Toast.LENGTH_SHORT).show();
        tracker.updatePrice();
        tracker.calculatePercentageChange();
        adapter.notifyDataSetChanged(); //updates ListView
    }

    private boolean menuChoice(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Toast.makeText(this, "Choice 1", Toast.LENGTH_LONG).show();
                // pop up dialog and create new product
                showAddProductDialog();
                return true;
        }
        return false;
    }

    public void showAddProductDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        EditText url = (EditText) dialogView.findViewById(R.id.url);
        EditText productName = (EditText) dialogView.findViewById(R.id.productName);


        dialogBuilder.setTitle("Create new product to track");
        dialogBuilder.setMessage("Enter URL below");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                Product e = new Product(productName.getText().toString(), url.getText().toString());
                tracker.products.add(e);
                adapter.notifyDataSetChanged();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


    //TODO save state
}
