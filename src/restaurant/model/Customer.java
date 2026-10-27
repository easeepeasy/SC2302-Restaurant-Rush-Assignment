package restaurant.model;

import java.util.List;

// Base class for all customer types.
// Different customers have different waiting penalties.
public abstract class Customer {

    public static final int MAX_SATISFACTION = 100;

    private final int id;
    private int satisfaction = MAX_SATISFACTION;
    private CustomerState state = CustomerState.WAITING;
    private Order order;

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

    // Each customer type determines how much satisfaction it loses per turn.
    public abstract int waitingPenalty();

    // Customers choose food based on their ID so the results are consistent.
    // The modulo operator cycles through the menu items.
    public MenuItem chooseItem(Menu menu) {
        List<MenuItem> items = menu.getItems();
        return items.get((id - 1) % items.size());
    }

    // Checks if the customer is still waiting for a table or food.
    public boolean isUnserved() {
        return state == CustomerState.WAITING || state == CustomerState.SEATED;
    }

    public void markSeated() {
        if (state != CustomerState.WAITING) {
            throw new IllegalStateException("Customer " + id + " is not waiting");
        }
        state = CustomerState.SEATED;
    }

    public boolean hasOrdered() {
        return order != null;
    }

    // Creates an order using the item chosen by the customer.
    public Order placeOrder(Menu menu) {
        if (state != CustomerState.SEATED || order != null) {
            throw new IllegalStateException("Customer " + id + " cannot order now");
        }
        MenuItem item = chooseItem(menu);
        order = new Order(this, item, item.getPriceCents());
        return order;
    }

    // An order can only be served when it is ready.
    public boolean canBeServed(int turn) {
        return state == CustomerState.SEATED && order != null && order.canServe(turn);
    }

    public void markServed(int turn) {
        if (!canBeServed(turn)) {
            throw new IllegalStateException("Customer " + id + " cannot be served now");
        }
        order.markServed(turn);
        state = CustomerState.EATING;
    }

    // The customer finishes eating one turn after being served.
    public boolean finishEating(int turn) {
        if (state == CustomerState.EATING && order.getServedTurn() < turn) {
            state = CustomerState.READY_TO_PAY;
            order.markReadyToPay(turn);
            return true;
        }
        return false;
    }

    // Payment can only happen after the customer is ready to pay.
    public boolean canPay(int turn) {
        return state == CustomerState.READY_TO_PAY && order.canPay(turn);
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

    public abstract String getTypeName();

    @Override
    public String toString() {
        return getTypeName() + " #" + id + " (satisfaction " + satisfaction + ", " + state + ")";
    }
}
