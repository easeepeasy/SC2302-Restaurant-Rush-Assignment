package restaurant.model;

import java.util.Set;

public class GameConfig {
    public final int MAX_TURNS;
    public final int TABLE_COUNT;
    public final int TARGET_PAID;
    public final int TARGET_REVENUE_CENTS;
    public final int TARGET_AVG_SAT;
    public final Set<Integer> ARRIVAL_TURNS;

    private GameConfig(int maxTurns, int tableCount, int targetPaid, int targetRevenueCents,
                       int targetAvgSat, Set<Integer> arrivalTurns) {
        MAX_TURNS = maxTurns;
        TABLE_COUNT = tableCount;
        TARGET_PAID = targetPaid;
        TARGET_REVENUE_CENTS = targetRevenueCents;
        TARGET_AVG_SAT = targetAvgSat;
        ARRIVAL_TURNS = Set.copyOf(arrivalTurns);
    }

    // 18 turns, 2 tables, a customer on turns 1, 4, 7, 10, 13 and 16 according to the base setting
    // Win: at least 4 paid, at least $45.00 revenue, average satisfaction at least 60.
    public static GameConfig base() {
        return new GameConfig(18, 2, 4, 4500, 60, Set.of(1, 4, 7, 10, 13, 16));
    }
    
}
