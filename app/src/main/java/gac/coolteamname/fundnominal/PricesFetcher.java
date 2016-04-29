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

    public List<Fund> fetchItems(List<Fund> stockPricesList) {
        try {
            stockPricesList = parseItems(stockPricesList);
        }
        catch (IOException ieo) {
            Log.e(TAG, "Failed to fetch item", ieo);
        }
        return stockPricesList;
    }

    private List<Fund> parseItems(List<Fund> funds) throws IOException {
        List<Fund> updatedListFunds = new ArrayList<>();
        for (Fund fund : funds) {
            TimeZone tz = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Calendar today = Calendar.getInstance();
            Date date = today.getTime();

            if (fund.getPrices() == null || fund.getTimeLastChecked() == null) {
                pricesSetter(fund);
            } else {
                if (moreThanTwentyFourHours(fund)) {
                    pricesSetter(fund);
                } else {
                    if (beforeClose(fund.getTimeLastChecked()) && beforeClose(date) &&
                            sameDate(fund.getTimeLastChecked(), date)) {}
                    else if (afterClose(fund.getTimeLastChecked()) && beforeClose(date)) {}
                    else if (afterClose(fund.getTimeLastChecked()) && afterClose(date) &&
                            sameDate(fund.getTimeLastChecked(), date)){}
                    else {
                        pricesSetter(fund);
                    }
                }
            }

            fund.setTimeLastChecked(date);
            updatedListFunds.add(fund);

            TimeZone.setDefault(tz);
        }

        return updatedListFunds;
    }

    private boolean sameDate(Date date1, Date date2) {
        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        if (date1.getDate() == date2.getDate()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean beforeClose(Date date) {
        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        int openTime = 21;
        if (date.getHours() < openTime) {
            return true;
        } else {
            return false;
        }
    }

    private boolean afterClose(Date date) {
        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        int openTime = 21;
        if (date.getHours() >= openTime) {
            return true;
        } else {
            return false;
        }
    }

    private boolean moreThanTwentyFourHours(Fund fund) {
        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Date dateYesterday = yesterday.getTime();

        if (fund.getTimeLastChecked().compareTo(dateYesterday) < 0) {
            return true;
        } else {
            return false;
        }
    }

    private void pricesSetter(Fund fund) throws IOException {
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
    }
}
