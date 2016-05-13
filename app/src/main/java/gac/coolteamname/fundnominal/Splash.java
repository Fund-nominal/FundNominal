package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

import java.sql.Time;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

/**
 * Created by Jacob on 5/2/2016.
 */
public class Splash extends Activity {
    private final int SPLASH_DISPLAY_LENGHT = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial_activity);

        new InitialViewLoad().execute();
    }

    private class InitialViewLoad extends AsyncTask<Void, Void, Long> {
        @Override
        protected Long doInBackground(Void ... params) {
            long before = System.currentTimeMillis();
            List<Fund> funds = FundPortfolio.get(getApplicationContext()).getFunds();
            new PricesFetcher().fetchItems(funds);
            for (Fund fund : funds) {
                new FinanceFetcher().fetchItems(fund);
                FundPortfolio.get(getApplicationContext()).updateFund(fund);
            }
            long after = System.currentTimeMillis();
            long time = after - before;
            return time;
        }

        @Override
        protected void onPostExecute(Long time) {
            Intent i = new Intent(Splash.this, MainActivity.class);
            synchronized (this) {
                try {
                    if (SPLASH_DISPLAY_LENGHT - time > 0) {
                        wait(SPLASH_DISPLAY_LENGHT - time);
                    }
                } catch (InterruptedException ioe) {
                    Log.e("Blah,Blah,Blah", "Blah", ioe);
                } finally {
                    startActivity(i);
                    finish();
                }
            }
        }
    }
}
