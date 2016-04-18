package gac.coolteamname.fundnominal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import gac.coolteamname.fundnominal.database.FundDbSchema.FundTable;

/**
 * Created by Joel Stremmel on 4/11/2016.
 */
public class FundBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "fundBase.db";

    public FundBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + FundTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                FundTable.Cols.UUID + ", " +
                FundTable.Cols.TICKER + ", " +
                FundTable.Cols.WEIGHT + ", " +
                FundTable.Cols.PORTFOLIO + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
