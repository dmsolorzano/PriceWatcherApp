package edu.utep.cs.cs4330.mypricewatcher;

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
        Product product2 = new Product();
        products.add(product2);
        Product product3 = new Product();
        products.add(product3);
        Product product4 = new Product();
        products.add(product4);
        Product product5 = new Product();
        products.add(product5);

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
    }

    /** Method used to calculate the percentage change between the initial price
     * and the current price*/
    public void calculatePercentageChange(){
        for(Product element: products){
            double decrease = (element.getInitialPrice() - element.getCurrentPrice());
            int newPercent = (int)((decrease / element.getInitialPrice())*100);
            element.setPercentageChange(newPercent);
        }
    }
}
