package gac.coolteamname.fundnominal;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private Fund apple = new Fund("AAPL");
    private String appleQuery = "Apple";
    private List<Fund> mFundsList;

    private String queryString;

    public String stringBuilder(List<Fund> fundList) {
        String builder = "";
        for (Fund fund : fundList) {
            builder = builder + fund.getCompanyName() + " : " + fund.getTicker() + "\n";
        }
        return builder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new FetchItemsTask().execute(appleQuery);

        mTextView = (TextView) findViewById(R.id.initial_text_view);

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText(queryString);
            }
        });
    }

    private class FetchItemsTask extends AsyncTask<String, Void, List<Fund>> {
        @Override
        protected List<Fund> doInBackground(String... params) {
            return new StockQuery().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(List<Fund> fundList) {
            mFundsList = fundList;
            queryString = stringBuilder(mFundsList);
        }
    }

    /*private class FetchItemsTask extends AsyncTask<Fund, Void, Fund> {
        @Override
        protected Fund doInBackground(Fund... params) {
            return new FinanceFetcher().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(Fund stock) {
            apple = stock;
        }
    }*/
}
