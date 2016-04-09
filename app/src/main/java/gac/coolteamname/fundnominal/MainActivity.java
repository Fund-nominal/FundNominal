package gac.coolteamname.fundnominal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    Stock stock;
    private static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.initial_text_view);

        try {
            Calendar from = Calendar.getInstance();
            Calendar to = Calendar.getInstance();
            from.add(Calendar.YEAR, 1);
            stock = YahooFinance.get("GOOG", from, to, Interval.DAILY);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to catch exception", ioe);
        }

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar from = Calendar.getInstance();
                Calendar to = Calendar.getInstance();
                from.add(Calendar.YEAR, 1);
                try {
                    mTextView.setText(stock.getHistory(from, to, Interval.DAILY).toString());
                } catch (IOException ioe) {
                    Log.e(TAG, "Failed to catch exception", ioe);
                }
            }
        });
    }
}
