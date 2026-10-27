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
        customer.markSeated();
        return ActionResult.ok("Customer #" + customerId + " seated at Table " + tableId + ".");
    }
    public void applyWaitingPenalties() {
        List<Customer> leaving = new ArrayList<>();
        for (Customer c : customers){
            if (c.isUnserved()){
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
    private Customer findQueuedCustomer(int customerId){
        for (Customer c : waitingQueue){
            if (c.getId() == customerId) return c;
        }
        return null;
    }
    private Table findTable(int tableId){
        for (Table t : tables){
            if (t.getId() == tableId) return t;
        }
        return null;
    }
    public Table findTableOf(Customer customer){
        for (Table t : tables){
            if (t.getOccupant() == customer) return t;
        }
        return null;
    }
}
