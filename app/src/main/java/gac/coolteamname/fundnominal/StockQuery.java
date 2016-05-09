package gac.coolteamname.fundnominal;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by Jacob on 4/12/2016.
 * TODO: Please describe what this class does. If possible, what each function does also
 */
public class StockQuery {

    private static final String TAG = "StringQuery";
    private long delayTime;

    public byte[] getUrlBytes(String urlSpec) throws IOException {
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

    public long getDelayTime() {
        return delayTime;
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<Fund> fetchItems(String stockQuery) {
        List<Fund> mFunds = new ArrayList<>();
        try {
            String url = "http://d.yimg.com/aq/autoc?" +
                    "query=" +
                    format(stockQuery) +
                    "&region=US&lang=en-US";
            final long timeBefore = System.currentTimeMillis();
            String jsonString = getUrlString(url);
            final long timeAfter = System.currentTimeMillis();
            delayTime = timeAfter - timeBefore;
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            mFunds = parseItems(jsonBody);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ieo) {
            Log.e(TAG, "Failed to fetch item", ieo);
        }
        return mFunds;
    }

    private List<Fund> parseItems(JSONObject jsonBody) throws IOException, JSONException {
        List<Fund> funds = new ArrayList<>();
        JSONObject resultSetJsonObject = jsonBody.getJSONObject("ResultSet");
        JSONArray resultJsonArray = resultSetJsonObject.getJSONArray("Result");

        for (int i = 0; i < resultJsonArray.length(); i++) {
            JSONObject financeJsonObject = resultJsonArray.getJSONObject(i);
            Fund fund = new Fund(financeJsonObject.getString("symbol"));
            fund.setCompanyName(financeJsonObject.getString("name"));
            funds.add(fund);
        }

        return funds;
    }

    private String format(String string) {
        String formattedString = "";
        String[] sArray = string.split(" ");
        for (int i = 0; i < sArray.length; i++) {
            if (i == (sArray.length - 1)) {
                formattedString = formattedString + sArray[i];
            } else {
                formattedString = formattedString + sArray[i] + "%20";
            }
        }

        return formattedString;
    }
}
