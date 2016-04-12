package gac.coolteamname.fundnominal;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FundPortfolio {
    private static FundPortfolio sFundPortfolio;

    private Context mContext;
    private List<Fund> mFunds;

    public static FundPortfolio get(Context context){
        if (sFundPortfolio == null){
            sFundPortfolio = new FundPortfolio(context);
        }
        return sFundPortfolio;
    }

    private FundPortfolio(Context context) {
        mFunds = new ArrayList<>();
        for (int i = 0; i<100; i++){
            Fund fund = new Fund("Fund " + i);
            mFunds.add(fund);
        }
        mContext = context.getApplicationContext();
    }

    public void addFund(Fund f) {
        mFunds.add(f);
    }

    public void deleteFund(Fund f) {
        mFunds.remove(f);
    }

    public List<Fund> getFunds(){
        return mFunds;
    }

    public Fund getFund(UUID id) {
        for (Fund fund : mFunds){
            if (fund.getId().equals(id)){
                return fund;
            }
        }
        return null;
    }
}
