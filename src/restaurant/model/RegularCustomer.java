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

    @Override
    public String getTypeName() {
        return "Regular";
    }
}
