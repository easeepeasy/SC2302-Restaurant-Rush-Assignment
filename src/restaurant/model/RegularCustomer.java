package restaurant.model;

// Represents a regular customer who loses 8 satisfaction points per waiting turn.
public class RegularCustomer extends Customer {
    private static final int WAITING_PENALTY = 8;

    public RegularCustomer(int id) {
        super(id);
    }

    @Override
    public int waitingPenalty() {
        return WAITING_PENALTY;
    }

    // Regular customers use the default choice: cycle through the menu by customer ID.
    @Override
    public MenuItem chooseItem(Menu menu) {
        return super.chooseItem(menu);
    }
}
