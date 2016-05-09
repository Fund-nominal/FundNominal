package gac.coolteamname.fundnominal.database;

/**
 * Created by Joel Stremmel on 4/11/2016.
 */
public class FundDbSchema {
    public static final class FundTable {
        public static final String NAME = "funds";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TICKER = "ticker";
            public static final String WEIGHT = "weight";
            public static final String PORTFOLIO = "portfolio";
            public static final String DATE = "date";
            public static final String PRICES = "prices";
            public static final String DATEUPDATE = "dateupdate";
            public static final String PRICE = "price";
            public static final String COMPANY = "company";
        }
    }
}
