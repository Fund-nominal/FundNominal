package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.locks.Lock;

/**
 * Created by Jacob on 4/12/2016.
 */
public class StockQueryFragment extends DialogFragment {

    private static final String TAG = "TAG";
    public static final String EXTRA_FUND = "com.bignerdranch.android.fundnominal.fund";

    private EditText mEditText;
    private Button mButton1;
    private Button mButton2;
    private Button mButton3;
    private Button mButton4;
    private Button mButton5;
    private List<Fund> mFunds;
    private Fund returnFund;

    private StockQuery stockQuery;
    private long waitTime;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_query, null);

        mButton1 = (Button) v.findViewById(R.id.button_1);
        mButton1.setVisibility(View.INVISIBLE);
        mButton2 = (Button) v.findViewById(R.id.button_2);
        mButton2.setVisibility(View.INVISIBLE);
        mButton3 = (Button) v.findViewById(R.id.button_3);
        mButton3.setVisibility(View.INVISIBLE);
        mButton4 = (Button) v.findViewById(R.id.button_4);
        mButton4.setVisibility(View.INVISIBLE);
        mButton5 = (Button) v.findViewById(R.id.button_5);
        mButton5.setVisibility(View.INVISIBLE);

        final Button[] mButtons = {mButton1, mButton2, mButton3, mButton4, mButton5};

        mEditText = (EditText) v.findViewById(R.id.stock_title);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateMFunds(s.toString());
                updateButtons(mButtons);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.stock_query_title)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (returnFund != null) {
                            sendResult(Activity.RESULT_OK, returnFund);
                        }
                    }
                })
                .create();
    }

    private void updateMFunds(String queryString) {
        new FetchItemsTask().execute(queryString);
        synchronized (this) {
            try {
                //waitTime = stockQuery.getDelayTime();
                //System.out.println(waitTime);
                this.wait(200);
            } catch (InterruptedException ie) {
                Log.e(TAG, "Interupted: " + ie);
            }
        }
    }

    private void updateButtons(final Button[] buttons) {
        if (mFunds != null) {
            for (int i = 0; i < Math.min(mFunds.size(),buttons.length); i++) {
                buttons[i].setText(mFunds.get(i).getTicker() +
                        " : " +
                        mFunds.get(i).getCompanyName());
                buttons[i].setVisibility(View.VISIBLE);
                final int j = i;
                buttons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        returnFund = mFunds.get(j);
                        mEditText.setText(mFunds.get(j).getCompanyName());
                        for (int k = 0; k < Math.min(mFunds.size(), buttons.length); k++) {
                            buttons[k].setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
            for (int j = Math.min(mFunds.size(),buttons.length); j < buttons.length; j++) {
                buttons[j].setVisibility(View.INVISIBLE);
            }
        }
        else {
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    private class FetchItemsTask extends AsyncTask<String, Void, List<Fund>> {
        @Override
        protected List<Fund> doInBackground(String... params) {
            StockQuery stockQuery = new StockQuery();
            List<Fund> fundList = stockQuery.fetchItems(params[0]);
            //waitTime = stockQuery.getDelayTime();
            //synchronized (this) {
                mFunds = fundList;
            //}
            return fundList;
        }

        @Override
        protected void onPostExecute(List<Fund> fundList) {
            //synchronized (this) {
               // mFunds = fundList;
               // this.notify();
            //}
        }
    }


    private void sendResult(int resultCode, Fund fund) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_FUND, fund);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
