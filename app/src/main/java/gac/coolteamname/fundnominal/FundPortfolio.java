package gac.coolteamname.fundnominal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gac.coolteamname.fundnominal.database.FundCursorWrapper;
import gac.coolteamname.fundnominal.database.FundBaseHelper;
import gac.coolteamname.fundnominal.database.FundDbSchema.FundTable;

/**
 * This is the class that manages all the ins and outs of the Funds
 */
public class FundPortfolio {
    private static FundPortfolio sFundPortfolio;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    /**
     * Get the existing instance of FundPortfolio.
     * If none exist, create a new one.
     */
    public static FundPortfolio get(Context context) {
        if (sFundPortfolio == null) {
            sFundPortfolio = new FundPortfolio(context);
        }
        return sFundPortfolio;
    }

    private FundPortfolio(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new FundBaseHelper(mContext).getWritableDatabase();
    }

    /**
     * Add a new fund to the database
     * @param f the fund to add
     */
    public void addFund(Fund f) {
        ContentValues values = getContentValues(f);

        mDatabase.insert(FundTable.NAME, null, values);
    }

    /**
     * Delete a fund from the database
     * @param fund the fund to delete
     */
    public void deleteFund(Fund fund) {
        mDatabase.delete(FundTable.NAME, FundTable.Cols.UUID + " = ?", new String[]{fund.getId().toString()});
    }

    /**
     * Get a list of all the Funds in the database
     * @return the list of funds
     */
    public List<Fund> getFunds() {
        List<Fund> funds = new ArrayList<>();

        FundCursorWrapper cursor = queryFunds(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                funds.add(cursor.getFund());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return funds;
    }

    /**
     * Get a list of all the OVERWEIGHT funds in the database
     * @return the list of overweight funds
     */
    public List<Fund> getOvers() {
        List<Fund> oversList = new ArrayList<>();

        for (int i = 0; i < getFunds().size(); i++) {
            if (getFunds().get(i).getWeight() == 1) {
                oversList.add(getFunds().get(i));
            }
        }
        return oversList;
    }

    /**
     * Get a list of all the UNDERWEIGHT funds in the database
     * @return the list of underweight funds
     */
    public List<Fund> getUnders() {
        List<Fund> undersList = new ArrayList<>();

        for (int i = 0; i < getFunds().size(); i++) {
            if (getFunds().get(i).getWeight() == -1) {
                undersList.add(getFunds().get(i));
            }
        }
        return undersList;
    }

    /**
     * Get one particular fund
     * @param id the id of the fund to get
     * @return the desired fund
     */
    public Fund getFund(UUID id) {
        FundCursorWrapper cursor = queryFunds(
                FundTable.Cols.UUID + " =?",
                new String[] { id.toString() }
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getFund();
        } finally {
            cursor.close();
        }
    }

    /**
     * Update a particular fund
     * @param fund the fund to update
     */
    public void updateFund(Fund fund) {
        String uuidString = fund.getId().toString();
        ContentValues values = getContentValues(fund);

        mDatabase.update(FundTable.NAME, values,
                FundTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private static ContentValues getContentValues(Fund fund) {
        ContentValues values = new ContentValues();
        values.put(FundTable.Cols.UUID, fund.getId().toString());
        values.put(FundTable.Cols.TICKER, fund.getTicker());
        values.put(FundTable.Cols.WEIGHT, fund.getWeight());
        values.put(FundTable.Cols.PORTFOLIO, fund.getPortfolioName());
        values.put(FundTable.Cols.DATE, fromTypeToString(fund.getTimeLastChecked()));
        values.put(FundTable.Cols.PRICES, fromTypeToString(fund.getPrices()));
        values.put(FundTable.Cols.PRICE, fromTypeToString(fund.getPrice()));
        values.put(FundTable.Cols.DATEUPDATE, fromTypeToString(fund.getTimePriceChecked()));

        return values;
    }

    private FundCursorWrapper queryFunds(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                FundTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, //groupBy
                null, // having
                null // orderBy
        );

        return new FundCursorWrapper(cursor);
    }

    private static String fromTypeToString(List<BigDecimal> prices) {
        Gson gson = new Gson();
        String stringBuild = gson.toJson(prices);
        return stringBuild;
    }
    private static String fromTypeToString(Date date) {
        Gson gson = new Gson();
        String stringBuild = gson.toJson(date);
        return stringBuild;
    }
    private static String fromTypeToString(BigDecimal price) {
        Gson gson = new Gson();
        String stringBuild = gson.toJson(price);
        return stringBuild;
    }

}
