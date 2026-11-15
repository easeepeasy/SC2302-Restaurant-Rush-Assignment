package restaurant.control;

// The manager's decision for the Host this turn: seat one queued customer, or wait.
// Passed from the GameCLI to the GameController.
public final class HostChoice {

    private static final int NONE = -1;

    private final int customerId;
    private final int tableId;
    private final boolean waiting;

    // Private so choices can only be made through seat() and waitTurn().
    private HostChoice(int customerId, int tableId, boolean waiting) {
        this.customerId = customerId;
        this.tableId = tableId;
        this.waiting = waiting;
    }

    // Seat this queued customer at this table.
    public static HostChoice seat(int customerId, int tableId) {
        return new HostChoice(customerId, tableId, false);
    }

    // Do nothing this turn.
    public static HostChoice waitTurn() {
        return new HostChoice(NONE, NONE, true);
    }

    public boolean isWait() {
        return waiting;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getTableId() {
        return tableId;
    }
}
