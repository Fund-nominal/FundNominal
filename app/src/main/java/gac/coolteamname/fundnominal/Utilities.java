package gac.coolteamname.fundnominal;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        Map<String, Double> scoresAndSwaps = new HashMap<>();
        for (Fund over : overs) {
            for (Fund under : unders) {
                scoresAndSwaps.
                put(over.getTicker() + " for " + under.getTicker(),
                    RateExchangeAtCurrentPrice(over, under));
            }
        }
        return SortFundPairs(overs, unders, scoresAndSwaps);
    }

    /**
     * Takes in a dictionary of all possible exchanges and their scores
     * and returns a sorted list of exchanges, from highest score to lowest score
     * @param overs the list of overweight funds
     * @param unders the list of underweight funds
     * @param scoresAndSwaps the dictionary of all the possible exchanges and their scores
     * @return the sorted list of exchanges
     */
    @NonNull
    private static List<String[]> SortFundPairs(List<Fund> overs, List<Fund> unders,
                                                Map<String, Double> scoresAndSwaps) {
        List<String[]> orderedExchanges = new ArrayList<>();

        Map<String, Double> orderedScoresAndSwaps = new HashMap<>();
        orderedScoresAndSwaps = MapCompare.sortByValue(scoresAndSwaps);

        for(Map.Entry<String, Double> entry : orderedScoresAndSwaps.entrySet()) {
            String[] orderedString = new String[2];
            orderedString[0] = entry.getKey();
            orderedString[1] = Double.toString((double) Math.round(entry.getValue()) / 100);
            orderedExchanges.add(orderedString);
        }
        return orderedExchanges;
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
    }

    public static String[] splitTickers(String exchangeName){
        String[] a = exchangeName.split(" ");
        String[] b = new String[2];
        b[0] = a[0];
        b[1] = a[2];
        return b;
    }
}

