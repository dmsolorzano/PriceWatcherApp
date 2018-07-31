package edu.utep.cs.cs4330.mypricewatcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/** PriceFinder used to track prices of all products*/
public class PriceFinder {
    public ArrayList<Product> products = new ArrayList<>();


    PriceFinder() {
        populateList();
    }

    /** Constructor to add dummy products to the list*/
    private void populateList(){
    }

     /** Method used to update price from webpage
      * currently only randomizes new price */
    public void updatePrice(){
        Random r = new Random();
        double randomValue;
        // go through products list and randomize
        for(Product element: products){
            randomValue = 10.00 + (20.00 - 10.00) * r.nextDouble();// reset random every iteration
            element.setCurrentPrice(randomValue);
        }
        //TODO need to update new prices to database
    }

    /** Method used to calculate the percentage change between the initial price
     * and the current price of all Products*/
    public void calculatePercentageChange(){
        for(Product element: products){
            double decrease = (element.getInitialPrice() - element.getCurrentPrice());
            int newPercent = (int)((decrease / element.getInitialPrice())*100);
            element.setPercentageChange(newPercent);
        }
    }
}
