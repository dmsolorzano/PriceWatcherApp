package edu.utep.cs.cs4330.mypricewatcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductsAdapter extends ArrayAdapter<Product> {
    private Context iContext;
    private List<Product> productsList;

    ProductsAdapter(Context context, ArrayList<Product> list) {
        super(context, 0, list);
        iContext = context;
        productsList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listProduct = convertView;
        if(listProduct == null)
            listProduct = LayoutInflater.from(iContext).inflate(R.layout.simple_row,parent,false);

        Product currentProduct = productsList.get(position);

        TextView name = (TextView)listProduct.findViewById(R.id.name);
        name.setText(String.format("Product Name: %s",currentProduct.getProductName()));

        TextView price = (TextView) listProduct.findViewById(R.id.initialPrice);
        price.setText(String.format(Locale.getDefault(),"Initial Price: $%.2f",currentProduct.getInitialPrice()));

        TextView currentPrice = (TextView) listProduct.findViewById(R.id.currentPrice);
        currentPrice.setText(String.format(Locale.getDefault(),"Current Price: $%.2f",currentProduct.getCurrentPrice()));

        TextView percentageChange = (TextView) listProduct.findViewById(R.id.percentageDrop);
        percentageChange.setText(String.format(Locale.getDefault(),"Percentage Drop: %d %%", currentProduct.getPercentageChange()));

        return listProduct;
    }
}