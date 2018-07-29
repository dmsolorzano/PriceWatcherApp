package edu.utep.cs.cs4330.mypricewatcher;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

public class MainActivity extends AppCompatActivity {
    PriceFinder tracker = new PriceFinder();
    private Button updatePriceButton;
    ProductsAdapter adapter;
    ListView listView;
    ArrayList<Product> productsList = tracker.products;
    DBAdapter db = new DBAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db.open();

//        db.insertProduct("Basketball", 21.99, 21.99, "https://www.amazon.com/Wilson-Composite-High-School-Football/dp/B0009KF4SE/");
//        db.insertProduct("Baseball", 21.99, 21.99, "https://www.amazon.com/Wilson-Composite-High-School-Football/dp/B0009KF4SE/");
//        db.insertProduct("Football", 21.99, 21.99, "https://www.amazon.com/Wilson-Composite-High-School-Football/dp/B0009KF4SE/");
        //db.close();

        //clear the database
        Cursor c = db.getAllProducts();
        if (c.moveToFirst()) {
            do {
                Product x = new Product(c.getString(0),
                        Double.parseDouble(c.getString(1)),
                        Double.parseDouble(c.getString(2)),
                        c.getString(3));

                productsList.add(x);
            } while (c.moveToNext());
        }
        db.close();

        //restore state
        /*if(savedInstanceState!=null) {
            productsList = (ArrayList<Product>) savedInstanceState.getSerializable("listViewState");
        }*/

        //productsList = tracker.products;
        updatePriceButton = findViewById(R.id.updatePriceButton);
        updatePriceButton.setOnClickListener(this::updatePriceButtonClicked);


        // Create the adapter to convert the array to views
        adapter = new ProductsAdapter(this, productsList);

        //listView.onRestoreInstanceState(listInstanceState); //restore instance state

        // Attach the adapter to a ListView
        listView = (ListView) findViewById(R.id.mainListView);
        listView.setAdapter(adapter);

        registerForContextMenu(listView);

        // Responds to single click within listView
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Product product = tracker.products.get((int) id); // product that created
            Toast.makeText(getApplicationContext(), "Clicked: " + product.getUrl(), Toast.LENGTH_LONG).show();
            onListViewClick(product); //to webview activity
        });
    }

    private void updatePriceButtonClicked(View view) {
        tracker.updatePrice();
        tracker.calculatePercentageChange();
        adapter.notifyDataSetChanged(); //updates ListView
    }

    public void onListViewClick(Product product){
        Intent i = new Intent(this, ProductDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putDouble("CURRENT_PRICE", product.getCurrentPrice());
        extras.putDouble("INITIAL_PRICE", product.getInitialPrice());
        extras.putInt("PERCENTAGE", product.getPercentageChange());
        extras.putString("URL", product.getUrl());
        i.putExtras(extras);
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
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(item.getTitle()=="Edit") {
            showEditProductDialog(info.position);// show dialog
        }
        if(item.getTitle()=="Delete") {
            showDeleteProductDialog(info.position);// show dialog
        }
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

    private boolean menuChoice(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                // pop up dialog and create new product
                showAddProductDialog();
                return true;
        }
        return false;
    }

    public void showAddProductDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.options_dialog, null);
        EditText url = (EditText) dialogView.findViewById(R.id.url);
        EditText productName = (EditText) dialogView.findViewById(R.id.productName);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Create new product to track");
        dialogBuilder.setMessage("Enter Product and URL below");
        dialogBuilder.setPositiveButton("Done", (dialog, whichButton) -> {
            Product e = new Product(productName.getText().toString(), url.getText().toString());
            db.open();
            long id = db.insertProduct(e.getProductName(),e.getInitialPrice(), e.getCurrentPrice(),e.getUrl());
            db.close();
            tracker.products.add(e);
            adapter.notifyDataSetChanged(); //refresh ListView
        });

        dialogBuilder.setNegativeButton("Cancel", (dialog, whichButton) -> {//do nothing
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showDeleteProductDialog(int position){
        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(this);
        //LayoutInflater inflater = this.getLayoutInflater();
        //View dialogView = inflater.inflate(R.layout.delete_dialog, null);
        //dialogBuilder.setView (dialogView);
        dialogBuilder.setTitle("Delete Product Entry");
        dialogBuilder.setMessage("Are you sure you want to delete this entry?");
        dialogBuilder.setPositiveButton("Delete", (DialogInterface dialog, int whichButton) -> {
            db.open();
            if(db.removeProduct(productsList.get(position).getProductName()))
                Toast.makeText(this, "Delete successful.", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Delete failed.", Toast.LENGTH_LONG).show();
            db.close();
            productsList.remove(position);// delete the Product
            adapter.notifyDataSetChanged(); //updates ListView
        });
        dialogBuilder.setNegativeButton("Cancel", (dialog, whichButton) -> {//do nothing
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


    public void showEditProductDialog(int position){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.options_dialog, null);
        dialogBuilder.setView(dialogView);
        Product e = productsList.get(position);

        EditText productName = (EditText) dialogView.findViewById(R.id.productName);
        EditText url = (EditText) dialogView.findViewById(R.id.url);
        productName.setText(e.getProductName());
        url.setText(e.getUrl());
        dialogBuilder.setTitle("Edit Product");
        dialogBuilder.setMessage("Enter Product and URL below");
        dialogBuilder.setPositiveButton("Done", (dialog, whichButton) -> {
            Product p = new Product(productName.getText().toString(),e.getInitialPrice(),e.getCurrentPrice(), url.getText().toString());
            db.open();
            if (db.updateProduct(e.getProductName(), p.getProductName(),p.getInitialPrice(),p.getCurrentPrice(), p.getUrl()))
                Toast.makeText(this, "Update successful.", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Update failed.", Toast.LENGTH_LONG).show();
            db.close();
            productsList.set(position, p);
            tracker.calculatePercentageChange();
            adapter.notifyDataSetChanged(); //refresh ListView
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {//do nothing
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("listViewState", productsList);
    }*/
}