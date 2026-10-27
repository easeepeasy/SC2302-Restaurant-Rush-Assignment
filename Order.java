// Represents a customer's order and tracks its preparation and payment status.
// The bill is fixed when the order is placed.

public class Order {

    private static final int NOT_SET = -1;

    private final Customer customer;
    private final MenuItem item;
    private OrderStatus status = OrderStatus.PLACED;
    private final int billCents;
    private int unitsDone = 0;
    private int readyTurn = NOT_SET;
    private int servedTurn = NOT_SET;
    private int readyToPayTurn = NOT_SET;

    public Order(Customer customer, MenuItem item, int billCents) {
        this.customer = customer;
        this.item = item;
        this.billCents = billCents;
    }

    // Each customer has one order, so the order ID matches the customer ID.
    public int getId() {
        return customer.getId();
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getBillCents() {
        return billCents;
    }

    public int getUnitsDone() {
        return unitsDone;
    }

    public int getServedTurn() {
        return servedTurn;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    // Checks if the order still needs preparation.
    public boolean canAdvance() {
        return status == OrderStatus.PLACED || status == OrderStatus.PREPARING;
    }

    // Chef completes one preparation unit at a time.
    // Once all units are done, the order becomes READY.
    public void advance(int turn) {
        if (!canAdvance()) {
            throw new IllegalStateException("Order " + getId() + " cannot be prepared while " + status);
        }
        unitsDone++;
        if (unitsDone >= item.getPrepUnits()) {
            status = OrderStatus.READY;
            readyTurn = turn;
        } else {
            status = OrderStatus.PREPARING;
        }
    }

    // An order can only be served on a later turn after becoming READY.
    public boolean canServe(int turn) {
        return status == OrderStatus.READY && readyTurn < turn;
    }

    public void markServed(int turn) {
        if (!canServe(turn)) {
            throw new IllegalStateException("Order " + getId() + " cannot be served now");
        }
        status = OrderStatus.SERVED;
        servedTurn = turn;
    }

    // Records the turn when the customer finishes eating.
    void markReadyToPay(int turn) {
        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException("Order " + getId() + " has not been served");
        }
        readyToPayTurn = turn;
    }

    // Payment can only happen on a later turn after the customer finishes eating.
    public boolean canPay(int turn) {
        return status == OrderStatus.SERVED && readyToPayTurn != NOT_SET && readyToPayTurn < turn;
    }

    public void markPaid() {
        if (status != OrderStatus.SERVED || readyToPayTurn == NOT_SET) {
            throw new IllegalStateException("Order " + getId() + " cannot be paid now");
        }
        status = OrderStatus.PAID;
    }

    // Cancels the order if the customer leaves without paying.
    public void cancel() {
        if (status != OrderStatus.PAID) {
            status = OrderStatus.CANCELLED;
        }
    }

    // Displays the order status and preparation progress if it is not ready yet.
    @Override
    public String toString() {
        return item.getName() + " [" + status + (status == OrderStatus.PREPARING || status == OrderStatus.PLACED
                ? ", " + unitsDone + "/" + item.getPrepUnits() : "") + "]";
    }
}
