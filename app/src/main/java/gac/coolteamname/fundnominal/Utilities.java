package gac.coolteamname.fundnominal;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Joel Stremmel on 4/14/2016.
 * These functions can be called everywhere by anything
 * For now it just stores the algorithm
 */
public class Utilities {

    /**
     * Takes in a list of Overweight funds and a list of Underweight funds, return the scores for
     * all those exchanges
     * @param overs the list of overweight funds
     * @param unders the list of underweight funds
     * @return a list of string describing the score of each exchange
     */
    public static List<String[]> ExchangeOptions(List<Fund> overs, List<Fund> unders) {
        List<String[]> result = new ArrayList<>();
        for (Fund over : overs) {
            for (Fund under : unders) {
                result.add(new String[]{
                        over.getTicker(), under.getTicker(),
                        String.format("%04.2f",
                                (double) Math.round(RateExchangeAtCurrentPrice(over, under)) / 100)
                });
            }
        }
        Collections.sort(result, new Comparator<String[]>() {
            @Override
            public int compare(String[] lhs, String[] rhs) {
                double x = Double.parseDouble(lhs[2]);
                double y = Double.parseDouble(rhs[2]);
                return Double.compare(y, x);
            }
        });
        return result;
    }

    /**
     * Find the rating score of the exchange between 2 particular Fund
     * @param over the overweight fund
     * @param under the underweight fund
     * @return the score of the exchange
     */
    public static double RateExchangeAtCurrentPrice(Fund over, Fund under) {
        List<BigDecimal> overPrices = over.getPrices();
        List<BigDecimal> underPrices = under.getPrices();
        if (over.getPrices() != null && under.getPrices() != null) {
            int daysOpenInLastYear = Math.min(overPrices.size(), underPrices.size());
            int score = 1;
            BigDecimal todaysRatio = (overPrices.get(0).divide(underPrices.get(0), 4, BigDecimal.ROUND_CEILING));;
            for (int i = 0; i < daysOpenInLastYear; i++) {
                BigDecimal thisRatio = (overPrices.get(i).divide(underPrices.get(i), 4, BigDecimal.ROUND_CEILING));
                if (todaysRatio.compareTo(thisRatio) == 1) {
                    score++;
                }
            }
            double scaledRating = (double)(score) / daysOpenInLastYear * 1000;
            return scaledRating;
        } else {
            return 0.0;
        }
    }

    /**
     * Extract the two tickers from a string with the format "TICKER1 for TICKER2"
     * @param exchangeName the input string of the exchange
     * @return a list of string - the two extracted tickers
     */
    public static String[] splitTickers(String exchangeName){
        String[] a = exchangeName.split(" ");
        String[] b = new String[2];
        b[0] = a[0];
        b[1] = a[2];
        return b;
    }

    public static List<Fund> sortFunds(List<Fund> funds) {
        List<Fund> sortedFunds = new ArrayList<>();

        for (int weight : new int[]{1, -1, 0}) {
            for (Fund fund : funds) {
                if (fund.getWeight() == weight) { sortedFunds.add(fund); }
            }
        }

        return sortedFunds;
    }
}

