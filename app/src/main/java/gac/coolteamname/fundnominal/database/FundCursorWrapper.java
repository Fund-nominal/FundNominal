package gac.coolteamname.fundnominal.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.UUID;

import gac.coolteamname.fundnominal.Fund;
import gac.coolteamname.fundnominal.database.FundDbSchema.FundTable;


/**
 * Created by Joel Stremmel on 4/11/2016.
 */
public class FundCursorWrapper extends CursorWrapper {
    public FundCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Fund getFund() {
        String uuidString = getString(getColumnIndex(FundTable.Cols.UUID));
        String ticker = getString(getColumnIndex(FundTable.Cols.TICKER));
        int weight = getInt(getColumnIndex(FundTable.Cols.WEIGHT));
        String portfolio = getString(getColumnIndex(FundTable.Cols.PORTFOLIO));
        String company = getString(getColumnIndex(FundTable.Cols.COMPANY));

        Fund fund = new Fund(UUID.fromString(uuidString), ticker, weight, portfolio, null, null, company);
        // correct parameters? Figure out if this is how we want to do this when first populating the database

        return fund;

    }

}
