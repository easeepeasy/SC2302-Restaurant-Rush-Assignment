
package restaurant.ui;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.StringJoiner;

import restaurant.model.CustomerState;
import restaurant.model.OrderStatus;
import restaurant.control.ChefChoice;
import restaurant.control.WaiterChoice;
import restaurant.model.Customer;
import restaurant.model.GameResult;
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
    }

    
    public WaiterChoice readWaiterChoice(Restaurant restaurant, int turn) {
        while (true) {
            // Refresh eligibility whenever the menu is displayed.
            List<Customer> waiting = restaurant.getWaitingQueue();
            List<Table> freeTables = getFreeTables(restaurant);
            List<Customer> orderable = getOrderableCustomers(restaurant);
            List<Order> servable = getServableOrders(restaurant, turn);

            System.out.println("\nWaiter's action:");
            System.out.println("  1. Seat a waiting customer");
            System.out.println("     Waiting: " + customerIds(waiting));
            System.out.println("     Free tables: " + tableIds(freeTables));
            System.out.println("  2. Take a seated customer's order");
            System.out.println("     Eligible customers: " + customerIds(orderable));
            System.out.println("  3. Serve a READY order");
            System.out.println("     Eligible orders: " + orderDetails(servable));
            System.out.println("  4. Wait");

            int action = readInt("Choose 1-4: ");

            switch (action) {
                case 1:
                    if (waiting.isEmpty()) {
                        showError("No customers are waiting to be seated.");
                        continue;
                    }
                    if (freeTables.isEmpty()) {
                        showError("No free tables are available.");
                        continue;
                    }

                    System.out.println("Eligible customers: " + customerIds(waiting));
                    System.out.println("Available tables: " + tableIds(freeTables));

                    int customerId = readInt("Customer ID: ");
                    int tableId = readInt("Table ID: ");
                    return WaiterChoice.seat(customerId, tableId);

                case 2:
                    if (orderable.isEmpty()) {
                        showError("No seated customers are waiting to place an order.");
                        continue;
                    }

                    System.out.println("Eligible customers: " + customerIds(orderable));
                    return WaiterChoice.takeOrder(readInt("Customer ID: "));

                case 3:
                    if (servable.isEmpty()) {
                        showError("No orders are ready to be served.");
                        continue;
                    }

                    System.out.println("Eligible orders: " + orderDetails(servable));
                    return WaiterChoice.serve(readInt("Order ID: "));

                case 4:
                    return WaiterChoice.waitTurn();

                default:
                    showError("Choose an option from 1 to 4.");
            }
        }
    }


    
    public ChefChoice readChefChoice(Restaurant restaurant) {
        while (true) {
            List<Order> preparable = getPreparableOrders(restaurant);

            System.out.println("\nChef's action:");
            System.out.println("  1. Prepare an order (one unit)");
            System.out.println("     Eligible orders: " + orderDetails(preparable));
            System.out.println("  2. Wait");

            int action = readInt("Choose 1-2: ");

            switch (action) {
                case 1:
                    if (preparable.isEmpty()) {
                        showError("No orders are available for preparation.");
                        continue;
                    }

                    System.out.println("Eligible orders: " + orderDetails(preparable));
                    return ChefChoice.prepare(readInt("Order ID: "));

                case 2:
                    return ChefChoice.waitTurn();

                default:
                    showError("Choose an option from 1 to 2.");
            }
        }
    }


    public void showError(String message) {
        System.out.println("Invalid action: " + message);
        System.out.println("Please try again. The staff task has not been used.");
    }

    public void showMessage(String message) {
        System.out.println(message);
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

    // Free tables available for seating.
    private List<Table> getFreeTables(Restaurant restaurant) {
        List<Table> free = new ArrayList<>();

        for (Table table : restaurant.getTables()) {
            if (table.isFree()) {
                free.add(table);
            }
        }
        return free;
    }

    // Seated customers who have not placed an order.
    private List<Customer> getOrderableCustomers(Restaurant restaurant) {
        List<Customer> eligible = new ArrayList<>();

        for (Table table : restaurant.getTables()) {
            Customer customer = table.getOccupant();

            if (customer != null
                    && customer.getState() == CustomerState.SEATED
                    && customer.getOrder() == null) {
                eligible.add(customer);
            }
        }
        return eligible;
    }

    // Orders eligible to be served on the current turn.
    private List<Order> getServableOrders(Restaurant restaurant, int turn) {
        List<Order> eligible = new ArrayList<>();

        for (Order order : restaurant.getActiveOrders()) {
            if (order.canServe(turn)) {
                eligible.add(order);
            }
        }
        return eligible;
    }

    // Orders that still require preparation.
    private List<Order> getPreparableOrders(Restaurant restaurant) {
        List<Order> eligible = new ArrayList<>();

        for (Order order : restaurant.getActiveOrders()) {
            if (order.getStatus() == OrderStatus.PLACED
                    || order.getStatus() == OrderStatus.PREPARING) {
                eligible.add(order);
            }
        }
        return eligible;
    }

    // Format Customer IDs.
    private String customerIds(List<Customer> customers) {
        if (customers.isEmpty()) {
            return "(none)";
        }

        StringJoiner ids = new StringJoiner(", ");
        for (Customer customer : customers) {
            ids.add("#" + customer.getId());
        }
        return ids.toString();
    }

    // Format Table IDs.
    private String tableIds(List<Table> tables) {
        if (tables.isEmpty()) {
            return "(none)";
        }

        StringJoiner ids = new StringJoiner(", ");
        for (Table table : tables) {
            ids.add("#" + table.getId());
        }
        return ids.toString();
    }

    // Format Order IDs, items and preparation progress.
    private String orderDetails(List<Order> orders) {
        if (orders.isEmpty()) {
            return "(none)";
        }

        StringJoiner details = new StringJoiner(", ");

        for (Order order : orders) {
            details.add("#" + order.getId() + " (" + order.toString() + ")");
        }
        return details.toString();
    }

    private String formatMoney(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    private String formatAverage(OptionalDouble average) {
        return average.isPresent() ? String.format("%.1f", average.getAsDouble()) : "N/A";
    }
}

