package restaurant.control;

// The manager's decision for the Chef this turn: cook one order, or wait.
// Passed from the GameCLI to the GameController.
public final class ChefChoice {

    private static final int NONE = -1;

    private final int orderId;
    private final boolean waiting;

    // Private so choices can only be made through prepare() and waitTurn().
    private ChefChoice(int orderId, boolean waiting) {
        this.orderId = orderId;
        this.waiting = waiting;
    }

    // Advance this order by one preparation unit.
    public static ChefChoice prepare(int orderId) {
        return new ChefChoice(orderId, false);
    }

    // Do nothing this turn.
    public static ChefChoice waitTurn() {
        return new ChefChoice(NONE, true);
    }

    public boolean isWait() {
        return waiting;
    }

    public int getOrderId() {
        return orderId;
    }
}
