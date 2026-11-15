package restaurant.model;

// The Cashier automatically charges the customer who has been ready to pay the longest.
public class Cashier extends StaffMember {

    public ActionResult collectOldest(Restaurant restaurant, int turn) {
        if (!isAvailable()) {
            return ActionResult.fail("Cashier has already handled payment this turn.");
        }

        ActionResult result = restaurant.payOldestEligible(turn);
        if (result.isSuccess()) {
            consumeTask();
        }
        return result;
    }
}
