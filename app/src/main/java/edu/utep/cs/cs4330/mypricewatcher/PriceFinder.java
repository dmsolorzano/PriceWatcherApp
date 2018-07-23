package edu.utep.cs.cs4330.mypricewatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** PriceFinder used to track prices of all products*/
public class PriceFinder {
    public ArrayList<Product> products = new ArrayList<>();
    private int percentageChange;

    public PriceFinder() {
        // populate product list
        populateList();
    }

    /** Constructor to add dummy products to the list*/
    public void populateList(){
        Product product2 = new Product("Football 2");
        products.add(product2);
        Product product3 = new Product("Football 3");
        products.add(product3);
        Product product4 = new Product("Football 4");
        products.add(product4);
        Product product5 = new Product("Football 5");
        products.add(product5);

    }

     /** Method used to update price from webpage
      * currently only randomizes new price */
    public void updatePrice(){
        Random r = new Random();
        double randomValue = 10.00 + (20.00 - 10.00) * r.nextDouble();
        // go through products list and randomize
        for(Product element: products){
            randomValue = 10.00 + (20.00 - 10.00) * r.nextDouble();// reset random every iteration
            element.setCurrentPrice(randomValue);
        }
    }

    /** Method used to calculate the percentage change between the initial price
     * and the current price*/
    public void calculatePercentageChange(){
        //double decrease = (product1.getInitialPrice() - product1.getCurrentPrice());
        //percentageChange = (int)((decrease / product1.getInitialPrice())*100);
        for(Product element: products){
            double decrease = (element.getInitialPrice() - element.getCurrentPrice());
            int newPercent = (int)((decrease / element.getInitialPrice())*100);
            element.setPercentageChange(newPercent);
        }
    }

    public int getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(int percentageChange){
        this.percentageChange = percentageChange;
    }

}
