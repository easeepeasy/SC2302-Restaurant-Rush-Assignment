package restaurant.model;

// The Chef completes one preparation unit per turn on one order.
// It only advances units, so it does not need to know which dish it is cooking.
public class Chef extends StaffMember {

    // Advances an order by one unit. The order becomes READY on its last unit.
    public ActionResult prepare(int orderId, Restaurant restaurant, int turn) {
        if (!isAvailable()) {
            return ActionResult.fail("Chef has already acted this turn.");
        }

        ActionResult result = restaurant.prepareOrder(orderId, turn);
        if (result.isSuccess()) {
            consumeTask();
        }
        return result;
    }
}
