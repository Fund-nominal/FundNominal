package gac.coolteamname.fundnominal;

import java.math.BigDecimal;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class Fund {

    private UUID mId;
    private String mTicker;
    private String mCompanyName;
    private int mWeight;
    private List<BigDecimal> mPrices;
    private BigDecimal mPrice;

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

    public void setPrices(List<BigDecimal> price){
        mPrices = price;
    }

    public List<BigDecimal> getPrices() {
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

    public BigDecimal getPrice() {
        return mPrice;
    }

    public void setPrice(BigDecimal price) {
        mPrice = price;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public void setCompanyName(String companyName) {
        mCompanyName = companyName;
    }
}
