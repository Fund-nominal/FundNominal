package gac.coolteamname.fundnominal;

import android.support.v4.app.Fragment;

public class FundListActivity extends SingleFragmentActivity{

    @Override
    protected Fragment createFragment(){
        return new FundListFragment();
    }
}
