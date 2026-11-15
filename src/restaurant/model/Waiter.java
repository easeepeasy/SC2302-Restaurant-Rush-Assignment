package restaurant.model;

// The Waiter does one task per turn: either seat a customer, take an order, or serve a READY dish.
// The GameController lets the Waiter act before the Chef.
public class Waiter extends StaffMember {

    // Seats a queued customer at a free table.
    public ActionResult seat(int customerId, int tableId, Restaurant restaurant) {
        if (!isAvailable()) {
            return ActionResult.fail("Waiter has already acted this turn.");
        }
        return completeIfSuccessful(restaurant.seatCustomer(customerId, tableId));
    }

    // Takes a seated customer's order. The customer chooses the dish.
    public ActionResult takeOrder(int customerId, Restaurant restaurant, Menu menu, int turn) {
        if (!isAvailable()) {
            return ActionResult.fail("Waiter has already acted this turn.");
        }
        return completeIfSuccessful(restaurant.placeOrder(customerId, menu, turn));
    }

    // Serves an order that was already READY before the current turn.
    public ActionResult serve(int orderId, Restaurant restaurant, int turn) {
        if (!isAvailable()) {
            return ActionResult.fail("Waiter has already acted this turn.");
        }
        return completeIfSuccessful(restaurant.serveOrder(orderId, turn));
    }

    private ActionResult completeIfSuccessful(ActionResult result) {
        if (result.isSuccess()) {
            consumeTask();
        }
        return result;
    }
}
