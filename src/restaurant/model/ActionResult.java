package restaurant.model;

// The outcome of an attempted action: whether it worked, plus a message for the manager.
// Staff members and Restaurant return this instead of throwing, so the UI can show the reason
// and let the manager choose again.
public class ActionResult {

    private final boolean success;
    private final String message;

    // Private so results can only be made through ok() and fail(), which read more clearly.
    private ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Creates a successful result with a message description of the action.
    public static ActionResult ok(String message) {
        return new ActionResult(true, message);
    }

    // Creates a failed result with the reason that the action was not allowed.
    public static ActionResult fail(String reason) {
        return new ActionResult(false, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
