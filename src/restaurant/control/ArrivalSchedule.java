package restaurant.control;

import java.util.Optional;
import java.util.Set;

import restaurant.model.Customer;
import restaurant.model.RegularCustomer;

// Decides when customers arrive and creates them with ids 1, 2, 3, ...
// Ids start at 1 because Customer.chooseItem uses the id to pick the dish.
public class ArrivalSchedule {

    private static final int FIRST_ID = 1;

    private final Set<Integer> arrivalTurns;
    private int nextCustomerId = FIRST_ID;

    public ArrivalSchedule(Set<Integer> arrivalTurns) {
        this.arrivalTurns = Set.copyOf(arrivalTurns);
    }

    // The customer who arrives at the start of this turn, or empty if nobody arrives.
    public Optional<Customer> customerAt(int turn) {
        if (!arrivalTurns.contains(turn)) {
            return Optional.empty();
        }
        return Optional.of(new RegularCustomer(nextCustomerId++));
    }

    // Starts the ids from 1 again, for a new game.
    public void reset() {
        nextCustomerId = FIRST_ID;
    }
}
