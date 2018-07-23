package edu.utep.cs.cs4330.mypricewatcher;

/** Product class which will represent an object of a product being tracked*/
public class Product {
    private String productName;
    private double initialPrice;
    private double currentPrice;
    private String url;

    public Product() {
        productName = "Generic Football";
        initialPrice = 20.00; // Fixed price for the time being
        currentPrice = 0.00;
        url = "testURL";
    }

    public Product(String productName) {
        this.productName = productName;
        initialPrice = 20.00; // Fixed price for the time being
        currentPrice = 0.00;
        url = "testURL";
    }

    public Product(String name, double iPrice, double cPrice, String url){
        this.productName = name;
        this.initialPrice = iPrice;
        this.currentPrice = cPrice;
        this.url = url;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(double initialPrice) {
        this.initialPrice = initialPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}