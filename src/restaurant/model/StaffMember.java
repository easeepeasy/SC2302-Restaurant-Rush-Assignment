package restaurant.model;

// Base class for staff members who can complete one task per turn.
public abstract class StaffMember {

    protected boolean taskUsed;

    public boolean isAvailable() {
        return !taskUsed;
    }

    public void resetForTurn() {
        taskUsed = false;
    }

    public void waitTurn() {
        consumeTask();
    }

    protected void consumeTask() {
        taskUsed = true;
    }
}
