package restaurant.model;

// Represents a customer's order and tracks its preparation status.
// The bill is fixed when the order is placed.

public class Order {

    private static final int NOT_SET = -1;

    private final int id;
    private final Customer customer;
    private final MenuItem item;
    private OrderStatus status = OrderStatus.PLACED;
    private final int billCents;
    private int unitsDone = 0;
    private final int placedTurn;
    private int readyTurn = NOT_SET;
    private int servedTurn = NOT_SET;

    public Order(int id, Customer customer, MenuItem item, int billCents, int placedTurn) {
        this.id = id;
        this.customer = customer;
        this.item = item;
        this.billCents = billCents;
        this.placedTurn = placedTurn;
    }

    public int getId() {
        return id;
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

    public int getUnitsDone() {
        return unitsDone;
    }

    public int getBillCents() {
        return billCents;
    }

    // Chef completes one preparation unit at a time.
    // Once all units are done, the order becomes READY.
    public void advanceOneUnit(int turn) {
        if (status != OrderStatus.PLACED && status != OrderStatus.PREPARING) {
            throw new IllegalStateException("Order " + id + " cannot be prepared while " + status);
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
            throw new IllegalStateException("Order " + id + " cannot be served now");
        }
        status = OrderStatus.SERVED;
        servedTurn = turn;
    }

    public void markPaid() {
        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException("Order " + id + " cannot be paid now");
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
