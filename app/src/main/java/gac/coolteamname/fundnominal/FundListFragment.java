package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
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

import java.util.HashMap;
import java.util.List;

/**
 * FundListFragment is a fragment that display a list of funds in the database, using RecyclerView
 */
public class FundListFragment extends Fragment {

    private EditText mPortfolioName;
    private TextView mPortfolioFundText;
    private TextView mPortfolioPriceText;
    private RecyclerView mFundRecyclerView;
    private FundAdapter mAdapter;
    private RelativeLayout mFundEmptyView;
    private Button mNewPortfolioButton;
    private FloatingActionButton mNewFundButton;
    private boolean mSubtitleVisible;

    private boolean mDeleteNotVisible = true;
    public boolean mRefreshPricesFlag;
    public static boolean mAutoUpdateFlag;
    public static int mWeightCheck;

    private static final int REQUEST_FUND = 0;
    private static final int REQUEST_DELETION = 2;
    private static final int REQUEST_EDIT = 1;

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
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
        // preserve subtitle state through destroy and create cycles
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fund_list, container, false);

        // This currently doesn't do anything, as it is hardcoded into the XML
        mPortfolioFundText = (TextView) view.findViewById(R.id.portfolio_fund_text_view);
        mPortfolioPriceText = (TextView) view.findViewById(R.id.portfolio_price_text_view);

        // Set the PortfolioName to the first fund's Portfolio name
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
        setUpRecyclerView();

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
                if (mDeleteNotVisible) {
                    mDeleteNotVisible = false;
                } else {
                    mDeleteNotVisible = true;
                }
                updateUI();
                return true;
            case R.id.menu_item_refresh_prices:
                mRefreshPricesFlag = true;
                updateUI();
                return true;
            case R.id.menu_item_undo_checkbox:
                if (item.isChecked()){
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                ((FundAdapter)mFundRecyclerView.getAdapter()).setUndoOn(item.isChecked());
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpRecyclerView(){
        mFundRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFundRecyclerView.setHasFixedSize(true);
        setUpItemTouchHelper();
        //setUpAnimationDecoratorHelper();
    }

    private void setUpItemTouchHelper(){

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper
                .SimpleCallback(0, ItemTouchHelper.LEFT) {
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                xMark = ContextCompat.getDrawable(getActivity(), R.drawable.ic_clear_24dp);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) getActivity().getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                FundAdapter adapter = (FundAdapter) mFundRecyclerView.getAdapter();
                boolean undoOn = adapter.isUndoOn();
                if (undoOn) {
                    adapter.pendingRemoval(swipedPosition);
                } else {
                    adapter.remove(swipedPosition);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder
                                    viewHolder, float dX, float dY, int actionState,
                                    boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                if (viewHolder.getAdapterPosition() == -1) {
                    return;
                }

                if (!initiated) {
                    init();
                }

                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(),
                        itemView.getBottom());
                background.draw(c);

                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mFundRecyclerView);
    }

    /**
     * Update Portfolio Name for every currently displayed Funds
     * @param beforeChanged old portfolio name
     * @param afterChanged new portfolio name
     */
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

        mRefreshPricesFlag = false;

        if (funds.isEmpty()) {
            // If there is no fund, hide RecyclerView, display message
            mFundRecyclerView.setVisibility(View.GONE);
            mPortfolioName.setVisibility(View.GONE);
            mPortfolioFundText.setVisibility(View.GONE);
            mPortfolioPriceText.setVisibility(View.GONE);
            mNewFundButton.setVisibility(View.GONE);
            mFundEmptyView.setVisibility(View.VISIBLE);
        }
        else {
            // If there are fund(s), hide message, display RecyclerView
            mFundRecyclerView.setVisibility(View.VISIBLE);
            mPortfolioName.setVisibility(View.VISIBLE);
            mPortfolioFundText.setVisibility(View.VISIBLE);
            mPortfolioPriceText.setVisibility(View.VISIBLE);
            mNewFundButton.setVisibility(View.VISIBLE);
            mFundEmptyView.setVisibility(View.GONE);
        }
    }

    private class FundHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mWeightTextView;
        private TextView mPriceTextView;
        private Fund mFund;
        private ImageButton mDeleteButton;
        private Button mUndoButton;

        public FundHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_fund_title_text_view);
            mWeightTextView = (TextView) itemView.findViewById(R.id.list_item_fund_weight_text_view);
            mPriceTextView = (TextView) itemView.findViewById(R.id.list_item_fund_price_text_view);
            mDeleteButton = (ImageButton) itemView.findViewById(R.id.list_item_fund_delete_button);
            mUndoButton = (Button) itemView.findViewById(R.id.list_item_fund_undo_button);

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // on click: call a DeleteFragment dialog
                    FragmentManager manager = getFragmentManager();
                    DeleteFragment dialog = DeleteFragment.newInstance(mFund);
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
            if (mDeleteNotVisible) {
                mDeleteButton.setVisibility(View.GONE);
                mPortfolioPriceText.setVisibility(View.VISIBLE);
                mPriceTextView.setVisibility(View.VISIBLE);
            } else {
                mPortfolioPriceText.setVisibility(View.GONE);
                mPriceTextView.setVisibility(View.GONE);
                mDeleteButton.setVisibility(View.VISIBLE);
            }
            mFund = fund;
            mTitleTextView.setText(mFund.getTicker());
            mWeightTextView.setText(mFund.getWeightText());
            updatePriceText(mFund);
        }

        private void updatePriceText(Fund fund) {
            if (mRefreshPricesFlag || fund.getPrice() == null) {
                new FetchItemsTask().execute(fund);
            } else {
                String neatPriceFormat = roundPrice(fund);
                mPriceTextView.setText(neatPriceFormat);
            }
        }

        private String roundPrice(Fund fund) {
            float roundedPrice = Math.round(fund.getPrice().floatValue() * 100 / 100);
            String roundedPriceString = "$" + Float.toString(roundedPrice);
            return roundedPriceString;
        }

        private class FetchItemsTask extends AsyncTask<Fund, Void, Fund> {
            @Override
            protected Fund doInBackground(Fund... params) {
                return new FinanceFetcher().fetchItems(params[0]);
            }

            @Override
            protected void onPostExecute(Fund fund) {
                /*mFund = stock;
                if (mFund.getPrice() != null) {
                    float textSetter = Math.round(mFund.getPrice().floatValue() * 100);
                    if (mPriceTextView.getText().toString().equals("$" + Float.toString(textSetter / 100))) {
                        //nothing
                    } else {
                        mPriceTextView.setText("$" + Float.toString(textSetter / 100));
                    }
                }*/
                if (fund.getPrice() != null){
                    String neatPriceFormat = roundPrice(fund);
                    mPriceTextView.setText(neatPriceFormat);
                } else {
                    mPriceTextView.setText("Could not retrieve price");
                }
            }
        }
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

        private static final int PENDING_REMOVAL_TIMEOUT = 4000;

        private List<Fund> mFunds;
        private List<Fund> fundsPendingRemoval;
        boolean undoOn;

        private Handler handler = new Handler();
        HashMap<Fund, Runnable> pendingRunnables = new HashMap<>();

        /**
         * Constructor: takes in a list of funds to display. Usually the list returned by
         * FundPortfolio.
         * @param funds
         */
        public FundAdapter(List<Fund> funds) {
            mFunds = funds;
            fundsPendingRemoval = funds;
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
            FundHolder viewHolder = (FundHolder) holder;
            final Fund fund = mFunds.get(position);
            holder.bindFund(fund);

            if (fundsPendingRemoval.contains(fund)) {
                viewHolder.itemView.setBackgroundColor(Color.RED);
                viewHolder.mTitleTextView.setVisibility(View.GONE);
                viewHolder.mPriceTextView.setVisibility(View.GONE);
                viewHolder.mWeightTextView.setVisibility(View.GONE);
                viewHolder.mUndoButton.setVisibility(View.VISIBLE);
                viewHolder.mUndoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Runnable pendingRemovalRunnable = pendingRunnables.get(fund);
                        pendingRunnables.remove(fund);
                        if (pendingRemovalRunnable != null) handler.removeCallbacks(pendingRemovalRunnable);
                        fundsPendingRemoval.remove(fund);
                        notifyItemChanged(mFunds.indexOf(fund));
                    }
                });
            } else {
                viewHolder.itemView.setBackgroundColor(Color.WHITE);
                viewHolder.mTitleTextView.setVisibility(View.VISIBLE);
                viewHolder.mPriceTextView.setVisibility(View.VISIBLE);
                viewHolder.mWeightTextView.setVisibility(View.VISIBLE);
                viewHolder.mUndoButton.setVisibility(View.VISIBLE);
                viewHolder.mUndoButton.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return mFunds.size();
        }

        public void setFunds(List<Fund> funds) {
            mFunds = funds;
        }

        public void setUndoOn(boolean undoOn) {
            this.undoOn = undoOn;
        }

        public boolean isUndoOn() {
            return undoOn;
        }

        public void pendingRemoval(int position) {
            final Fund fund = mFunds.get(position);
            if (!fundsPendingRemoval.contains(fund)) {
                fundsPendingRemoval.add(fund);
                notifyItemChanged(position);
                Runnable pendingRemovalRunnable = new Runnable() {
                    @Override
                    public void run() {
                        remove(mFunds.indexOf(fund));
                    }
                };
                handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
                pendingRunnables.put(fund, pendingRemovalRunnable);
            }
        }

        public void remove(int position) {
            Fund fund = mFunds.get(position);
            if (fundsPendingRemoval.contains(fund)) {
                fundsPendingRemoval.remove(fund);
            }
            if (mFunds.contains(fund)) {
                mFunds.remove(position);
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