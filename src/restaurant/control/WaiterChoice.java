
package restaurant.control;

// Represents the manager's choice for the Waiter.
// Factory methods ensure each action has the appropriate IDs.
public final class WaiterChoice {

    private static final int NONE = -1;

    private final WaiterAction action;
    private final int customerId;
    private final int tableId;
    private final int orderId;

    // Choices can only be created through the factory methods.
    private WaiterChoice(WaiterAction action, int customerId,
                         int tableId, int orderId) {
        this.action = action;
        this.customerId = customerId;
        this.tableId = tableId;
        this.orderId = orderId;
    }

    public static WaiterChoice seat(int customerId, int tableId) {
        return new WaiterChoice(WaiterAction.SEAT, customerId, tableId, NONE);
    }

    public static WaiterChoice takeOrder(int customerId) {
        return new WaiterChoice(
                WaiterAction.TAKE_ORDER, customerId, NONE, NONE);
    }

    public static WaiterChoice serve(int orderId) {
        return new WaiterChoice(
                WaiterAction.SERVE, NONE, NONE, orderId);
    }

    public static WaiterChoice waitTurn() {
        return new WaiterChoice(
                WaiterAction.WAIT, NONE, NONE, NONE);
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

