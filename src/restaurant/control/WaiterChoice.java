package restaurant.control;

// The manager's decision for the Waiter this turn, passed from the GameCLI to the GameController.
// It only carries the decision. Whether the action is allowed is checked later by the Restaurant.
public final class WaiterChoice {

    private static final int NONE = -1;   // for ids an action does not use

    private final WaiterAction action;
    private final int customerId;
    private final int tableId;
    private final int orderId;

    // Private so choices can only be made through the factory methods below,
    // which set exactly the ids each action needs.
    private WaiterChoice(WaiterAction action, int customerId, int tableId, int orderId) {
        this.action = action;
        this.customerId = customerId;
        this.tableId = tableId;
        this.orderId = orderId;
    }

    // Seat a queued customer at a table.
    public static WaiterChoice seat(int customerId, int tableId) {
        return new WaiterChoice(WaiterAction.SEAT, customerId, tableId, NONE);
    }

    // Take a seated customer's order. The customer chooses the dish.
    public static WaiterChoice takeOrder(int customerId) {
        return new WaiterChoice(WaiterAction.TAKE_ORDER, customerId, NONE, NONE);
    }

    // Serve a READY order.
    public static WaiterChoice serve(int orderId) {
        return new WaiterChoice(WaiterAction.SERVE, NONE, NONE, orderId);
    }

    // Do nothing this turn.
    public static WaiterChoice waitTurn() {
        return new WaiterChoice(WaiterAction.WAIT, NONE, NONE, NONE);
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
