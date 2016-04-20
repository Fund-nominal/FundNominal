package gac.coolteamname.fundnominal;

/**
 * Created by Joel Stremmel on 4/18/2016.
 */

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ComparisonFragment extends Fragment {

    private RecyclerView mSwapRecyclerView;
    private SwapAdapter mAdapter;
    private TextView mSwapsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swap_list, container, false);

        mSwapsText = (TextView) view.findViewById(R.id.swap_text_view);
        List<Fund> comparisonFunds = FundPortfolio.get(getActivity()).getFunds();
        new FetchItemsTask().execute(comparisonFunds);

        mSwapRecyclerView = (RecyclerView) view
                .findViewById(R.id.swap_recycler_view);
        mSwapRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    private void updateUI() {
        List<String> swaps = Utilities.ExchangeOptions(FundPortfolio.get(getActivity()).getOvers(),
                FundPortfolio.get(getActivity()).getUnders());

        // Update the RecyclerView
        if (mAdapter == null) {
            mAdapter = new SwapAdapter(swaps);
            mSwapRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setSwaps(swaps);
            mAdapter.notifyDataSetChanged();
        }

        if (swaps.isEmpty()) {
            // If there is no swap, hide RecyclerView, display message
            mSwapRecyclerView.setVisibility(View.GONE);
        }
        else {
            // If there are swap(s), hide message, display RecyclerView
            mSwapRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class SwapHolder extends RecyclerView.ViewHolder {

        private TextView mSwapTextView;
        private String mSwap;


        public SwapHolder(View itemView) {
            super(itemView);

            mSwapTextView = (TextView) itemView.findViewById(R.id.list_item_swap_title_text_view);
        }
        /**
         *
         */
        public void bindSwap(String swap){
            mSwap = swap;
            mSwapTextView.setText(swap);
        }

    }

    private class SwapAdapter extends RecyclerView.Adapter<SwapHolder> {

        private List<String> mSwaps;

        /**
         * Constructor: takes in a list of strings to display. The list returned by ExchangeOptions in Utilities.
         * @param
         */
        public SwapAdapter(List<String> swaps) {
            mSwaps = swaps;
        }

        @Override
        public SwapHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater
                    .inflate(R.layout.list_item_swap, parent, false);
            return new SwapHolder(view);
        }

        @Override
        public void onBindViewHolder(SwapHolder holder, int position) {
            String swap = mSwaps.get(position);
            holder.bindSwap(swap);
        }

        @Override
        public int getItemCount() {
            return mSwaps.size();
        }

        public void setSwaps(List<String> swaps) {
            mSwaps = swaps;
        }
    }

    private class FetchItemsTask extends AsyncTask<List<Fund>, Void, List<Fund>> {
        @Override
        protected List<Fund> doInBackground(List<Fund>... params) {
            return new PricesFetcher().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(List<Fund> updatedFunds) {
            List<String> comparisons = Utilities.ExchangeOptions(FundPortfolio.get(getActivity()).getOvers(),
                    FundPortfolio.get(getActivity()).getUnders());
            for (String string : comparisons) {
                System.out.println(string);
            }
            if (comparisons.size() > 0) {
                if (comparisons.get(0) != null) {
                    mSwapsText.setText(comparisons.get(0));
                }
            }
        }
    }
}
