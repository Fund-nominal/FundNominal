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
 * Created by Jacob on 4/20/2016.
 */
public class PricesFetcher {

    private static final String TAG = "PricesFetcher";

    public List<Fund> fetchItems(List<Fund> stockName) {
        try {
            stockName = parseItems(stockName);
        }
        catch (IOException ieo) {
            Log.e(TAG, "Failed to fetch item", ieo);
        }
        return stockName;
    }

    private List<Fund> parseItems(List<Fund> funds) throws IOException {
        List<Fund> updatedListFunds = new ArrayList<>();
        for (Fund fund : funds) {
            List<BigDecimal> prices = new ArrayList<>();

            Stock stockA = YahooFinance.get(fund.getTicker());
            Calendar from = Calendar.getInstance();
            Calendar to = Calendar.getInstance();
            from.add(Calendar.YEAR, -1);
            List<HistoricalQuote> history = stockA.getHistory(from, to, Interval.DAILY);

            for (HistoricalQuote quote : history) {
                prices.add(quote.getClose());
            }

            fund.setPrices(prices);
            updatedListFunds.add(fund);
        }

        return updatedListFunds;
    }
}
