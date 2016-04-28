package gac.coolteamname.fundnominal;

import android.util.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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

    /* Work in Progress
    private List<Fund> parseItems(List<Fund> funds) throws IOException {
        List<Fund> updatedListFunds = new ArrayList<>();
        for (Fund fund : funds) {
            TimeZone tz = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            Calendar calendar2 = Calendar.getInstance();
            calendar2.add(Calendar.DAY_OF_MONTH, -1);
            Date date2 = calendar2.getTime();
            if ((fund.getTimeLastChecked() == null) || (fund.getPrices() == null)) {
                System.out.println("What's UP MY HOMMIE!");
                updatePrices(fund);
                fund.setTimeLastChecked(date);
                updatedListFunds.add(fund);
            } else if (fund.getTimeLastChecked().compareTo(date2) == 1) {
                if ((fund.getTimeLastChecked().getHours() > 15) &&
                    (fund.getTimeLastChecked().getHours() < 21)) {
                    if (fund.getTimeLastChecked().getHours() < date.getHours()) {
                        if (date.getHours() < 21) {
                            fund.setTimeLastChecked(date);
                            updatedListFunds.add(fund);
                        }
                        else {
                            updatePrices(fund);
                            fund.setTimeLastChecked(date);
                            updatedListFunds.add(fund);
                        }
                    } else {
                        updatePrices(fund);
                        fund.setTimeLastChecked(date);
                        updatedListFunds.add(fund);
                    }
                } else {
                    if (date.getHours() > 21) {
                        updatePrices(fund);
                        fund.setTimeLastChecked(date);
                        updatedListFunds.add(fund);
                    } else {
                        fund.setTimeLastChecked(date);
                        updatedListFunds.add(fund);
                    }
                }
            } else {
                updatePrices(fund);
                fund.setTimeLastChecked(date);
                updatedListFunds.add(fund);
            }
            TimeZone.setDefault(tz);
        }

        return updatedListFunds;
    }

    private void updatePrices(Fund fund) throws IOException {
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
    }*/
}
