package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.UUID;

/**
 * Created by vongr on 4/17/2016.
 * A dialog for confirmation when deleting funds
 */
public class DeleteFragment extends DialogFragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_FUND = "fund";
    public static final String FUND_DELETION = "gac.coolteamname.fundnominal.fund";

    /**
     * Create a new instance of the dialog
     * @param fund the fund to delete
     */
    public static DeleteFragment newInstance(Fund fund) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_NAME, fund.getTicker());
        args.putSerializable(ARG_FUND, fund);

        DeleteFragment fragment = new DeleteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // init the message
        String name = (String) getArguments().getSerializable(ARG_NAME);
        String deleteConfirmation = getResources().getString(R.string.delete_confirmation) + name;

        // create the dialog
        return new AlertDialog.Builder(getActivity())
                .setTitle(deleteConfirmation)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendResult(Activity.RESULT_CANCELED);
                            }
                        })
                .setPositiveButton("Delete Fund",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendResult(Activity.RESULT_OK);
                            }
                        })
                .create();
    }

    /**
     * Send result back to previous activity
     */
    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        Fund fund = (Fund) getArguments().getSerializable(ARG_FUND);
        intent.putExtra(FUND_DELETION, fund);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
