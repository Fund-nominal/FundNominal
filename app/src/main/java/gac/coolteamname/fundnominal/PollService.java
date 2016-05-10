package gac.coolteamname.fundnominal;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Jacob on 5/8/2016.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 7);

            alarmManager.setInexactRepeating(AlarmManager.RTC,
                    calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        Log.i(TAG, "Received an intent: " + intent);

        List<Fund> overs = FundPortfolio.get(getApplicationContext()).getOvers();
        List<Fund> unders = FundPortfolio.get(getApplicationContext()).getUnders();
        List<List<Fund>> oversUnders = new ArrayList<>();
        oversUnders.add(new PricesFetcher().fetchItems(overs));
        oversUnders.add(new PricesFetcher().fetchItems(unders));
        for (Fund fund : oversUnders.get(0)) {
            FundPortfolio.get(getApplicationContext()).updateFund(fund);
        }
        for (Fund fund : oversUnders.get(1)) {
            FundPortfolio.get(getApplicationContext()).updateFund(fund);
        }


        List<String[]> comparisons = Utilities.ExchangeOptions(oversUnders.get(0),
                oversUnders.get(1));

        float baseline = 9;
        List<String> contentText = new ArrayList<>();

        for (String[] strings : comparisons) {
            if (Float.parseFloat(strings[2]) >= baseline) {
                contentText.add(
                        strings[0] + " for " +
                        strings[1] + " with rating: " +
                        strings[2]
                );
            }
        }

        switch (contentText.size()) {
            case 0:

        }
        if (contentText.size() == 0) {
            contentText.add("No great exchanges today.");
        }

        Resources resources = getResources();
        Intent i = MainActivity.newIntent(getApplicationContext());
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(resources.getString(R.string.great_exchanges));

        for (String string : contentText) {
            inboxStyle.addLine(string);
        }

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setTicker(resources.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_coin)
                .setColor(Color.parseColor("#1b5e20"))
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(stringSetter(contentText, resources))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setStyle(inboxStyle)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(0, notification);
    }

    private String stringSetter(List<String> contextString, Resources resources) {
        switch (contextString.size()) {
            case 0:
                return "No great exchanges today.";
            case 1:
                return contextString.get(0);
        }
        return resources.getString(R.string.exchanges);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }
}
