package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class FundListFragment extends Fragment{

    private EditText mPortfolioName;
    private TextView mPortfolioText;
    private RecyclerView mFundRecyclerView;
    private FundAdapter mAdapter;
    private RelativeLayout mFundEmptyView;
    private Button mNewPortfolioButton;
    private Button mNewFundButton;

    private static final String DIALOG_QUERY = "DialogQuery";
    private static final int REQUEST_FUND = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        updateUI();

        return view;
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

    private void updateUI() {
        List<Fund> funds = FundPortfolio.get(getActivity()).getFunds();
        for (Fund fund : funds) {
            System.out.println(fund.getTicker() + " : " + fund.getWeight());
        }

        if (mAdapter == null) {
            mAdapter= new FundAdapter(funds);
            mFundRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setFunds(funds);
            mAdapter.notifyDataSetChanged();
        }

        if (funds.isEmpty()) {
            mFundRecyclerView.setVisibility(View.GONE);
            mPortfolioName.setVisibility(View.GONE);
            mPortfolioText.setVisibility(View.GONE);
            mNewFundButton.setVisibility(View.GONE);
            mFundEmptyView.setVisibility(View.VISIBLE);
        }
        else {
            mFundRecyclerView.setVisibility(View.VISIBLE);
            mPortfolioName.setVisibility(View.VISIBLE);
            mPortfolioText.setVisibility(View.VISIBLE);
            mNewFundButton.setVisibility(View.VISIBLE);
            mFundEmptyView.setVisibility(View.GONE);
        }
    }

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

        public void bindFund(Fund fund){
            mFund = fund;
            mTitleTextView.setText(mFund.getTicker());
            mWeightTextView.setText(setWeightText(mFund));
        }

        @Override
        public void onClick(View v) {
        }

    }

    private String setWeightText(Fund fund) {
        if (fund.getWeight() == 1) {
            return "Overweight";
        } else if (fund.getWeight() == 0) {
            return "Normal";
        } else {
            return "Underweight";
        }
    }

    private class FundAdapter extends RecyclerView.Adapter<FundHolder> {

        private List<Fund> mFunds;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_FUND) {
            Fund fund = (Fund) data.getSerializableExtra(StockQueryFragment.EXTRA_FUND);
            FundPortfolio.get(getActivity()).addFund(fund);
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


