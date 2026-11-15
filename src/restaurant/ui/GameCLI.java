
package restaurant.ui;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Scanner;

import restaurant.control.ChefChoice;
import restaurant.control.HostChoice;
import restaurant.control.WaiterChoice;
import restaurant.control.WaiterAction;
import restaurant.model.Customer;
import restaurant.model.GameResult;
import restaurant.model.HappyHour;
import restaurant.model.Order;
import restaurant.model.Restaurant;
import restaurant.model.ScoreBoard;
import restaurant.model.StaffMember;
import restaurant.model.Table;

// Only handles input and output. Game rules and validation remain in the model/controller.
public class GameCLI {

    private final Scanner scanner = new Scanner(System.in);

    // Called after the scheduled arrival for this turn has been added.
    public void showState(Restaurant restaurant, int turn, List<StaffMember> staff) {
        ScoreBoard score = restaurant.getScoreBoard();
        System.out.println();
        System.out.println("========================================");
        System.out.println("           RESTAURANT RUSH");
        System.out.println("               TURN " + turn);
        System.out.println("========================================");
        System.out.println("Revenue: " + formatMoney(score.getRevenueCents()));
        System.out.println("Paid customers: " + score.getPaidCount());
        System.out.println("Average paid satisfaction: " + formatAverage(score.averagePaidSatisfaction()));
        System.out.println("Happy Hour: " + describeHappyHour(restaurant.getHappyHour(), turn));

        System.out.println("\nWaiting queue (arrival order):");
        List<Customer> waiting = restaurant.getWaitingQueue();
        if (waiting.isEmpty()) {
            System.out.println("  (empty)");
        } else {
            for (Customer customer : waiting) {
                System.out.println("  " + describeCustomer(customer));
            }
        }

        System.out.println("\nTables:");
        for (Table table : restaurant.getTables()) {
            if (table.isFree()) {
                System.out.println("  Table " + table.getId() + ": free");
            } else {
                System.out.println("  Table " + table.getId() + ": " + describeCustomer(table.getOccupant()));
            }
        }

        System.out.println("\nActive orders:");
        List<Order> orders = restaurant.getActiveOrders();
        if (orders.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (Order order : orders) {
                System.out.println("  Order #" + order.getId() + " (Customer #"
                        + order.getCustomer().getId() + "): " + order);
            }
        }

        System.out.println("\nStaff tasks available:");
        System.out.println("  Waiter: " + availability(staff, 0)
                + " - seat / take order / serve / wait");
        System.out.println("  Chef: " + availability(staff, 1) + " - prepare / wait");
        System.out.println("  Cashier: " + availability(staff, 2)
                + " - automatically collects up to one eligible payment");
        System.out.println("  Host: " + availability(staff, 3) + " - seat / wait");
    }

    public HostChoice readHostChoice() {
        System.out.println("\nHost's action (seats a customer in addition to the Waiter):");
        System.out.println("  1. Seat a waiting customer");
        System.out.println("  2. Wait");

        while (true) {
            int action = readInt("Choose 1-2: ");
            switch (action) {
                case 1:
                    int customerId = readInt("Customer ID: ");
                    int tableId = readInt("Table ID: ");
                    return HostChoice.seat(customerId, tableId);
                case 2:
                    return HostChoice.waitTurn();
                default:
                    showError("Choose an option from 1 to 2.");
            }
        }
    }

    public WaiterChoice readWaiterChoice() {
        System.out.println("\nWaiter's action:");
        System.out.println("  1. Seat a waiting customer");
        System.out.println("  2. Take a seated customer's order");
        System.out.println("  3. Serve a READY order");
        System.out.println("  4. Wait");

        while (true) {
            int action = readInt("Choose 1-4: ");
            switch (action) {
                case 1:
                    int customerId = readInt("Customer ID: ");
                    int tableId = readInt("Table ID: ");
                    return new WaiterChoice(WaiterAction.SEAT, customerId, tableId, -1);
                case 2:
                    return new WaiterChoice(WaiterAction.TAKE_ORDER, readInt("Customer ID: "), -1, -1);
                case 3:
                    return new WaiterChoice(WaiterAction.SERVE, -1, -1, readInt("Order ID: "));
                case 4:
                    return new WaiterChoice(WaiterAction.WAIT, -1, -1, -1);
                default:
                    showError("Choose an option from 1 to 4.");
            }
        }
    }

    public ChefChoice readChefChoice() {
        System.out.println("\nChef's action:");
        System.out.println("  1. Prepare an order (one unit)");
        System.out.println("  2. Wait");

        while (true) {
            int action = readInt("Choose 1-2: ");
            switch (action) {
                case 1:
                    return ChefChoice.prepare(readInt("Order ID: "));
                case 2:
                    return ChefChoice.waitTurn();
                default:
                    showError("Choose an option from 1 to 2.");
            }
        }
    }

    // Asked at the start of a turn while Happy Hour has not been used yet.
    public boolean askHappyHour() {
        System.out.println("\nActivate Happy Hour now? It can be used once and lasts this turn and the next.");
        System.out.println("  1. Yes");
        System.out.println("  2. No");

        while (true) {
            int answer = readInt("Choose 1-2: ");
            if (answer == 1) {
                return true;
            }
            if (answer == 2) {
                return false;
            }
            showError("Choose an option from 1 to 2.");
        }
    }

    public void showError(String message) {
        System.out.println("Invalid action: " + message);
        System.out.println("Please try again. The staff task has not been used.");
    }

    public void showResult(GameResult result, ScoreBoard score, int turnsUsed) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("            FINAL RESULT");
        System.out.println("========================================");
        System.out.println(result == GameResult.VICTORY ? "VICTORY!" : "DEFEAT!");
        System.out.println("Served customers: " + score.getServedCount());
        System.out.println("Paid customers: " + score.getPaidCount());
        System.out.println("Unhappy departures: " + score.getUnhappyDepartures());
        System.out.println("Revenue: " + formatMoney(score.getRevenueCents()));
        System.out.println("Average paid satisfaction: " + formatAverage(score.averagePaidSatisfaction()));
        System.out.println("Turns used: " + turnsUsed);
        System.out.println("========================================");
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                throw new IllegalStateException("Input ended before the game was complete.");
            }
            String value = scanner.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private String describeCustomer(Customer customer) {
        Order order = customer.getOrder();
        String orderText = order == null ? "no order" : "order #" + order.getId()
                + " " + order.getItem().getName() + " [" + order.getStatus() + "]";
        return "Customer #" + customer.getId() + " (" + customer.getClass().getSimpleName()
                + ", satisfaction " + customer.getSatisfaction() + ", "
                + customer.getState() + ", " + orderText + ")";
    }

    private String availability(List<StaffMember> staff, int index) {
        if (index >= staff.size()) {
            return "unknown";
        }
        return staff.get(index).isAvailable() ? "available" : "busy";
    }

    private String describeHappyHour(HappyHour happyHour, int turn) {
        if (happyHour.isActive(turn)) {
            int left = happyHour.remainingTurns(turn);
            return "ACTIVE (" + left + " turn" + (left == 1 ? "" : "s") + " left, including this one)";
        }
        return happyHour.isUsed() ? "used" : "available";
    }

    private String formatMoney(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    private String formatAverage(OptionalDouble average) {
        return average.isPresent() ? String.format("%.1f", average.getAsDouble()) : "N/A";
    }
}

