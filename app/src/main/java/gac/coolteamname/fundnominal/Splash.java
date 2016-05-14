package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import java.sql.Time;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jacob on 5/2/2016.
 */
public class Splash extends Activity {
    private final int SPLASH_DISPLAY_LENGHT = 5000;
    private Handler handler;
    private Timer timer;

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
            final Intent i = new Intent(Splash.this, MainActivity.class);
            handler = new Handler();

            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(i);
                            finish();
                        }
                    });
                }
            };
            if (SPLASH_DISPLAY_LENGHT - time > 0)
                timer.schedule(timerTask, SPLASH_DISPLAY_LENGHT - time);
            else
                timer.schedule(timerTask, 5000);
        }
    }
}
