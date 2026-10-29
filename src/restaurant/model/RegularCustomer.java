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

    // Chooses food based on the customer ID so the results are consistent.
    // The modulo operator cycles through the menu items.
    @Override
    public MenuItem chooseItem(Menu menu) {
        return menu.itemAt((getId() - 1) % menu.listItems().size());
    }
}
