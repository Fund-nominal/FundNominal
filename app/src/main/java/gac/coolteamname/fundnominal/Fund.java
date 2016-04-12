package gac.coolteamname.fundnominal;

import java.util.UUID;

public class Fund {

    private UUID mId;
    private String mTicker;
    private int mWeight;
    private double[] mPrices;

    public Fund() {this(UUID.randomUUID()); }

    public Fund(UUID id) {
        mId = id;   // is this ok?  Ryan and Joel changed this a bit
    }


    public Fund(String ticker) {
        // Generates a random ID
        this(UUID.randomUUID(), ticker, 0);
    }

    public Fund(UUID id, String ticker, int weight) {
        mId = id;
        mTicker = ticker;
        mWeight = weight;
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
