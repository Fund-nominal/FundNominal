package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

public class FundListFragment extends Fragment {

    private EditText mPortfolioName;
    private TextView mPortfolioText;
    private RecyclerView mFundRecyclerView;
    private FundAdapter mAdapter;
    private RelativeLayout mFundEmptyView;
    private Button mNewPortfolioButton;
    private Button mNewFundButton;
    private boolean mSubtitleVisible;

    private static final int REQUEST_FUND = 0;
    private static final int REQUEST_DELETION = 2;
    private static final int REQUEST_EDIT = 1;

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private static final String DIALOG_QUERY = "DialogQuery";
    private static final String DIALOG_DELETE = "DialogDelete";
    private static final String DIALOG_EDIT = "DialogEdit";

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

        mPortfolioText = (TextView) view.findViewById(R.id.portfolio_text_view);

        mPortfolioName = (EditText) view.findViewById(R.id.portfolio_edit_text);
        List<Fund> fundInits = FundPortfolio.get(getActivity()).getFunds();
        if (fundInits.size() > 0) {
            if (fundInits.get(0).getPortfolioName() != null) {
                mPortfolioName.setText(fundInits.get(0).getPortfolioName());
            }
        }
        mPortfolioName.addTextChangedListener(new TextWatcher() {
            String beforeChanged;
            String afterChanged;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeChanged = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                afterChanged = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePortfolioName(beforeChanged, afterChanged);
            }
        });

        mFundRecyclerView = (RecyclerView) view
                .findViewById(R.id.fund_recycler_view);
        mFundRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mFundEmptyView = (RelativeLayout) view.findViewById(R.id.empty_fund_list_display);
        mNewPortfolioButton = (Button) view.findViewById(R.id.new_portfolio_button);
        mNewPortfolioButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                StockQueryFragment query = new StockQueryFragment();
                query.setTargetFragment(FundListFragment.this, REQUEST_FUND);
                query.show(manager, DIALOG_QUERY);
            }
        });

        mNewFundButton = (Button) view.findViewById(R.id.new_fund_button);
        mNewFundButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                StockQueryFragment query = new StockQueryFragment();
                query.setTargetFragment(FundListFragment.this, REQUEST_FUND);
                query.show(manager, DIALOG_QUERY);
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
            /*case R.id.menu_item_new_fund:
                createNewFund();
                return true;*/
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

    private void updatePortfolioName(String beforeChanged, String afterChanged) {
        List<Fund> funds = FundPortfolio.get(getActivity()).getFunds();
        for (Fund fund : funds) {
            if (fund.getPortfolioName() == (null) ||
                fund.getPortfolioName().equals(beforeChanged)) {
                fund.setPortfolioName(afterChanged);
                FundPortfolio.get(getActivity()).updateFund(fund);
            }
        }
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
            mPortfolioName.setVisibility(View.GONE);
            mPortfolioText.setVisibility(View.GONE);
            mNewFundButton.setVisibility(View.GONE);
            mFundEmptyView.setVisibility(View.VISIBLE);
        }
        else {
            // If there are fund(s), hide message, display RecyclerView
            mFundRecyclerView.setVisibility(View.VISIBLE);
            mPortfolioName.setVisibility(View.VISIBLE);
            mPortfolioText.setVisibility(View.VISIBLE);
            mNewFundButton.setVisibility(View.VISIBLE);
            mFundEmptyView.setVisibility(View.GONE);
        }
        updateSubtitle();
    }

    private class FundHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mWeightTextView;
        private TextView mPriceTextView;
        private Fund mFund;
        private ImageButton mDeleteButton;

        //the delete button should be put somewhere here

        public FundHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_fund_title_text_view);
            mWeightTextView = (TextView) itemView.findViewById(R.id.list_item_fund_weight_text_view);
            mPriceTextView = (TextView) itemView.findViewById(R.id.list_item_fund_price_text_view);
            mDeleteButton = (ImageButton) itemView.findViewById(R.id.list_item_fund_delete_button);

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    FragmentManager manager = getFragmentManager();
                    DeleteFragment dialog = DeleteFragment.newInstance(mFund.getTicker(), mFund);
                    dialog.setTargetFragment(FundListFragment.this, REQUEST_DELETION);
                    dialog.show(manager, DIALOG_DELETE);
                }
            });
        }

        /**
         * What happens when tap on each RecyclerView entry
         * @param v
         */
        @Override
        public void onClick(View v) {
            //editFund(mFund);
            FragmentManager manager = getFragmentManager();
            FundEditFragment dialog = FundEditFragment.newInstance(mFund);
            dialog.setTargetFragment(FundListFragment.this, REQUEST_EDIT);
            dialog.show(manager, DIALOG_EDIT);
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
    }

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

    /**
     * @param resultCode
     * Switch function that chooses option
     * depending on the requestCode
     * @param requestCode
     * If resultCode is different from Activity.RESULT_OK (-1)
     * then the onActivityResult does nothing (when the user presses cancel for example)
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fund mFund;
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode){
            case REQUEST_FUND:
                mFund = (Fund) data.getSerializableExtra(StockQueryFragment.EXTRA_FUND);
                FundPortfolio.get(getActivity()).addFund(mFund);
                updateUI();
                break;
            case REQUEST_DELETION:
                mFund = (Fund) data.getSerializableExtra(DeleteFragment.FUND_DELETION);
                FundPortfolio.get(getActivity()).deleteFund(mFund);
                updateUI();
                break;
            case REQUEST_EDIT:
                mFund = (Fund) data.getSerializableExtra(FundEditFragment.EXTRA_FUND);
                FundPortfolio.get(getActivity()).updateFund(mFund);
                updateUI();
        }
    }

    private class FetchItemsTask extends AsyncTask<Fund, Void, Fund> {
        @Override
        protected Fund doInBackground(Fund... params) {
            return new FinanceFetcher().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(Fund stock) {

        }
    }
}


