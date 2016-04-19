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
import android.widget.RadioButton;

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
    private RadioButton mUnderWeightButton, mNormalWeightButton, mOverWeightButton;

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

        mUnderWeightButton = (RadioButton) v.findViewById(R.id.one);
        mNormalWeightButton = (RadioButton) v.findViewById(R.id.two);
        mOverWeightButton = (RadioButton) v.findViewById(R.id.three);

        switch (mWeight){
            case -1:
                mUnderWeightButton.setChecked(true);
                break;
            case 0:
                mNormalWeightButton.setChecked(true);
                break;
            case 1:
                mOverWeightButton.setChecked(true);
                break;
            default:
                break;
        }

        mTicker = mFund.getTicker();

        mTickerField.setText(mFund.getTicker());

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Title of dialog")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        weightCheck(mFund);
                        sendResult(Activity.RESULT_OK, mWeight);
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, int weight){
        if (getTargetFragment() == null){
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TICKER, weight);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }

    private void weightCheck(Fund fund) {
        if (mUnderWeightButton.isChecked()){
            mWeight = -1;
        } else if (mNormalWeightButton.isChecked()){
            mWeight = 0;
        } else if (mOverWeightButton.isChecked()){
            mWeight = 1;
        }
        System.out.println(fund.getWeight());
    }

    public Fund getFund() {
        return mFund;
    }

    public void setFund(Fund fund) {
        mFund = fund;
    }
}
