package restaurant.model;

// Parent class for manager controlled staff (Waiter and Chef)
// Each staff member can only do one task per turn. A failed attempt does not use up the task,
// so the manager can choose again

public abstract class Staff { //Meant to be extended by the more specific staff classes

    private final String staffName; 
    //Final so we do not accidentally swap roles or restaurants
    private final Restaurant restaurant;
    private boolean actedThisTurn;

    protected Staff(String name, Restaurant res) {
        staffName = name;
        restaurant = res;
    }

    public String getName() {
        return staffName;
    }

    // Subclasses act through the Restaurant, which checks and applies the game rules
    protected Restaurant getRestaurant() {
        return restaurant;
    }

    // Called by the GameController at the start of every turn.
    public void startTurn() {
        actedThisTurn = false;
    }

    public boolean hasActed() {
        return actedThisTurn;
    }

    // Wait is always allowed, but it still uses up a turn's task.
    public ActionResult waitTurn() {
        if (hasActed()) {
            return alreadyActed();
        }
        actedThisTurn = true;
        return ActionResult.ok(staffName + " waits."); //for clarity of what action was done
    }

    // The failure every action returns once the task for this turn is used.
    protected ActionResult alreadyActed() {
        return ActionResult.fail(staffName + " has already acted this turn.");
    }

    // Uses up the task only if the attempt succeeded, then passes the result on unchanged.
    protected ActionResult useTaskIfSuccessful(ActionResult result) {
        if (result.isSuccess()) {
            actedThisTurn = true;
        }
        return result;
    }
}
