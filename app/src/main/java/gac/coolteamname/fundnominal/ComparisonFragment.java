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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ComparisonFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";

    private RecyclerView mSwapRecyclerView;
    private SwapAdapter mAdapter;
    private TextView mSwapsText;
    private Button mCompareButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swap_list, container, false);

        mSwapsText = (TextView) view.findViewById(R.id.swap_text_view);
        List<Fund> overs = FundPortfolio.get(getActivity()).getOvers();
        List<Fund> unders = FundPortfolio.get(getActivity()).getUnders();
        new FetchItemsTask().execute(overs, unders);

        mSwapRecyclerView = (RecyclerView) view
                .findViewById(R.id.swap_recycler_view);
        mSwapRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        mCompareButton = (Button) view.findViewById(R.id.compare_button);
        mCompareButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                List<Fund> overs = FundPortfolio.get(getActivity()).getOvers();
                List<Fund> unders = FundPortfolio.get(getActivity()).getUnders();
                new FetchItemsTask().execute(overs, unders);
            }
        });

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_swap_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_refresh_icon:
                List<Fund> overs = FundPortfolio.get(getActivity()).getOvers();
                List<Fund> unders = FundPortfolio.get(getActivity()).getUnders();
                new FetchItemsTask().execute(overs, unders);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    private class FetchItemsTask extends AsyncTask<List<Fund>, Void, List<List<Fund>>> {
        @Override
        protected List<List<Fund>> doInBackground(List<Fund>... params) {
            List<List<Fund>> oversUnders = new ArrayList<>();
            oversUnders.add(new PricesFetcher().fetchItems(params[0]));
            oversUnders.add(new PricesFetcher().fetchItems(params[1]));
            return oversUnders;
        }

        @Override
        protected void onPostExecute(List<List<Fund>> oversUnders) {
            List<String> comparisons = Utilities.ExchangeOptions(oversUnders.get(0),
                    oversUnders.get(1));

            // Update the RecyclerView
            if (mAdapter == null) {
                mAdapter = new SwapAdapter(comparisons);
                mSwapRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.setSwaps(comparisons);
                mAdapter.notifyDataSetChanged();
            }

            if (comparisons.isEmpty()) {
                // If there is no swap, hide RecyclerView, display message
                mSwapRecyclerView.setVisibility(View.GONE);
            }
            else {
                // If there are swap(s), hide message, display RecyclerView
                mSwapRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
