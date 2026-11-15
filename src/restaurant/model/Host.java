package restaurant.model;

// The Host seats at most one queued customer per turn, in addition to the Waiter's task.
// It reuses Restaurant.seatCustomer(), so seating follows the same rules for both roles.
public class Host extends StaffMember {

    // Seats a queued customer at a free table. The task is used only if seating succeeds.
    public ActionResult seat(int customerId, int tableId, Restaurant restaurant) {
        if (!isAvailable()) {
            return ActionResult.fail("Host has already acted this turn.");
        }

        ActionResult result = restaurant.seatCustomer(customerId, tableId);
        if (result.isSuccess()) {
            consumeTask();
        }
        return result;
    }
}
