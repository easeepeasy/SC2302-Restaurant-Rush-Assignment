package restaurant.control;

// The manager's decision for the Waiter this turn, passed from the GameCLI to the GameController.
// It only carries the decision. Whether the action is allowed is checked later by the Restaurant.
public final class WaiterChoice {

    private final WaiterAction action;
    private final int customerId;
    private final int tableId;
    private final int orderId;

    // IDs not used by the selected action can be set to -1.
    public WaiterChoice(WaiterAction action, int customerId, int tableId, int orderId) {
        this.action = action;
        this.customerId = customerId;
        this.tableId = tableId;
        this.orderId = orderId;
    }

    public WaiterAction getAction() {
        return action;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getTableId() {
        return tableId;
    }

    public int getOrderId() {
        return orderId;
    }
}
