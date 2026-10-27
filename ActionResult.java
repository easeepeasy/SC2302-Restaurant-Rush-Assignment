public class ActionResult {
    private boolean succeed;
    private String returnmsg;

    private ActionResult(boolean success, String message) {
    succeed = success;
    returnmsg = message;
    }

    public static ActionResult success(String message) {
    return new ActionResult(true, message);
    }

    public static ActionResult fail(String message) {
    return new ActionResult(false, message);
    }

    public boolean isSuccessful(){
        return succeed;
    }

    public String getMessage(){
        return returnmsg;
    }
    
}
