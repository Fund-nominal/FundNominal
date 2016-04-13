package gac.coolteamname.fundnominal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class FundListFragment extends Fragment {

    private RecyclerView mFundRecyclerView;
    private FundAdapter mAdapter;
    private RelativeLayout mFundEmptyView;
    private Button mNewfundButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fund_list, container, false);

        mFundRecyclerView = (RecyclerView) view
                .findViewById(R.id.fund_recycler_view);
        mFundRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mFundEmptyView = (RelativeLayout) view.findViewById(R.id.empty_fund_list_display);
        mNewfundButton = (Button) view.findViewById(R.id.new_fund_button);
        /*mNewfundButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                createNewFund();
            }
        });*/

        updateUI();

        return view;
    }

    private void updateUI() {
        List<Fund> crimes = FundPortfolio.get(getActivity()).getFunds();

        if (mAdapter == null) {
            mAdapter= new FundAdapter(crimes);
            mFundRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setFunds(crimes);
            mAdapter.notifyDataSetChanged();
        }

        mAdapter = new FundAdapter(crimes);
        mFundRecyclerView.setAdapter(mAdapter);

        if (crimes.isEmpty()) {
            mFundRecyclerView.setVisibility(View.GONE);
            mFundEmptyView.setVisibility(View.VISIBLE);
        }
        else {
            mFundRecyclerView.setVisibility(View.VISIBLE);
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
            //mWeightTextView.setText(mFund.getWeight());
        }

        @Override
        public void onClick(View v) {
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
}


