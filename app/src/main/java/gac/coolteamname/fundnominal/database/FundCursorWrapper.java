package gac.coolteamname.fundnominal.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
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
        String dateLastChecked = getString(getColumnIndex(FundTable.Cols.DATE));
        String pricesList = getString(getColumnIndex(FundTable.Cols.PRICES));
        String price = getString(getColumnIndex(FundTable.Cols.PRICE));
        String datePriceChecked = getString(getColumnIndex(FundTable.Cols.DATEUPDATE));
        String company = getString(getColumnIndex(FundTable.Cols.COMPANY));

        Fund fund = new Fund(UUID.fromString(uuidString), ticker, weight, portfolio,
                fromStringToDate(dateLastChecked), fromStringToList(pricesList),
                fromStringToPrice(price), fromStringToDate(datePriceChecked), company);
        // correct parameters? Figure out if this is how we want to do this when first populating the database

        return fund;

    }

    private static List<BigDecimal> fromStringToList(String string) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<BigDecimal>>() {}.getType();
        List<BigDecimal> prices = gson.fromJson(string, type);
        return prices;
    }
    private static Date fromStringToDate(String string) {
        Gson gson = new Gson();
        Type type = new TypeToken<Date>() {}.getType();
        Date date = gson.fromJson(string, type);
        return date;
    }
    private static BigDecimal fromStringToPrice(String string) {
        Gson gson = new Gson();
        Type type = new TypeToken<BigDecimal>() {}.getType();
        BigDecimal price = gson.fromJson(string, type);
        return price;
    }

}
