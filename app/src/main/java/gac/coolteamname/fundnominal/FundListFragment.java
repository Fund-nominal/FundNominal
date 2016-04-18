package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class FundListFragment extends Fragment {

    private static final int REQUEST_ADD_FUND = 0;
    private static final int REQUEST_EDIT_FUND = 1;

    private static final String ADD_FUND_TAG = "AddFund";
    private static final String EDIT_FUND_TAG = "EditFund";
    private static final String DELETE_FUND_TAG = "DeleteFund";
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mFundRecyclerView;
    private FundAdapter mAdapter;
    private RelativeLayout mFundEmptyView;
    private Button mNewfundButton;
    private boolean mSubtitleVisible;
    private Fund mActiveFund;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fund_list, container, false);

        mFundRecyclerView = (RecyclerView) view
                .findViewById(R.id.fund_recycler_view);
        mFundRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mFundEmptyView = (RelativeLayout) view.findViewById(R.id.empty_fund_list_display);
        mNewfundButton = (Button) view.findViewById(R.id.new_fund_button);
        mNewfundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewFund();
            }
        });

        // Save subtitle text on screen rotation
        if (savedInstanceState != null)
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);

        updateUI();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_fund_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible)
            subtitleItem.setTitle(R.string.hide_subtitle);
        else
            subtitleItem.setTitle(R.string.show_subtitle);
    }

    // When pressing buttons on the Options Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_new_fund:
                createNewFund();
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        // Integer with number of Funds in the portfolio
        int fundCount = FundPortfolio.get(getActivity()).getFunds().size();
        String subtitle = getResources() // Displays the number of Funds in the portfolio
                .getQuantityString(R.plurals.subtitle_plural, fundCount, fundCount);

        if (!mSubtitleVisible)
            subtitle = null;

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    /**
     * Repopulate RecyclerView and update info for each Fund.
     * If there is no Fund, display a message and a button to add Fund.
     */
    private void updateUI() {
        List<Fund> funds = FundPortfolio.get(getActivity()).getFunds();

        // Update the RecyclerView
        if (mAdapter == null) {
            mAdapter= new FundAdapter(funds);
            mFundRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setFunds(funds);
            mAdapter.notifyDataSetChanged();
        }

        if (funds.isEmpty()) {
            // If there is no fund, hide RecyclerView, display message
            mFundRecyclerView.setVisibility(View.GONE);
            mFundEmptyView.setVisibility(View.VISIBLE);
        }
        else {
            // If there are fund(s), hide message, display RecyclerView
            mFundRecyclerView.setVisibility(View.VISIBLE);
            mFundEmptyView.setVisibility(View.GONE);
        }
        updateSubtitle();
    }

    /**
     * For use when returning from another activity.
     * For example: Returning from a dialog and need to fetch data from that dialog
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // resultCode: 0 = Cancel
        // resultCode: 2 = Delete
        // resultCode: 1 = Confirm
        if (requestCode == REQUEST_ADD_FUND){
            if (resultCode == 2){
                return;
            } else if (resultCode == 0){
                return;
            }
            else {
                String ticker = data.getStringExtra(FundEditFragment.EXTRA_TICKER);
                mActiveFund.setTicker(ticker);
                FundPortfolio.get(getActivity()).addFund(mActiveFund);
                updateUI();
            }
        } else if (requestCode == REQUEST_EDIT_FUND){
            if (resultCode == 2){
                FundPortfolio.get(getActivity()).deleteFund(mActiveFund);
                updateUI();
            } else if (resultCode == 1) {
                String ticker = data.getStringExtra(FundEditFragment.EXTRA_TICKER);
                mActiveFund.setTicker(ticker);
                FundPortfolio.get(getActivity()).updateFund(mActiveFund);
                updateUI();
            }
        }
    }

    /**
     * Create a new fund, assign it to mActiveFund to manage,
     * then create and show the dialog.
     */
    private void createNewFund(){
        Fund fund = new Fund();
        mActiveFund = fund;
        FragmentManager manager = getFragmentManager();
        FundEditFragment dialog = FundEditFragment.newInstance(fund);
        dialog.setTargetFragment(this, REQUEST_ADD_FUND);
        dialog.show(manager,ADD_FUND_TAG);
    }

    private void editFund(Fund fund){
        mActiveFund = fund;
        FragmentManager manager = getFragmentManager();
        FundEditFragment dialog = FundEditFragment.newInstance(fund);
        dialog.setTargetFragment(this, REQUEST_EDIT_FUND);
        dialog.show(manager, EDIT_FUND_TAG);
    }

    /**
     * Manages one fund entry in the RecyclerView
     */
    private class FundHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private TextView mTitleTextView;
        private TextView mWeightTextView;
        private TextView mPriceTextView;
        private Fund mFund;

        public FundHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView)
                    itemView.findViewById(R.id.list_item_fund_title_text_view);
            mWeightTextView = (TextView)
                    itemView.findViewById(R.id.list_item_fund_weight_text_view);
            mPriceTextView = (TextView)
                    itemView.findViewById(R.id.list_item_fund_price_text_view);
        }

        /**
         * Update the fields of one RecyclerView entry
         * @param fund
         */
        public void bindFund(Fund fund){
            mFund = fund;
            mTitleTextView.setText(mFund.getTicker());
            mWeightTextView.setText(mFund.getWeightText());
        }

        /**
         * What happens when tap on each RecyclerView entry
         * @param v
         */
        @Override
        public void onClick(View v) {
            editFund(mFund);
        }
    }

    /**
     * Manages the RecyclerView as a whole
     */
    private class FundAdapter extends RecyclerView.Adapter<FundHolder> {

        private List<Fund> mFunds;

        /**
         * Constructor: takes in a list of funds to display. Usually the list returned by
         * FundPortfolio.
         * @param funds
         */
        public FundAdapter(List<Fund> funds) {
            mFunds = funds;
        }

        @Override
        public FundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater
                    .inflate(R.layout.list_item_fund, parent, false);
            return new FundHolder(view);
        }

        @Override
        public void onBindViewHolder(FundHolder holder, int position) {
            Fund fund = mFunds.get(position);
            holder.bindFund(fund);
        }

        @Override
        public int getItemCount() {
            return mFunds.size();
        }

        public void setFunds(List<Fund> funds) {
            mFunds = funds;
        }
    }
}


