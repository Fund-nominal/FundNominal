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
        }
    }
}
