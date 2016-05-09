package gac.coolteamname.fundnominal;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
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
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    private boolean mDeleteNotVisible = true;
    private boolean mUndoOn;
    public boolean mRefreshPricesFlag;
    public static boolean mAutoUpdateFlag;
    public static int mWeightCheck;

    private static final int REQUEST_FUND = 0;
    private static final int REQUEST_DELETION = 2;
    private static final int REQUEST_EDIT = 1;

    private static final String SAVED_UNDO_ON = "undo";
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
        outState.putBoolean(SAVED_UNDO_ON, mUndoOn);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fund_list, container, false);

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

        if (savedInstanceState != null) {
            mUndoOn = savedInstanceState.getBoolean(SAVED_UNDO_ON);
        }

        updateUI();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_fund_list, menu);

        MenuItem undoItem = menu.findItem(R.id.menu_item_undo_button);
        if (mUndoOn) {
            undoItem.setTitle(R.string.undo_enabled);
        } else {
            undoItem.setTitle(R.string.undo_disabled);
        }
    }

    /**
     * Process the menu buttons accordingly
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_delete_view:
                if (!mAdapter.areFundsPendingRemoval()) {
                    if (mDeleteNotVisible) {
                        mDeleteNotVisible = false;
                    } else {
                        mDeleteNotVisible = true;
                    }
                    updateUI();
                }
                return true;
            case R.id.menu_item_refresh_prices:
                mRefreshPricesFlag = true;
                updateUI();
                return true;
            case R.id.menu_item_undo_button:
                if (mUndoOn){
                    mUndoOn = false;
                    item.setTitle(R.string.undo_disabled);
                } else {
                    mUndoOn = true;
                    item.setTitle(R.string.undo_enabled);
                }
                ((FundAdapter)mFundRecyclerView.getAdapter()).setUndoOn(mUndoOn);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpRecyclerView(){
        mFundRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFundRecyclerView.setHasFixedSize(true);
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
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
                boolean undoOn = mAdapter.isUndoOn();
                if (undoOn) {
                    mAdapter.pendingRemoval(swipedPosition);
                } else {
                    mAdapter.remove(swipedPosition);
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

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                Fund fund = mAdapter.mFunds.get(position);
                boolean viewInUndoState = mAdapter.fundsPendingRemoval.contains(fund);
                if (!mDeleteNotVisible || viewInUndoState) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mFundRecyclerView);
    }

    private void setUpAnimationDecoratorHelper() {
        mFundRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                if (!initiated) {
                    init();
                }

                if (parent.getItemAnimator().isRunning()) {

                    Log.d(TAG, "item animator is running");

                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    int left = 0;
                    int right = parent.getWidth();

                    int top = 0;
                    int bottom = 0;

                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);
                }

                super.onDraw(c, parent, state);
                Log.d(TAG, "onDraw() called");
            }

        });
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

        mRefreshPricesFlag = false;

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
        //My undo button
        private Button mUndoButton;
        //Recent undo button
        /*private Button mUndoButton;

        private boolean unDone = false;*/

        public FundHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.list_item_fund_linear_layout_one);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_fund_title_text_view);
            mCompanyNameTextView = (TextView) itemView.findViewById(R.id.list_item_fund_company_name_text_view);
            mWeightTextView = (TextView) itemView.findViewById(R.id.list_item_fund_weight_text_view);
            mPriceTextView = (TextView) itemView.findViewById(R.id.list_item_fund_price_text_view);
            mDeleteButton = (ImageButton) itemView.findViewById(R.id.list_item_fund_delete_button);
            //My undo button
            mUndoButton = (Button) itemView.findViewById(R.id.list_item_fund_undo_button);
            //Recent Undo Button
            /*mUndoButton = (Button) itemView.findViewById(R.id.list_transition_item_undo);
            mUndoButton.setVisibility(View.GONE);

            mUndoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setMenuVisibility(true);
                    unDone = true;
                    Animation in = AnimationUtils.makeInAnimation(getContext(), true);
                    Animation out = AnimationUtils.makeOutAnimation(getContext(), true);
                    mUndoButton.startAnimation(out);
                    mUndoButton.setVisibility(View.GONE);
                    mLinearLayout.setVisibility(View.VISIBLE);
                    mLinearLayout.startAnimation(in);
                }
            });*/

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Fragment based delete feature
                    // on click: call a DeleteFragment dialog
                    //FragmentManager manager = getFragmentManager();
                    //DeleteFragment dialog = DeleteFragment.newInstance(mFund);
                    //dialog.setTargetFragment(FundListFragment.this, REQUEST_DELETION);
                    //dialog.show(manager, DIALOG_DELETE);
                    //Recent delete feature
                    /*setMenuVisibility(false);
                    Animation in = AnimationUtils.makeInAnimation(getContext(), false);
                    Animation out = AnimationUtils.makeOutAnimation(getContext(), false);
                    mLinearLayout.startAnimation(out);
                    mLinearLayout.setVisibility(View.INVISIBLE);
                    mUndoButton.setVisibility(View.VISIBLE);
                    mUndoButton.startAnimation(in);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPrice = true;
                            updateUI();
                        }
                    }, 250);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!unDone) {
                                setMenuVisibility(true);
                                mUndoButton.setVisibility(View.GONE);
                                mLinearLayout.setVisibility(View.VISIBLE);
                                unDone = false;
                                mPrice = true;
                                mAutoUpdateFlag = true;
                                FundPortfolio.get(getActivity()).deleteFund(mFund);
                                updateUI();
                            } else {
                                unDone = false;
                            }
                        }
                    }, 3000);*/
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
            //My fund holder implementation
            mFund = fund;
            if (mAdapter.fundsPendingRemoval.contains(mFund)){
                itemView.setOnClickListener(null);
                itemView.setBackgroundColor(Color.RED);
                mTitleTextView.setVisibility(View.INVISIBLE);
                mPriceTextView.setVisibility(View.INVISIBLE);
                mWeightTextView.setVisibility(View.INVISIBLE);
                mDeleteButton.setVisibility(View.GONE);
                mUndoButton.setVisibility(View.VISIBLE);
                mUndoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Runnable pendingRemovalRunnable = mAdapter.pendingRunnables.get(mFund);
                        mAdapter.pendingRunnables.remove(mFund);
                        if (pendingRemovalRunnable != null) {
                            mAdapter.handler.removeCallbacks(pendingRemovalRunnable);
                        }
                        mAdapter.fundsPendingRemoval.remove(mFund);
                        mAdapter.notifyItemChanged(mAdapter.mFunds.indexOf(mFund));
                    }
                });
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorBackground));
                mTitleTextView.setVisibility(View.VISIBLE);
                mWeightTextView.setVisibility(View.VISIBLE);
                mUndoButton.setVisibility(View.GONE);
                mUndoButton.setOnClickListener(null);
                if (mDeleteNotVisible) {
                    mPortfolioPriceText.setVisibility(View.VISIBLE);
                    mPriceTextView.setVisibility(View.VISIBLE);
                    mDeleteButton.setVisibility(View.GONE);
                } else {
                    mPortfolioPriceText.setVisibility(View.GONE);
                    mPriceTextView.setVisibility(View.GONE);
                    mDeleteButton.setVisibility(View.VISIBLE);
                }
                mTitleTextView.setText(mFund.getTicker());
                mWeightTextView.setText(mFund.getWeightText());
                updatePriceText(mFund);
            }
            //Recent holder implementation
            /*if (mDeleteNotVisible){
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
            updatePriceText(mFund);*/
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
                System.out.println("Null");
                toUpdate = true;
            } else {
                if (moreThanTwentyFourHours(fund)) {
                    System.out.println("MT24H");
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
            protected void onPostExecute(Fund fund) {
                //Old retrieve price method
                /*mFund = stock;
                if (mFund.getPrice() != null) {
                    float textSetter = Math.round(mFund.getPrice().floatValue() * 100);
                    if (mPriceTextView.getText().toString().equals("$" + Float.toString(textSetter / 100))) {
                        //nothing
                    } else {
                        mPriceTextView.setText("$" + Float.toString(textSetter / 100));
                    }
                }*/
                //My retrieve price method
                /*if (fund.getPrice() != null){
                    String neatPriceFormat = roundPrice(fund);
                    mPriceTextView.setText(neatPriceFormat);
                } else {
                    mPriceTextView.setText("Could not retrieve price");
                }*/
                //Recent retrieve price method
                /*mFund = fund;
                if (mFund.getPrice() != null) {
                    FundPortfolio.get(getActivity()).updateFund(mFund);
                    float textSetter = Math.round(mFund.getPrice().floatValue() * 100);
                    mPriceTextView.setText("$" + String.format( "%.2", (textSetter / 100)));
                }*/
            }
        }

        public Fund switchViews() {
            return mFund;
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

        private static final int PENDING_REMOVAL_TIMEOUT = 3000;

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
            final Fund fund = mFunds.get(position);

            holder.bindFund(fund);
            /*if (fundsPendingRemoval.contains(fund)) {
                holder.itemView.setBackgroundColor(Color.RED);
                holder.mTitleTextView.setVisibility(View.INVISIBLE);
                holder.mPriceTextView.setVisibility(View.INVISIBLE);
                holder.mWeightTextView.setVisibility(View.INVISIBLE);
                holder.mUndoButton.setVisibility(View.VISIBLE);
                holder.mUndoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Runnable pendingRemovalRunnable = pendingRunnables.get(fund);
                        pendingRunnables.remove(fund);
                        if (pendingRemovalRunnable != null) {
                            handler.removeCallbacks(pendingRemovalRunnable);
                        }
                        fundsPendingRemoval.remove(fund);
                        notifyItemChanged(mFunds.indexOf(fund));
                    }
                });
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorBackground));
                holder.mTitleTextView.setVisibility(View.VISIBLE);
                holder.mPriceTextView.setVisibility(View.VISIBLE);
                holder.mWeightTextView.setVisibility(View.VISIBLE);
                holder.mUndoButton.setVisibility(View.GONE);
                holder.mUndoButton.setOnClickListener(null);
            }*/
        }

        @Override
        public int getItemCount() { return mFunds.size(); }

        public void setFunds(List<Fund> funds) { mFunds = funds; }

        public void setUndoOn(boolean undoOn) {
            this.undoOn = undoOn;
        }

        public boolean isUndoOn() {
            return undoOn;
        }

        public boolean areFundsPendingRemoval() {
            if (fundsPendingRemoval.isEmpty()) {
                return false;
            } else {
                return true;
            }
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
                FundPortfolio.get(getActivity()).deleteFund(fund);
                notifyItemRemoved(position);
            }
        }

        public boolean isPendingRemoval(int position) {
            Fund fund = mFunds.get(position);
            return fundsPendingRemoval.contains(fund);
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