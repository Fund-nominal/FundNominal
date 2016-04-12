package gac.coolteamname.fundnominal;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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

    /*public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with" +
                        urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer))>0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }*/

    public Fund fetchItems(Fund stockName) {
        /*JSONObject jsonBody = null;
        try {
            String url = "http://query.yahooapis.com/v1/public/yql" +
                    "?q=select%20*%20from%20" +
                    "yahoo.finance.quotes%20where%20symbol%20in%20(%22" +
                    stockName.getTicker() + "%22)" +
                    "&env=store://datatables.org/alltableswithkeys" +
                    "&format=json";
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            jsonBody = new JSONObject(jsonString);
            stockName = parseItems(stockName, jsonBody);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ieo) {
            Log.e(TAG, "Failed to fetch item", ieo);
        }*/
        try {
            stockName = parseItems(stockName);
        }
        catch (IOException ieo) {
            Log.e(TAG, "Failed to fetch item", ieo);
        }
        return stockName;
    }

    private Fund parseItems(Fund fund) throws IOException {
        List<BigDecimal> prices = new ArrayList<>();
        /*JSONObject financeJsonObject = jsonBody.getJSONObject("query");
        JSONObject resultsJsonObject = financeJsonObject.getJSONObject("results");
        JSONObject quoteJsonObject = resultsJsonObject.getJSONObject("quote");*/

        Stock stockA = YahooFinance.get(fund.getTicker());
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -1);
        List<HistoricalQuote> history = stockA.getHistory(from, to, Interval.DAILY);
        for (HistoricalQuote quote : history) {
            prices.add(quote.getClose());
        }

        fund.setPrice(stockA.getQuote().getPrice());
        fund.setPrices(prices);

        return fund;

        /*for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")) {
                continue;
            }

            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }*/
    }
}