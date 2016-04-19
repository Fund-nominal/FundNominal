package gac.coolteamname.fundnominal;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Fund implements Serializable {
/**
 * Stores information of a Fund
 *
 * Id: UUID for the fund
 * Weight: -1 = Underweight, 0 = Normal, 1 = Overweight
 * Prices: Array of doubles to store the prices of the fund
 * PortfolioName: a string, the name of the Portfolio the fund belongs to
 */

    private UUID mId;
    private String mTicker;
    private String mCompanyName;
    private int mWeight;
    private double[] mPrices;
    private double mPrice;
    private String mPortfolioName;

    /**
     * Create an empty Fund
     */
    public Fund() {
        this(UUID.randomUUID(),"",0, null);
    }

    /**
     * Create a Fund with a predetermined Ticker
     * @param ticker The ticker name
     */
    public Fund(String ticker) {
        // Generates a random ID
        this(UUID.randomUUID(), ticker, 0, null);
    }

    public Fund(UUID id, String ticker, int weight, String portfolioName) {
        mId = id;
        mTicker = ticker;
        mWeight = weight;
        mPortfolioName = portfolioName;
    }

    /**
     * Get the weight, but convert it to a string of values either
     * "Overweight", "Underweight" or "Normal"
     * @return The result string
     */
    public String getWeightText(){
        if (mWeight == -1) {
            return "Underweight";
        }
        else if (mWeight == 1) {
            return "Overweight";
        }
        else {
            return "Normal";
        }
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

    public double[] getPrices() {  // can we just call the fetcher in here and cast the result as a double
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

    public Double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public void setCompanyName(String companyName) {
        mCompanyName = companyName;
    }

    public String getPortfolioName() {
        return mPortfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        mPortfolioName = portfolioName;
    }
}
