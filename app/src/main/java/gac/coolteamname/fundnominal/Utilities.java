package gac.coolteamname.fundnominal;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    public static List<String> ExchangeOptions(List<Fund> overs, List<Fund> unders) {
        Map<Double, String> scoresAndSwaps = new HashMap<>();
        for (Fund over : overs) {
            for (Fund under : unders) {
                scoresAndSwaps.
                put(RateExchangeAtCurrentPrice(over, under),
                over.getTicker() + " for " + under.getTicker());
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
    private static List<String> SortFundPairs(List<Fund> overs, List<Fund> unders, Map<Double, String> scoresAndSwaps) {
        List<String> orderedExchanges = new ArrayList<>(overs.size() * unders.size());

        Map<Double,String> orderedScoresAndSwaps = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return o2.compareTo(o1);
            }
        });
        orderedScoresAndSwaps.putAll(scoresAndSwaps);

        for(Map.Entry<Double, String> entry : orderedScoresAndSwaps.entrySet()) {
            orderedExchanges.add(entry.getValue() + " has rating " +
                    (double)Math.round(entry.getKey())/100);
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
        // TODO: function currently not working
        List<BigDecimal> overPrices = over.getPrices();
        List<BigDecimal> underPrices = under.getPrices();
        int daysOpenInLastYear = Math.min(overPrices.size(), underPrices.size());
        BigDecimal[] comparison = new BigDecimal[daysOpenInLastYear];
        int score = 1;
        BigDecimal todaysRatio = (overPrices.get(0).divide(underPrices.get(0), 4, BigDecimal.ROUND_CEILING));;
        for (int i = 0; i < daysOpenInLastYear; i++) {
            BigDecimal thisRatio = (overPrices.get(i).divide(underPrices.get(i), 4, BigDecimal.ROUND_CEILING));
            if (todaysRatio.compareTo(thisRatio) == 1) {
                score++;
            }
        }
        //Arrays.sort(comparison);
        // TODO: Duy has an idea to optimize this. Instead of go through the loop AND THEN sort, we can go through the loop only once.
        //double rating = ((double)getArrayIndex(comparison, todaysRatio) / comparison.length);
        double scaledRating = (double)(score) / daysOpenInLastYear * 1000;
        return scaledRating;
    }

    public static int getArrayIndex(BigDecimal[] arr, BigDecimal value) {
        int k=0;
        for(int i=0;i<arr.length;i++){

            if(arr[i]==value){
                k=i;
                break;
            }
        }
        return k;
    }
}

