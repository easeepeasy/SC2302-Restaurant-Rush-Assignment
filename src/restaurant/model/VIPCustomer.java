package restaurant.model;

// A VIP loses less satisfaction while waiting and receives 10% off each order.
public class VIPCustomer extends Customer {

    private static final int WAITING_PENALTY = 5;
    private static final int DISCOUNT_PERCENT = 10;

    public VIPCustomer(int id) {
        super(id);
    }

    @Override
    public int waitingPenalty() {
        return WAITING_PENALTY;
    }

    @Override
    public int billFor(MenuItem item) {
        return item.getPriceCents() * (100 - DISCOUNT_PERCENT) / 100;
    }
}
