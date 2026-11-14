package restaurant.model;

// Base class for all customer types.
// Different customers have different waiting penalties and food choices.
public abstract class Customer {

    public static final int MAX_SATISFACTION = 100;

    private final int id;
    private int satisfaction = MAX_SATISFACTION;
    private CustomerState state = CustomerState.WAITING;
    private Order order;
    private Table table;
    private int readyToPayTurn;

    protected Customer(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerState getState() {
        return state;
    }

    public Order getOrder() {
        return order;
    }

    // Used by the Cashier to pay the customer who has been ready to pay the longest.
    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    // Each customer type determines how much satisfaction it loses per turn.
    public abstract int waitingPenalty();

    // Customers choose food based on their ID so the results are consistent.
    // The modulo operator cycles through the menu items. Subclasses can override this.
    public MenuItem chooseItem(Menu menu) {
        return menu.itemAt((id - 1) % menu.listItems().size());
    }

    // Returns the bill for an item. Subclasses can override this to apply discounts.
    public int billFor(MenuItem item) {
        return item.getPriceCents();
    }

    // Moves the customer from the waiting queue to a table.
    public void seatAt(Table table) {
        if (state != CustomerState.WAITING) {
            throw new IllegalStateException("Customer " + id + " is not waiting");
        }
        this.table = table;
        state = CustomerState.SEATED;
    }

    // Links the order that was taken for this customer.
    public void attachOrder(Order order) {
        if (state != CustomerState.SEATED || this.order != null) {
            throw new IllegalStateException("Customer " + id + " cannot order now");
        }
        this.order = order;
    }

    // Called on the turn the order is served. The customer finishes eating on the next turn.
    public void startEating(int turn) {
        if (state != CustomerState.SEATED || order == null) {
            throw new IllegalStateException("Customer " + id + " cannot start eating now");
        }
        state = CustomerState.EATING;
        readyToPayTurn = turn + 1;
    }

    // The customer finishes eating one turn after being served.
    public void finishEating(int turn) {
        if (state == CustomerState.EATING && turn >= readyToPayTurn) {
            state = CustomerState.READY_TO_PAY;
        }
    }

    // Payment can only happen on a later turn after the customer is ready to pay.
    public boolean canPay(int turn) {
        return state == CustomerState.READY_TO_PAY && turn > readyToPayTurn;
    }

    public void markPaid() {
        if (state != CustomerState.READY_TO_PAY) {
            throw new IllegalStateException("Customer " + id + " is not ready to pay");
        }
        order.markPaid();
        state = CustomerState.PAID;
    }

    // Reduces satisfaction but does not allow it to go below 0.
    public void loseSatisfaction(int points) {
        satisfaction = Math.max(0, satisfaction - points);
    }

    // Raises satisfaction but does not allow it to go above 100.
    public void recoverSatisfaction(int points) {
        satisfaction = Math.min(MAX_SATISFACTION, satisfaction + points);
    }

    public boolean isSatisfied() {
        return satisfaction > 0;
    }

    // Customer leaves without paying, and their order is cancelled.
    public void leave() {
        if (state == CustomerState.PAID || state == CustomerState.LEFT) {
            throw new IllegalStateException("Customer " + id + " has already finished");
        }
        if (order != null) {
            order.cancel();
        }
        state = CustomerState.LEFT;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " #" + id + " (satisfaction " + satisfaction + ", " + state + ")";
    }
}
