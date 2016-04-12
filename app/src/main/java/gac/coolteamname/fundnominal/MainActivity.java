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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new FetchItemsTask().execute(apple);

        mTextView = (TextView) findViewById(R.id.initial_text_view);


        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText(apple.getPrices().toString());
            }
        });
    }

    private class FetchItemsTask extends AsyncTask<Fund, Void, Fund> {
        @Override
        protected Fund doInBackground(Fund... params) {
            return new FinanceFetcher().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(Fund stock) {
            apple = stock;
        }
    }
}
