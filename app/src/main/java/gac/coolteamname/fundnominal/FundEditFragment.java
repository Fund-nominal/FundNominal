package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * FundEditFragment is a Dialog that let user edit the details of the fund.
 * This include ticker name and the state of overweight/underweight/normal of the fund.
 */
public class FundEditFragment extends DialogFragment {
    public static final String EXTRA_FUND = "gac.coolteamname.fundnominal.fund";
    private static final String ARG_NAME = "ticker";

    private Fund mFund;
    private TextView mNameField;
    private RadioButton mUnderWeightButton, mNormalWeightButton, mOverWeightButton;

    public static FundEditFragment newInstance(Fund fund){
        Bundle args = new Bundle();
        args.putString(ARG_NAME, fund.getTicker());

        FundEditFragment fragment = new FundEditFragment();
        fragment.setArguments(args);
        fragment.setFund(fund);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fund,null);

        mNameField = (TextView) v.findViewById(R.id.name_field);

        mUnderWeightButton = (RadioButton) v.findViewById(R.id.underweight_button);
        mNormalWeightButton = (RadioButton) v.findViewById(R.id.normalweight_button);
        mOverWeightButton = (RadioButton) v.findViewById(R.id.overweight_button);

        /**
         * When clicking on on a Fund to edit
         * Checks RadioButton to the Fund's current weight
         */
        switch (mFund.getWeight()){
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

        mNameField.setText(mFund.getTicker());

        android.support.v7.app.AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Edit Fund")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        weightCheck(mFund);
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((android.support.v7.app.AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).
                        setTextColor(getResources().getColor(R.color.PrimaryColor));
            }
        });

        return dialog;
    }

    private void sendResult(int resultCode){
        if (getTargetFragment() == null){
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FUND, mFund);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }

    private void weightCheck(Fund fund) {
        if (mUnderWeightButton.isChecked()){
            fund.setWeight(-1);
        } else if (mNormalWeightButton.isChecked()){
            fund.setWeight(0);
        } else if (mOverWeightButton.isChecked()){
            fund.setWeight(1);
        }
    }

    public Fund getFund() {
        return mFund;
    }

    public void setFund(Fund fund) {
        mFund = fund;
    }
}
