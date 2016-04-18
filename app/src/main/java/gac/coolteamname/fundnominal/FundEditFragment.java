package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * FundEditFragment is a Dialog that let user edit the details of the fund.
 * This include ticker name and the state of overweight/underweight/normal of the fund.
 */
public class FundEditFragment extends DialogFragment {
    public static final String EXTRA_TICKER = "gac.coolteamname.fundnominal.ticker";
    private static final String EXTRA_FUND = "gac.coolteamname.fundnominal.fund";
    private static final String ARG_TICKER = "ticker";
    private static final String ARG_WEIGHT = "weight";

    private Fund mFund;
    private String mTicker;
    private int mWeight;
    private EditText mTickerField;

    public static FundEditFragment newInstance(Fund fund){
        Bundle args = new Bundle();
        args.putString(ARG_TICKER, fund.getTicker());
        args.putInt(ARG_TICKER, fund.getWeight());
        FundEditFragment fragment = new FundEditFragment();
        fragment.setArguments(args);
        fragment.setFund(fund);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mTicker = getArguments().getString(ARG_TICKER);
        mWeight = getArguments().getInt(ARG_WEIGHT);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fund,null);
        mTickerField = (EditText) v.findViewById(R.id.ticker_field);
        mTicker = mFund.getTicker();
        mTickerField.setText(mFund.getTicker());
        mTickerField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTicker = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Title of dialog")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK, mTicker);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_CANCELED, mTicker);
                    }
                })
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendDeletion(2, mFund.getId().toString());
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, String ticker){
        if (getTargetFragment() == null){
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TICKER, ticker);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }

    private void sendDeletion(int resultCode, String fund){
        if(getTargetFragment() == null)
            return;
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FUND, fund);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

    public Fund getFund() {
        return mFund;
    }

    public void setFund(Fund fund) {
        mFund = fund;
    }
}
