package edu.utep.cs.cs4330.mypricewatcher;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
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

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    PriceFinder tracker = new PriceFinder();
    private Button updatePriceButton;
    ProductsAdapter adapter;
    ListView listView;
    ArrayList<Product> productsList = tracker.products;
    DBAdapter db = new DBAdapter(this);
    DownloadPriceTask task;
    NetworkAdapter networkAdapter = new NetworkAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // load data from database into ArrayList
        networkAdapter.checkWifi(this);

        db.open();
        Cursor c = db.getAllProducts();
        if (c.moveToFirst()) { //TODO abstract into method
            do {
                Product x = new Product(c.getString(0),
                        Double.parseDouble(c.getString(1)),
                        Double.parseDouble(c.getString(2)),
                        c.getString(3));
                productsList.add(x);
            } while (c.moveToNext());
        }
        db.close();

        //productsList = tracker.products;
        updatePriceButton = findViewById(R.id.updatePriceButton);
        updatePriceButton.setOnClickListener(this::updatePriceButtonClicked);


        // Create the adapter to convert the array to views
        adapter = new ProductsAdapter(this, productsList);


        // Attach the adapter to a ListView
        listView = findViewById(R.id.mainListView);
        listView.setAdapter(adapter);

        registerForContextMenu(listView);

        // Responds to single click within listView
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Product product = tracker.products.get((int) id); // product that created
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
        EditText url = dialogView.findViewById(R.id.url);
        EditText productName = dialogView.findViewById(R.id.productName);
        Product product = new Product();
        Product trash = new Product();
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Create new product to track");
        dialogBuilder.setMessage("Enter Product and URL below");
        dialogBuilder.setPositiveButton("Done", (dialog, whichButton) -> {
            String[] schemes = {"http","https"};
            UrlValidator urlValidator = new UrlValidator(schemes);
            String link = url.getText().toString();
            if (urlValidator.isValid(link)) { //TODO extract to method
                System.out.println("Link is valid");
            } else {
                link = "https://www.walmart.com/ip/Franklin-Sports-Competition-100-Size-4-Soccer-Ball-Black-White/135643452?";
            }
            product.setUrl(link);
            product.setProductName(productName.getText().toString());
            task = new DownloadPriceTask(this, product, trash);
            task.execute(); // Only execute if network is available
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
        Product product = productsList.get(position);
        Product trash = new Product(product.getProductName(), product.getInitialPrice(), product.getCurrentPrice(), product.getUrl());
        EditText productName = dialogView.findViewById(R.id.productName);
        EditText url = dialogView.findViewById(R.id.url);

        productName.setText(product.getProductName());
        url.setText(product.getUrl());

        dialogBuilder.setTitle("Edit Product");
        dialogBuilder.setMessage("Enter Product and URL below");
        dialogBuilder.setPositiveButton("Done", (dialog, whichButton) -> {
            String[] schemes = {"http","https"};
            UrlValidator urlValidator = new UrlValidator(schemes);
            String link = url.getText().toString();
            if (urlValidator.isValid(link)) { //TODO extract to method
                System.out.println("Link is valid");
            } else { // TODO fix this
                link = "https://www.walmart.com/ip/Bodum-BISTRO-Coffee-Mug-Dishwasher-Safe-35-L-12-Ounce-Transparent/55047939";
            }
            product.setProductName(productName.getText().toString());
            product.setUrl(link);

            task = new DownloadPriceTask(this, product, trash);
            task.execute(); // Async task
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {//do nothing
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    /** Static nested Asynctask used to create connection to webpage
     *  and scrape webpage for price
     *
     *  Updates database and productList in background task*/
    private static class DownloadPriceTask extends AsyncTask<Void, Void, Boolean> {
        private MainActivity activity;
        private Product product;
        private Product trash;

        DownloadPriceTask(Context context, Product product, Product trash) {
            activity = (MainActivity) context;
            this.product = product;
            this.trash = trash;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String content = null;
            URLConnection connection;
            NetworkAdapter conn = new NetworkAdapter();
            Boolean network = false;
            if(conn.checkConnection(activity))
                network = true;
            else
                cancel(true);

            try {
                connection =  new URL(product.getUrl()).openConnection();
                Scanner scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter("\\Z");
                content = scanner.next();
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
            Document document = Jsoup.parse(content); // parses HTML that was retrieved
            Elements price = document.select("span[class=price-characteristic]");// grabs HTML tag
            product.setInitialPrice(Double.parseDouble(price.attr("content")));
            return network;
        }

        @Override
        protected void onPostExecute(Boolean network) {
            if(network)
                Toast.makeText(activity, "Network connection stable", Toast.LENGTH_SHORT).show();

            //TODO if product already exists update instead of add
            boolean edit = false;
            int position = 0;
            for (Product element: activity.productsList) {
                if(element.getProductName().equals(product.getProductName())){
                    position = activity.productsList.indexOf(element);
                    edit = true;
                    break;
                }
            }

            activity.db.open();
            if(edit){
                if(activity.db.updateProduct(trash.getProductName(), product.getProductName(), product.getInitialPrice(),
                        product.getCurrentPrice(), product.getUrl())) {
                    System.out.println("Successful edit");
                }
                activity.tracker.products.set(position,product);
                activity.adapter.notifyDataSetChanged(); //refresh ListView
            }
            else{
                activity.db.insertProduct(product.getProductName(), product.getInitialPrice(),
                        product.getCurrentPrice(), product.getUrl());
                activity.tracker.products.add(product);
                activity.adapter.notifyDataSetChanged(); //refresh ListView
            }
            activity.db.close();
            Toast.makeText(activity, "Successfully updated", Toast.LENGTH_SHORT).show();
        }
    }
}