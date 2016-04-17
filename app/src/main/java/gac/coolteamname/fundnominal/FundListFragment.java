package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

public class FundListFragment extends Fragment {

    private static final String DIALOG_QUERY = "DialogQuery";
    private static final String DIALOG_DELETE = "DialogDelete";

    private static final int REQUEST_FUND = 0;
    private static final int REQUEST_DELETION = 1;

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
        mNewfundButton.setOnClickListener(new View.OnClickListener() {

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

    private void updateUI() {
        List<Fund> funds = FundPortfolio.get(getActivity()).getFunds();

        if (mAdapter == null) {
            mAdapter= new FundAdapter(funds);
            mFundRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setFunds(funds);
            mAdapter.notifyDataSetChanged();
        }

        mAdapter = new FundAdapter(funds);
        mFundRecyclerView.setAdapter(mAdapter);

        if (funds.isEmpty()) {
            mFundRecyclerView.setVisibility(View.GONE);
            mFundEmptyView.setVisibility(View.VISIBLE);
        }
        else {
            mFundRecyclerView.setVisibility(View.VISIBLE);
            //mFundEmptyView.setVisibility(View.GONE);
        }
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

        if (requestCode == REQUEST_DELETION) {
            Fund fund = (Fund) data.getSerializableExtra(DeleteFragment.FUND_DELETION);
            FundPortfolio.get(getActivity()).deleteFund(fund);
            updateUI();
        }


    }
}


