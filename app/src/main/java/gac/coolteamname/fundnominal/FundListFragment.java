package gac.coolteamname.fundnominal;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Handler;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * FundListFragment is a fragment that display a list of funds in the database, using RecyclerView
 */
public class FundListFragment extends Fragment {

    private RecyclerView mFundRecyclerView;
    private FundAdapter mAdapter;
    private RelativeLayout mFundEmptyView;
    private Button mNewPortfolioButton;
    private FloatingActionButton mNewFundButton;

    private boolean mPrice = true;
    public static boolean mAutoUpdateFlag = false;
    public static int mWeightCheck;

    private static final int REQUEST_FUND = 0;
    private static final int REQUEST_DELETION = 2;
    private static final int REQUEST_EDIT = 1;

    private static final String DIALOG_QUERY = "DialogQuery";
    private static final String DIALOG_DELETE = "DialogDelete";
    private static final String DIALOG_EDIT = "DialogEdit";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enable Options Menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fund_list, container, false);

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

        mNewFundButton = (FloatingActionButton) view.findViewById(R.id.new_fund_button);
        mNewFundButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                StockQueryFragment query = new StockQueryFragment();
                query.setTargetFragment(FundListFragment.this, REQUEST_FUND);
                query.show(manager, DIALOG_QUERY);
            }
        });

        updateUI();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_fund_list, menu);

    }

    /**
     * Process the menu buttons accordingly
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_delete_view:
                if (mPrice) {
                    mPrice = false;
                } else {
                    mPrice = true;
                }
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Repopulate RecyclerView and update info for each Fund.
     * If there is no Fund, display a message and a button to add Fund.
     */
    private void updateUI() {
        List<Fund> funds = FundPortfolio.get(getActivity()).getFunds();
        funds = Utilities.sortFunds(funds);

        // Update the RecyclerView
        if (mAdapter == null) {
            mAdapter = new FundAdapter(funds);
            mFundRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setFunds(funds);
            mAdapter.notifyDataSetChanged();
        }

        if (funds.isEmpty()) {
            // If there is no fund, hide RecyclerView, display message
            mFundRecyclerView.setVisibility(View.GONE);
            mNewFundButton.setVisibility(View.GONE);
            mFundEmptyView.setVisibility(View.VISIBLE);
        }
        else {
            // If there are fund(s), hide message, display RecyclerView
            mFundRecyclerView.setVisibility(View.VISIBLE);
            mNewFundButton.setVisibility(View.VISIBLE);
            mFundEmptyView.setVisibility(View.GONE);
        }
    }

    private class FundHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout mLinearLayout;
        private TextView mTitleTextView;
        private TextView mCompanyNameTextView;
        private TextView mWeightTextView;
        private TextView mPriceTextView;
        private Fund mFund;
        private ImageButton mDeleteButton;
        private Button mUndoButton;

        public FundHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.list_item_fund_linear_layout_one);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_fund_title_text_view);
            mCompanyNameTextView = (TextView) itemView.findViewById(R.id.list_item_fund_company_name_text_view);
            mWeightTextView = (TextView) itemView.findViewById(R.id.list_item_fund_weight_text_view);
            mPriceTextView = (TextView) itemView.findViewById(R.id.list_item_fund_price_text_view);
            mDeleteButton = (ImageButton) itemView.findViewById(R.id.list_item_fund_delete_button);
            mUndoButton = (Button) itemView.findViewById(R.id.list_transition_item_undo);
            mUndoButton.setVisibility(View.GONE);

            mUndoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation in = AnimationUtils.makeInAnimation(getContext(), true);
                    Animation out = AnimationUtils.makeOutAnimation(getContext(), true);
                    mUndoButton.startAnimation(out);
                    mUndoButton.setVisibility(View.GONE);
                    mLinearLayout.setVisibility(View.VISIBLE);
                    mLinearLayout.startAnimation(in);
                    Runnable pendingRemovalRunnable = mAdapter.pendingRunnables.get(mFund);
                    mAdapter.pendingRunnables.remove(mFund);
                    if (pendingRemovalRunnable != null) mAdapter.mHandler.removeCallbacks(pendingRemovalRunnable);
                    mAdapter.fundsPendingRemoval.remove(mFund.getTicker());
                    mAdapter.notifyItemChanged(mAdapter.mFunds.indexOf(mFund));
                }
            });

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    mAdapter.pendingRemoval(position);
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
            mWeightCheck = mFund.getWeight();
            FragmentManager manager = getFragmentManager();
            FundEditFragment dialog = FundEditFragment.newInstance(mFund);
            dialog.setTargetFragment(FundListFragment.this, REQUEST_EDIT);
            dialog.show(manager, DIALOG_EDIT);
        }

        /**
         * Update the fields of one RecyclerView entry
         * @param fund the fund to update
         */
        public void bindFund(Fund fund){
            if (mPrice) {
                mDeleteButton.setVisibility(View.GONE);
                mPriceTextView.setVisibility(View.VISIBLE);
                if (updatePrice(fund)) {
                    new FetchItemsTask().execute(fund);
                } else {
                    float textSetter = Math.round(fund.getPrice().floatValue() * 100);
                    mPriceTextView.setText("$" + String.format( "%.2f", (textSetter / 100)));
                }
                mWeightTextView.setVisibility(View.VISIBLE);
            } else {
                mPriceTextView.setVisibility(View.GONE);
                mWeightTextView.setVisibility(View.GONE);
                mDeleteButton.setVisibility(View.VISIBLE);
            }
            mFund = fund;
            mTitleTextView.setText(mFund.getTicker());
            mCompanyNameTextView.setText(mFund.getCompanyName());
            setWeight();
        }

        private void setWeight(){
            //StateListDrawable listDrawable = (StateListDrawable) mWeightTextView.getBackground();
            //GradientDrawable drawable = (GradientDrawable) listDrawable.getCurrent();
            if (mFund.getWeightText() == "Underweight") {
                mWeightTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.UnderIndicator));
                mWeightTextView.setText("UNDER");
            }
            if (mFund.getWeightText() == "Overweight") {
                mWeightTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.OverIndicator));
                mWeightTextView.setText("OVER");
            }
            if (mFund.getWeightText() == "Normal") {
                mWeightTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.NormalIndicator));
                mWeightTextView.setText("NORMAL");
            }
        }

        private boolean updatePrice(Fund fund) {
            boolean toUpdate = false;
            TimeZone tz = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Calendar today = Calendar.getInstance();
            Date date = today.getTime();

            if (fund.getPrice() == null || fund.getTimePriceChecked() == null) {
                toUpdate = true;
            } else {
                if (moreThanTwentyFourHours(fund)) {
                    toUpdate = true;
                } else {
                    if (beforeClose(fund.getTimePriceChecked()) && beforeClose(date) &&
                            sameDate(fund.getTimePriceChecked(), date)) {}
                    else if (afterClose(fund.getTimePriceChecked()) && beforeClose(date)) {}
                    else if (afterClose(fund.getTimePriceChecked()) && afterClose(date) &&
                            sameDate(fund.getTimePriceChecked(), date)){}
                    else {
                        toUpdate = true;
                    }
                }
            }

            TimeZone.setDefault(tz);

            fund.setTimePriceChecked(date);
            FundPortfolio.get(getActivity()).updateFund(fund);

            return toUpdate;
        }

        private boolean sameDate(Date date1, Date date2) {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            if (date1.getDate() == date2.getDate()) {
                return true;
            } else {
                return false;
            }
        }

        private boolean beforeClose(Date date) {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            int closeTime = 21;
            if (date.getHours() < closeTime) {
                return true;
            } else {
                return false;
            }
        }

        private boolean afterClose(Date date) {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            int closeTime = 21;
            if (date.getHours() >= closeTime) {
                return true;
            } else {
                return false;
            }
        }

        private boolean moreThanTwentyFourHours(Fund fund) {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            Date dateYesterday = yesterday.getTime();

            if (fund.getTimePriceChecked().compareTo(dateYesterday) < 0) {
                return true;
            } else {
                return false;
            }
        }

        private class FetchItemsTask extends AsyncTask<Fund, Void, Fund> {
            @Override
            protected Fund doInBackground(Fund... params) {
                return new FinanceFetcher().fetchItems(params[0]);
            }

            @Override
            protected void onPostExecute(Fund stock) {
                mFund = stock;
                if (mFund.getPrice() != null) {
                    FundPortfolio.get(getActivity()).updateFund(mFund);
                    float textSetter = Math.round(mFund.getPrice().floatValue() * 100);
                    mPriceTextView.setText("$" + String.format( "%.2f", (textSetter / 100)));
                }
            }
        }

        public Fund switchViews() { return mFund; }
    }

    @Override
    public void onResume() {
        super.onResume();
        mFundRecyclerView.setAdapter(mAdapter);
        updateUI();
    }

    /**
     * Class: FundAdapter manages the whole RecyclerView and every elements of it
     */
    private class FundAdapter extends RecyclerView.Adapter<FundHolder> {

        private List<Fund> mFunds;
        private List<String> fundsPendingRemoval;

        private Handler mHandler = new Handler();
        HashMap<Fund, Runnable> pendingRunnables = new HashMap<>();

        private final int DELAY_TIME = 3000;

        /**
         * Constructor: takes in a list of funds to display. Usually the list returned by
         * FundPortfolio.
         * @param funds
         */
        public FundAdapter(List<Fund> funds) {
            mFunds = funds;
            fundsPendingRemoval = new ArrayList<>();
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

            if (fundsPendingRemoval.contains(fund.getTicker())) {
                Animation in = AnimationUtils.makeInAnimation(getContext(), false);
                Animation out = AnimationUtils.makeOutAnimation(getContext(), false);
                holder.mLinearLayout.startAnimation(out);
                holder.mLinearLayout.setVisibility(View.INVISIBLE);
                holder.mUndoButton.setVisibility(View.VISIBLE);
                holder.mUndoButton.startAnimation(in);
            } else {
                holder.mLinearLayout.setVisibility(View.VISIBLE);
                holder.mUndoButton.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() { return mFunds.size(); }

        public void setFunds(List<Fund> funds) { mFunds = funds; }
        public void setFunds(List<Fund> funds) {
            mFunds = funds;
        }

        public void pendingRemoval(int position) {
            final Fund fund = mFunds.get(position);
            if (!fundsPendingRemoval.contains(fund)) {
                fundsPendingRemoval.add(fund.getTicker());
                notifyItemChanged(position);
                Runnable pendingRemovalRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Fund getFund = new Fund();
                        for (Fund fund1: mFunds) {
                            if (fund1.getTicker().equals(fund.getTicker())) {
                                 getFund = fund1;
                            }
                        }
                        remove(mFunds.indexOf(getFund));
                        mAutoUpdateFlag = true;
                    }
                };
                mHandler.postDelayed(pendingRemovalRunnable, DELAY_TIME);
                pendingRunnables.put(fund, pendingRemovalRunnable);
            }
        }

        public void remove(int position) {
            Fund fund = mFunds.get(position);
            if (fundsPendingRemoval.contains(fund)) {
                fundsPendingRemoval.remove(fund.getTicker());
            }
            if (mFunds.contains(fund)) {
                mFunds.remove(position);
                FundPortfolio.get(getActivity()).deleteFund(fund);
                notifyItemRemoved(position);
            }
        }
    }

    /**
     * Process data when returning from other activities
     * @param resultCode Switch function that chooses option depending on the requestCode
     * @param requestCode If resultCode is different from Activity.RESULT_OK (-1)
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
                // set the update flag when a fund has been added
                mAutoUpdateFlag = true;
                break;
            case REQUEST_DELETION:
                mFund = (Fund) data.getSerializableExtra(DeleteFragment.FUND_DELETION);
                FundPortfolio.get(getActivity()).deleteFund(mFund);
                updateUI();
                // set the update flag when a fund has been deleted
                mAutoUpdateFlag = true;
                break;
            case REQUEST_EDIT:
                mFund = (Fund) data.getSerializableExtra(FundEditFragment.EXTRA_FUND);
                FundPortfolio.get(getActivity()).updateFund(mFund);
                updateUI();
                if (mWeightCheck != mFund.getWeight()) {
                    // set the update flag when a fund has been edited
                    mAutoUpdateFlag = true;
                }
                break;
        }
    }
}