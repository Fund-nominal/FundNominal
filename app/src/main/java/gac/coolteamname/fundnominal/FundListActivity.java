package gac.coolteamname.fundnominal;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * FundListActivity is the activity that hosts just a single fragment, "FundListFragment",
 * displaying all the Funds that the user currently have in the Portfolio
 */
public class FundListActivity extends SingleFragmentActivity{

    @Override
    protected Fragment createFragment(){
        return new FundListFragment();
    }
}
