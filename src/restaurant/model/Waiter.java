package restaurant.model;

// The Waiter does one task per turn: either seat a customer, take an order, or serve a READY dish.
// The GameController lets the Waiter act before the Chef.
public class Waiter extends Staff {

    public Waiter(Restaurant restaurant) {
        super("Waiter", restaurant);
    }

    // Seats a queued customer at a free table
    public ActionResult seat(int customerId, int tableId) {
        if (hasActed()) {
            return alreadyActed();
        }
        return useTaskIfSuccessful(getRestaurant().seatCustomer(customerId, tableId));
    }

    // Takes a seated customer's order. The customer chooses the dish
    public ActionResult takeOrder(int customerId, Menu menu, int turn) {
        if (hasActed()) {
            return alreadyActed();
        }
        return useTaskIfSuccessful(getRestaurant().placeOrder(customerId, menu, turn));
    }

    // Serves an order that was already READY before the current turn
    public ActionResult serve(int orderId, int turn) {
        if (hasActed()) {
            return alreadyActed();
        }
        return useTaskIfSuccessful(getRestaurant().serveOrder(orderId, turn));
    }
}
