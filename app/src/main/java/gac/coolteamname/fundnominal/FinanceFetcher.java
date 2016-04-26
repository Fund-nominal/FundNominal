package gac.coolteamname.fundnominal;

import android.util.Log;
import java.io.IOException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by Jacob on 3/29/2016.
 * TODO: Please describe what this class does.
 */
public class FinanceFetcher {

    private static final String TAG = "FinanceFetcher";

    public Fund fetchItems(Fund stockName) {
        try {
            stockName = parseItems(stockName);
        }
        catch (IOException ieo) {
            Log.e(TAG, "Failed to fetch item", ieo);
        }
        return stockName;
    }

    private Fund parseItems(Fund fund) throws IOException {

        Stock stockA = YahooFinance.get(fund.getTicker());
        fund.setPrice(stockA.getQuote().getPreviousClose());

        return fund;
    }
}