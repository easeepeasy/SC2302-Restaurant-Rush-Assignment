package restaurant.model;

// The Chef completes one preparation unit per turn on one order.
// It only advances units, so it does not need to know which dish it is cooking.
public class Chef extends Staff {

    public Chef(Restaurant restaurant) {
        super("Chef", restaurant);
    }

    // Advances an order by one unit. The order becomes READY on its last unit
    public ActionResult cook(int orderId, int turn) {
        if (hasActed()) {
            return alreadyActed();
        }
        return useTaskIfSuccessful(getRestaurant().prepareOrder(orderId, turn));
    }
}
