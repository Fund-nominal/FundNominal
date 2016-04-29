package gac.coolteamname.fundnominal;

import android.util.Log;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Calendar today = Calendar.getInstance();
        Date date = today.getTime();

        if (fund.getPrice() == null || fund.getTimePriceChecked() == null) {
            priceSetter(fund);
        } else {
            if (moreThanTwentyFourHours(fund)) {
                priceSetter(fund);
            } else {
                if (beforeClose(fund.getTimePriceChecked()) && beforeClose(date) &&
                        sameDate(fund.getTimePriceChecked(), date)) {}
                else if (afterClose(fund.getTimePriceChecked()) && beforeClose(date)) {}
                else if (afterClose(fund.getTimePriceChecked()) && afterClose(date) &&
                        sameDate(fund.getTimePriceChecked(), date)){}
                else {
                    priceSetter(fund);
                }
            }
        }

        fund.setTimePriceChecked(date);
        TimeZone.setDefault(tz);

        return fund;
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

        if (fund.getTimePriceChecked().compareTo(dateYesterday) < 0) {
            return true;
        } else {
            return false;
        }
    }

    private void priceSetter(Fund fund) throws IOException {
        Stock stockA = YahooFinance.get(fund.getTicker());
        fund.setPrice(stockA.getQuote().getPreviousClose());
    }
}