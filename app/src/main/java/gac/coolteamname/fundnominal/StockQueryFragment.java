package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.locks.Lock;

/**
 * Created by Jacob on 4/12/2016.
 * StockQueryFragment is a DialogFragment for adding new fund, with incorporated
 * suggestion feature.
 */
public class StockQueryFragment extends DialogFragment {

    private static final String TAG = "TAG";
    public static final String EXTRA_FUND = "gac.coolteamname.fundnominal.fund";

    private AlertDialog mDialog;
    private EditText mEditText;
    private RecyclerView mQueryRecyclerView;
    private QueryAdapter mQueryAdapter;
    private RadioButton mRadioButton1;
    private RadioButton mRadioButton2;
    private RadioButton mRadioButton3;
    private List<Fund> mFunds;
    private Fund returnFund;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_query, null);

        mQueryRecyclerView = (RecyclerView) v.findViewById(R.id.query_recycler_view);
        mQueryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // initiate the ticker input field
        mEditText = (EditText) v.findViewById(R.id.stock_title);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // When text changed: update the list of suggestions, and the buttons.
                updateMFunds(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // intentionally left blank
            }
        });

        mRadioButton1 = (RadioButton) v.findViewById(R.id.overweight);
        mRadioButton2 = (RadioButton) v.findViewById(R.id.normal);
        mRadioButton3 = (RadioButton) v.findViewById(R.id.underweight);

        // create the dialog
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.StyledDialog)
                .setView(v)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Add fund", new DialogInterface.OnClickListener() {
                    /**
                     * When click on OK button: set the weight of the fund, and send the fund back
                     * to previous activity.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (returnFund != null) {
                            weightCheck(returnFund);
                            sendResult(Activity.RESULT_OK, returnFund);
                        }
                    }
                })
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                //((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor();
                //could probably do same thing for negative button here... just need to know what text to set to
                mDialog = (AlertDialog) dialog;
            }
        });
        return dialog;
    }

    /**
     * Changes state of the Fund according to the radio buttons
     * @param fund the fund to change
     */
    private void weightCheck(Fund fund) {
        if (mRadioButton1.isChecked()) {
            fund.setWeight(1);
        } else if (mRadioButton2.isChecked()) {
            fund.setWeight(0);
        } else {
            fund.setWeight(-1);
        }
    }

    /**
     * Update the list of stock suggestion, and store it in mFunds
     * @param queryString the query string
     */
    private void updateMFunds(String queryString) {
        new FetchItemsTask().execute(queryString);
    }

    /**
     * send the result back to previous activity
     * @param resultCode result code
     * @param fund the fund to send
     */
    private void sendResult(int resultCode, Fund fund) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_FUND, fund);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

    private class QueryHolder extends RecyclerView.ViewHolder {

        private TextView mQueryTextView;
        private Fund mFund;

        public QueryHolder(View itemView) {
            super(itemView);

            mQueryTextView = (TextView) itemView.findViewById(R.id.query_text_view);
            mQueryTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFund != null) {
                        returnFund = mFund;
                        mEditText.setText(mFund.getCompanyName());
                    }
                    if (v != null){
                        InputMethodManager imm = (InputMethodManager) getContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                    }
                    if (mDialog != null) {
                        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            });
        }

        public void bindQuery(Fund query){
            mFund = query;
            if (query.getTicker() != null) {
                mQueryTextView.setText(query.getTicker() + " : " + query.getCompanyName());
            }
        }
    }

    private class QueryAdapter extends RecyclerView.Adapter<QueryHolder> {

        private List<Fund> mQuery;

        /**
         * Constructor: takes in a list of strings to display. The list returned by ExchangeOptions in Utilities.
         * @param
         */
        public QueryAdapter(List<Fund> queries) {
            mQuery = queries;
        }

        @Override
        public QueryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater
                    .inflate(R.layout.list_item_query, parent, false);
            return new QueryHolder(view);
        }

        @Override
        public void onBindViewHolder(QueryHolder holder, int position) {
            Fund query = mQuery.get(position);
            holder.bindQuery(query);
        }

        @Override
        public int getItemCount() {
            return mQuery.size();
        }

        public void setQueries(List<Fund> queries) {
        mQuery = queries;
        }
    }

    /**
     * Handles the fetching of the data from internet.
     * Takes in a string as query (the name of the stock)
     * Returns a list of Funds
     */
    private class FetchItemsTask extends AsyncTask<String, Void, List<Fund>> {
        @Override
        protected List<Fund> doInBackground(String... params) {
            StockQuery stockQuery = new StockQuery();
            List<Fund> fundList = stockQuery.fetchItems(params[0]);
            return fundList;
        }

        @Override
        protected void onPostExecute(List<Fund> fundList) {
            mFunds = fundList;
            // Update the RecyclerView
            if (mQueryAdapter == null) {
                mQueryAdapter = new QueryAdapter(mFunds);
                mQueryRecyclerView.setAdapter(mQueryAdapter);
            } else {
                mQueryAdapter.setQueries(mFunds);
                mQueryAdapter.notifyDataSetChanged();
            }
        }
    }
}
