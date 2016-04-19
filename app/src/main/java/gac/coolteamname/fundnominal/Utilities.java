package gac.coolteamname.fundnominal;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Joel Stremmel on 4/14/2016.
 */
public class Utilities {

    public static List<String> ExchangeOptions(Fund[] overs, Fund[] unders) {
        Map<Double, String> scoresAndSwaps = new HashMap<>();
        for (int i = 0; i <= overs.length; i++) {
            for (int j = 0; j <= unders.length; j++) {
                scoresAndSwaps.
                put(RateExchangeAtCurrentPrice(overs[i], unders[j]),
                overs[i].getTicker() + " for " + unders[j].getTicker());
            }
        }
        return SortFundPairs(overs, unders, scoresAndSwaps);
    }

    @NonNull
    private static List<String> SortFundPairs(Fund[] overs, Fund[] unders, Map<Double, String> scoresAndSwaps) {
        // takes the dictionary of scores and the swaps associated with them and returns a list of ordered exchanges
        List<String> orderedExchanges = new ArrayList<>(overs.length * unders.length);

        Map<Double,String> orderedScoresAndSwaps = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return o2.compareTo(o1);
            }
        });
        orderedScoresAndSwaps.putAll(scoresAndSwaps);

        for(Map.Entry<Double, String> entry : orderedScoresAndSwaps.entrySet()) {
            orderedExchanges.add(entry.getValue() + " has rating " + entry.getKey().toString() + "/252.");
        }
        return orderedExchanges;
    }

    public static double RateExchangeAtCurrentPrice(Fund over, Fund under) {
        // for two funds, finds the rating of the trade
        int daysOpenInLastYear = over.getPrices().length;
        double[] overPrices = over.getPrices();
        double[] underPrices = under.getPrices();
        double[] comparison = new double[daysOpenInLastYear];
        for (int i = 0; i < daysOpenInLastYear; i++) {
            comparison[i] = (overPrices[i] / underPrices[i]);
        }
        double todaysRatio = comparison[-1];
        Arrays.sort(comparison);
        double rating = (getArrayIndex(comparison, todaysRatio) / comparison.length);
        double scaledRating = rating * 252;
        return scaledRating;
    }

    public static int getArrayIndex(double[] arr, double value) {
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

