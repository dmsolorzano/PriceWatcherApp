package edu.utep.cs.cs4330.mypricewatcher;


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
import android.widget.Toast;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
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
            /* TODO Create new product based on inputs from dialog
            *        and then pass product to the asynctask to perform task
            *        and then grab that information and pass it to the posExecute method
            *        to update database and add product to list.*/
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
            task = new DownloadPriceTask(this);

            ArrayList<Product> products = new ArrayList<>();// fix here
            products.add(product);
            products.add(trash);
            task.execute(products); // Asynctask running here
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
        EditText productName = (EditText) dialogView.findViewById(R.id.productName);
        EditText url = (EditText) dialogView.findViewById(R.id.url);

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
            product.setUrl(product.getUrl());
            task = new DownloadPriceTask(this);
            ArrayList<Product> products = new ArrayList<>();
            products.add(product);// and here
            products.add(trash);
            task.execute(products); // Async task
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
     *  Updates database and productList in background*/
    private static class DownloadPriceTask extends AsyncTask<ArrayList<Product>, Void, ArrayList<Product>> {
        private MainActivity activity;

        DownloadPriceTask(Context context) {//TODO fix nasty code by giving Products an id
            activity = (MainActivity) context;
        }
        protected ArrayList<Product> doInBackground(ArrayList<Product>... products) {
            String content = null;
            URLConnection connection;
            Product product = new Product(products[0].get(0).getProductName(),products[0].get(0).getInitialPrice(),
                    products[0].get(0).getCurrentPrice(), products[0].get(0).getUrl()); // product passed from dialog
            //TODO check for malformed connection
            try {
                connection =  new URL(product.getUrl()).openConnection();
                Scanner scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter("\\Z");
                content = scanner.next();
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
            Document document = Jsoup.parse(content); // connects and parses HTML of url
            Elements price = document.select("span[class=price-characteristic]");// grabs HTML tag
            product.setInitialPrice(Double.parseDouble(price.attr("content")));
            product.setUrl(products[0].get(0).getUrl());
            ArrayList<Product> products1 = new ArrayList<>();
            products1.add(product);
            products1.add(products[0].get(1));
            return products1;
        }

        @Override
        protected void onPostExecute(ArrayList<Product> products) {
            Product product = products.get(0); // replace
            Product p = products.get(1);
            activity.db.open();
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

            if(edit){
                if(activity.db.updateProduct(p.getProductName(), product.getProductName(), product.getInitialPrice(),
                        product.getCurrentPrice(), product.getUrl())) {
                    Toast.makeText(activity, "Edit success", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(activity, "Database updated", Toast.LENGTH_SHORT).show();
        }
    }
}