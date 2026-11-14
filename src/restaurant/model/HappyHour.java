package restaurant.model;

public class HappyHour {
    private static final int DISCOUNT_PERCENT = 20;
    private static final int RECOVERY_POINTS = 15;
    private static final int DURATION_TURNS = 2;

    private boolean used;
    private int startTurn;

    // Switches Happy Hour on from this turn. Refused if it has already been used.
    public ActionResult activate(int turn) {
        if (used) {
            return ActionResult.fail("Happy Hour has already been used this game.");
        }
        used = true;
        startTurn = turn;
        return ActionResult.ok("Happy Hour is on for turns " + turn + " and " + (turn + 1) + ".");
    }

    public boolean isUsed() {
        return used;
    }

    // Active on the activation turn and the turn after it.
    public boolean isActive(int turn) {
        return used && turn >= startTurn && turn < startTurn + DURATION_TURNS;
    }

    // Active turns left, counting this one: 2 on the activation turn, 1 on the next, then 0.
    public int remainingTurns(int turn) {
        return isActive(turn) ? startTurn + DURATION_TURNS - turn : 0;
    }

    // Applied after the customer's own price (e.g. the VIP discount), rounded to the nearest cent.
    public int discount(int billCents, int turn) {
        if (!isActive(turn)) {
            return billCents;
        }
        return (billCents * (100 - DISCOUNT_PERCENT) + 50) / 100;
    }

    public int getRecoveryPoints() {
        return RECOVERY_POINTS;
    }
    
}
