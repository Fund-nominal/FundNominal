package gac.coolteamname.fundnominal;

import android.util.Log;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

/**
 * Created by Jacob on 3/29/2016.
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
        //List<BigDecimal> prices = new ArrayList<>();

        Stock stockA = YahooFinance.get(fund.getTicker());
        /*Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -1);
        List<HistoricalQuote> history = stockA.getHistory(from, to, Interval.DAILY);

        for (HistoricalQuote quote : history) {
            prices.add(quote.getClose());
        }

        fund.setPrices(prices);*/
        //fund.setPrice(stockA.getQuote().getPrice());

        return fund;
    }
}