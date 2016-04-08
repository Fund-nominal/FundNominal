package gac.coolteamname.fundnominal;

import java.util.UUID;

public class Fund {

    private UUID mId;
    private String mTicker;
    private int mWeight;
    private double[] mPrices;

    public Fund() {
        // Generates a random ID
        this(UUID.randomUUID());
    }

    public Fund(UUID id) {
        mId = id;
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
}
