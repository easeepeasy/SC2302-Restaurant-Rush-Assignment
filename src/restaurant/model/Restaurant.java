package restaurant.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class Restaurant {
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Order> activeOrders = new ArrayList<>();
    private final List<Table> tables = new ArrayList<>();
    private final ScoreBoard scoreBoard = new ScoreBoard();
    public Restaurant(int tableCount) {
        for (int i = 1; i <= tableCount; i++){
            tables.add(new Table(i));
        }
    }
    public void enqueue(Customer customer){
        customers.add(customer);
        waitingQueue.add(customer);
    }
    public ActionResult seatCustomer(int customerId, int tableId){
        if (waitingQueue.isEmpty()){
            return ActionResult.fail("No customers are waiting to be seated.");
        }
        Customer customer = findQueuedCustomer(customerId);
        if (customer == null){
            return ActionResult.fail("Customer #" + customerId + " is not in the waiting queue.");
        }
        Table table = findTable(tableId);
        if (table == null){
            return ActionResult.fail("Table " + tableId + " does not exist.");
        }
        if (!table.isFree()){
            return ActionResult.fail("Table " + tableId + " is occupied by Customer #" + table.getOccupant().getId() + ".");
        }
        waitingQueue.remove(customer);
        table.occupy(customer);
        customer.seatAt(table);
        return ActionResult.ok("Customer #" + customerId + " seated at Table " + tableId + ".");
    }
    // The bill is computed once here and fixed in the Order.
    public ActionResult placeOrder(int customerId, Menu menu, int turn){
        Customer customer = findCustomer(customerId);
        if (customer == null){
            return ActionResult.fail("Customer #" + customerId + " does not exist.");
        }
        if (customer.getState() != CustomerState.SEATED){
            return ActionResult.fail("Customer #" + customerId + " is not seated.");
        }
        if (customer.getOrder() != null){
            return ActionResult.fail("Customer #" + customerId + " has already ordered.");
        }
        MenuItem item = customer.chooseItem(menu);
        Order order = new Order(customerId, customer, item, customer.billFor(item), turn);
        customer.attachOrder(order);
        activeOrders.add(order);
        return ActionResult.ok("Took order from Customer #" + customerId + ": " + order + ".");
    }
    public ActionResult prepareOrder(int orderId, int turn){
        Order order = findOrder(orderId);
        if (order == null){
            return ActionResult.fail("Order " + orderId + " is not active.");
        }
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.PREPARING){
            return ActionResult.fail("Order " + orderId + " cannot be prepared while " + order.getStatus() + ".");
        }
        order.advanceOneUnit(turn);
        return ActionResult.ok("Prepared Order " + orderId + ": " + order + ".");
    }
    public ActionResult serveOrder(int orderId, int turn){
        Order order = findOrder(orderId);
        if (order == null){
            return ActionResult.fail("Order " + orderId + " is not active.");
        }
        if (!order.canServe(turn)){
            if (order.getStatus() == OrderStatus.READY){
                return ActionResult.fail("Order " + orderId + " became ready this turn and can be served next turn.");
            }
            return ActionResult.fail("Order " + orderId + " is not ready (" + order.getStatus() + ").");
        }
        order.markServed(turn);
        order.getCustomer().startEating(turn);
        scoreBoard.recordServed();
        return ActionResult.ok("Served Order " + orderId + " to Customer #" + order.getCustomer().getId() + ".");
    }
    public void finishEating(int turn){
        for (Customer c : customers){
            c.finishEating(turn);
        }
    }
    // Pays the customer who has been ready to pay the longest (lowest id breaks ties).
    public ActionResult payOldestEligible(int turn){
        Customer oldest = null;
        for (Customer c : customers){
            if (!c.canPay(turn)) continue;
            if (oldest == null
                    || c.getReadyToPayTurn() < oldest.getReadyToPayTurn()
                    || (c.getReadyToPayTurn() == oldest.getReadyToPayTurn() && c.getId() < oldest.getId())){
                oldest = c;
            }
        }
        if (oldest == null){
            return ActionResult.fail("No customer is ready to pay.");
        }
        Order order = oldest.getOrder();
        oldest.markPaid();
        scoreBoard.recordPayment(order.getBillCents(), oldest.getSatisfaction());
        Table table = findTableOf(oldest);
        if (table != null){
            table.release();
        }
        activeOrders.remove(order);
        return ActionResult.ok("Customer #" + oldest.getId() + " paid " + String.format("$%d.%02d", order.getBillCents() / 100, order.getBillCents() % 100) + ".");
    }
    public void applyWaitingPenalties() {
        List<Customer> leaving = new ArrayList<>();
        for (Customer c : customers){
            if (c.getState() == CustomerState.WAITING || c.getState() == CustomerState.SEATED){
                c.loseSatisfaction(c.waitingPenalty());
                if (!c.isSatisfied()){
                    leaving.add(c);
                }
            }
        }
        for (Customer c : leaving){
            removeUnhappyCustomer(c);
        }
    }
    public List<Customer> getWaitingQueue(){
        return Collections.unmodifiableList(new ArrayList<>(waitingQueue));
    }
    public List<Table> getTables(){
        return Collections.unmodifiableList(tables);
    }
    public List<Order> getActiveOrders(){
        return Collections.unmodifiableList(activeOrders);
    }
    public ScoreBoard getScoreBoard(){
        return scoreBoard;
    }
    private void removeUnhappyCustomer(Customer c){
        waitingQueue.remove(c);
        Table table = findTableOf(c);
        if (table != null){
            table.release();
        }
        if (c.getOrder() != null){
            activeOrders.remove(c.getOrder());
        }
        c.leave();
        scoreBoard.recordAbandonment();
    }
    private Customer findCustomer(int customerId){
        for (Customer c : customers){
            if (c.getId() == customerId) return c;
        }
        return null;
    }
    private Customer findQueuedCustomer(int customerId){
        for (Customer c : waitingQueue){
            if (c.getId() == customerId) return c;
        }
        return null;
    }
    private Order findOrder(int orderId){
        for (Order o : activeOrders){
            if (o.getId() == orderId) return o;
        }
        return null;
    }
    private Table findTable(int tableId){
        for (Table t : tables){
            if (t.getId() == tableId) return t;
        }
        return null;
    }
    private Table findTableOf(Customer customer){
        for (Table t : tables){
            if (t.getOccupant() == customer) return t;
        }
        return null;
    }
}
