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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jacob on 3/29/2016.
 */
public class FinanceFetcher {

    private static final String TAG = "FlickrFetchr";

    private static final String API_KEY = "4e2d04fc1c9e94e2fe604c77c23ab86e";

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

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public String fetchItems(String stockName) {
        JSONObject jsonBody = null;
        try {
            String url = "http://query.yahooapis.com/v1/public/yql" +
                    "?q=select%20*%20from%20" +
                    "yahoo.finance.quotes%20where%20symbol%20in%20(%22" +
                    stockName + "%22)" +
                    "&env=store://datatables.org/alltableswithkeys" +
                    "&format=json";
            /*String url = Uri.parse("https://api.flickr.com/services/rest")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .appendQueryParameter("page", Integer.toString(page))
                    .build().toString();*/
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            jsonBody = new JSONObject(jsonString);
            //parseItems(jsonBody);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ieo) {
            Log.e(TAG, "Failed to fetch item", ieo);
        }
        return jsonBody.toString();
    }

    private void parseItems(JSONObject jsonBody) throws IOException, JSONException {
        JSONObject financeJsonObject = jsonBody.getJSONObject("query");
        JSONObject resultsJsonObject = financeJsonObject.getJSONObject("results");
        JSONObject quoteJsonObject = resultsJsonObject.getJSONObject("quote");

        Fund apple = new Fund("APPL");

        apple.setPrice(Double.parseDouble(quoteJsonObject.getString("PreviousClose")));

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