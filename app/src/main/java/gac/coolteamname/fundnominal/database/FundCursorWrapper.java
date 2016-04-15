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

        Fund fund = new Fund(UUID.fromString(uuidString), ticker, 0);
        // correct parameters? Figure out if this is how we want to do this when first populating the database

        return fund;

    }

}
