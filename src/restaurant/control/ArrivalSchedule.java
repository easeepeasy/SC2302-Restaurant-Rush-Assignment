package restaurant.control;

import java.util.Map;
import java.util.Optional;

import restaurant.model.CriticCustomer;
import restaurant.model.Customer;
import restaurant.model.CustomerType;
import restaurant.model.RegularCustomer;
import restaurant.model.VIPCustomer;

// Decides when customers arrive and creates them with ids 1, 2, 3, ...
// Ids start at 1 because Customer.chooseItem uses the id to pick the dish.
public class ArrivalSchedule {

    private static final int FIRST_ID = 1;

    private final Map<Integer, CustomerType> arrivalTypes;
    private int nextCustomerId = FIRST_ID;

    public ArrivalSchedule(Map<Integer, CustomerType> arrivalTypes) {
        this.arrivalTypes = Map.copyOf(arrivalTypes);
    }

    // The customer who arrives at the start of this turn, or empty if nobody arrives.
    public Optional<Customer> customerAt(int turn) {
        CustomerType type = arrivalTypes.get(turn);
        if (type == null) {
            return Optional.empty();
        }

        int customerId = nextCustomerId++;
        switch (type) {
            case VIP:
                return Optional.of(new VIPCustomer(customerId));
            case CRITIC:
                return Optional.of(new CriticCustomer(customerId));
            default:
                return Optional.of(new RegularCustomer(customerId));
        }
    }

    // Starts the ids from 1 again, for a new game.
    public void reset() {
        nextCustomerId = FIRST_ID;
    }
}
