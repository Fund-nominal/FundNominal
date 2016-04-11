package gac.coolteamname.fundnominal;

import java.util.UUID;

public class Fund {

    private UUID mId;
    private String mTicker;
    private int mWeight;
    private double[] mPrices;
    private double mPrice;

    public Fund(String ticker) {
        // Generates a random ID
        this(UUID.randomUUID(), ticker);
    }

    public Fund(UUID id, String ticker) {
        mId = id;
        mTicker = ticker;
    }

    public void setTicker(String ticker) {
        mTicker = ticker;
    }

    public String getTicker() {
        return mTicker;
    }

    public void setPrices(double[] price){
        mPrices = price;
    }

    public double[] getPrices() {
        return mPrices;
    }

    public UUID getId() {
        return mId;
    }

    public void setWeight(int weight) {
        mWeight = weight;
    }

    public int getWeight() {
        return mWeight;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }
}
