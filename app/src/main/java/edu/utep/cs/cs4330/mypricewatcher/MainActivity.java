package edu.utep.cs.cs4330.mypricewatcher;


import android.annotation.SuppressLint;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements AsyncResponse{
    PriceFinder tracker = new PriceFinder();
    private Button updatePriceButton;
    ProductsAdapter adapter;
    ListView listView;
    ArrayList<Product> productsList = tracker.products;
    DBAdapter db = new DBAdapter(this);
    DownloadPriceTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db.open();

        // load data from database into ArrayList
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
        // update database
        db.open();
        for(Product element: productsList){
            db.updateProduct(element.getProductName(),
                    element.getProductName(),
                    element.getInitialPrice(),
                    element.getCurrentPrice(),
                    element.getUrl());
        }
        db.close();

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
        ProgressBar progress = dialogView.findViewById(R.id.progressBar);
        EditText url = (EditText) dialogView.findViewById(R.id.url);
        EditText productName = (EditText) dialogView.findViewById(R.id.productName);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Create new product to track");
        dialogBuilder.setMessage("Enter Product and URL below");
        dialogBuilder.setPositiveButton("Done", (dialog, whichButton) -> {
            Double price = 0.0;
            task = new DownloadPriceTask(this);
            task.delegate = this;
            String[] schemes = {"http","https"};
            UrlValidator urlValidator = new UrlValidator(schemes);
            String link = url.getText().toString();
            if (urlValidator.isValid(link)) { //TODO extract to method
                System.out.println("Link is valid");
            } else {
                link = "https://www.google.com";
            }
            try {
                price = task.execute(link).get(); // Async task
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            progress.setVisibility(View.GONE);
            Product e = new Product(productName.getText().toString(),price, price, link);
            db.open();
            long id = db.insertProduct(e.getProductName(),price, price,e.getUrl());
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
            Double price = 0.0;
            task = new DownloadPriceTask(this);
            task.delegate = this;
            String[] schemes = {"http","https"};
            UrlValidator urlValidator = new UrlValidator(schemes);
            String link = url.getText().toString();
            if (urlValidator.isValid(link)) { //TODO extract to method
                System.out.println("Link is valid");
            } else {
                link = "https://www.walmart.com/ip/Bodum-BISTRO-Coffee-Mug-Dishwasher-Safe-35-L-12-Ounce-Transparent/55047939";
            }
            try {
                price = task.execute(link).get(); // Async task
            } catch (InterruptedException f) {
                f.printStackTrace();
            } catch (ExecutionException f) {
                f.printStackTrace();
            }
            Product p = new Product(productName.getText().toString(),price,price,link);
            db.open();
            if (db.updateProduct(e.getProductName(), p.getProductName(),p.getInitialPrice(),p.getCurrentPrice(), p.getUrl()))
                Toast.makeText(this, "Database Update successful.", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Database Update failed.", Toast.LENGTH_LONG).show();
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

    public void processFinish(Double output){
        //Toast.makeText(this, "Background finished successfully", Toast.LENGTH_SHORT).show();
    }

    private static class DownloadPriceTask extends AsyncTask<String, Void, Double> {
        AsyncResponse delegate = null;

        public DownloadPriceTask(Context context) {
        }

        protected Double doInBackground(String... urls) {
            String content = null;
            URLConnection connection = null;
            //TODO check for malformed connection
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
            return Double.parseDouble(price.attr("content"));
        }

        @Override
        protected void onPostExecute(Double result) {
            delegate.processFinish(result);
        }
    }
}