package restaurant.model;

import java.util.OptionalDouble;

public class ScoreBoard {
    private int revenueCents, paidCount, satisfactionSum, servedCount, unhappyDepartures;

    public void recordPayment(int cents, int satisfaction)
    { 
        revenueCents += cents;
        paidCount++;
        satisfactionSum += satisfaction;
        
    }
    public void recordServed()
    { 
        servedCount++;
    }
    public void recordAbandonment() 
    {
        unhappyDepartures++;    
    }
    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }
    public OptionalDouble averagePaidSatisfaction()
    { 
        if (paidCount == 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of((double) satisfactionSum / paidCount);

    }
    public GameResult evaluate(GameConfig config)
    {
        if (paidCount >= config.TARGET_PAID
            && revenueCents >= config.TARGET_REVENUE_CENTS
            && averagePaidSatisfaction().orElse(0) >= config.TARGET_AVG_SAT) {
        return GameResult.VICTORY;
    }
    return GameResult.DEFEAT;
    }
}
