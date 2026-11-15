package restaurant.model;

// A Critic loses satisfaction quickly and always orders the most expensive item.
public class CriticCustomer extends Customer {

    private static final int WAITING_PENALTY = 12;

    public CriticCustomer(int id) {
        super(id);
    }

    @Override
    public int waitingPenalty() {
        return WAITING_PENALTY;
    }

    @Override
    public MenuItem chooseItem(Menu menu) {
        MenuItem mostExpensive = menu.itemAt(0);

        for (MenuItem item : menu.listItems()) {
            if (item.getPriceCents() > mostExpensive.getPriceCents()) {
                mostExpensive = item;
            }
        }

        return mostExpensive;
    }
}
