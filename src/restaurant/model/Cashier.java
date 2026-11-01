package restaurant.model;

// The Cashier acts automatically (no manager choice), so it does not need to extend from Staff
// Each turn it charges at most one customer, who is the one who has been ready to pay the longest
public class Cashier {

    private static final int NO_TURN = -1;

    private final Restaurant restaurant;
    private int lastTurnHandled = NO_TURN;

    public Cashier(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    // Called once per turn by the GameController in step 4, after eating is updated.
    // A second call in the same turn is refused, so no more than one customer pays per turn.
    public ActionResult collectPayment(int turn) {
        if (turn == lastTurnHandled) {
            return ActionResult.fail("Cashier has already handled payment this turn.");
        }
        lastTurnHandled = turn;
        return restaurant.payOldestEligible(turn);
    }
}
