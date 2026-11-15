package restaurant.model;

public class Table {
    private final int id;
    private Customer occupant;
    public Table(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }
    public Customer getOccupant(){
        return occupant;
    }
    public boolean isFree(){
        return occupant == null;
    }
    public void occupy(Customer customer){
        if (!isFree()){
            throw new IllegalStateException("Table " + id + " is already occupied.");
        }
        occupant = customer;
    }
    public void release() {
        occupant = null;
    }
    @Override
    public String toString(){
        return "Table " + id + ": " + (isFree() ? "free" : "Customer #" + occupant.getId());
    }
}