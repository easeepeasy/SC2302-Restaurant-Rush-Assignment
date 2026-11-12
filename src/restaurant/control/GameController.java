package restaurant.control;

import java.util.List;
import java.util.Map;

import restaurant.model.ActionResult;
import restaurant.model.Cashier;
import restaurant.model.Chef;
import restaurant.model.CustomerType;
import restaurant.model.Dish;
import restaurant.model.GameConfig;
import restaurant.model.Menu;
import restaurant.model.Restaurant;
import restaurant.model.ScoreBoard;
import restaurant.model.Waiter;
import restaurant.ui.GameCLI;

// Runs the game turn by turn, following the six steps in the brief in order.
// It holds no game rules: Restaurant, Order and Customer check them and
// return an ActionResult explaining any failure.
public class GameController {

    private final GameCLI cli;
    private final Restaurant restaurant;
    private final Menu menu;
    private final ArrivalSchedule arrivals;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final GameConfig config;

    public GameController(GameConfig config) {
        this.config = config;
        cli = new GameCLI();
        restaurant = new Restaurant(config.TABLE_COUNT);
        menu = new Menu(List.of(
                new Dish("Salad", 900, 1),
                new Dish("Burger", 1500, 1),
                new Dish("Pasta", 1800, 2)));
        arrivals = new ArrivalSchedule(Map.of(
                1, CustomerType.REGULAR,
                4, CustomerType.VIP,
                7, CustomerType.CRITIC,
                10, CustomerType.REGULAR,
                13, CustomerType.REGULAR,
                16, CustomerType.REGULAR));
        waiter = new Waiter(restaurant);
        chef = new Chef(restaurant);
        cashier = new Cashier(restaurant);
    }

    // Plays all turns (no early ending), then shows Victory or Defeat.
    public void run() {
        for (int turn = 1; turn <= config.MAX_TURNS; turn++) {
            runTurn(turn);
        }
        ScoreBoard score = restaurant.getScoreBoard();
        cli.showResult(score.evaluate(config), score);
    }

    private void runTurn(int turn) {
        // Step 1: arrivals
        arrivals.customerAt(turn).ifPresent(restaurant::enqueue);

        // Start of turn: fresh tasks for the staff, then show the state
        waiter.startTurn();
        chef.startTurn();
        cli.showState(restaurant, turn, List.of(waiter, chef));

        // Steps 2-3: manager choices, Waiter before Chef.
        // An invalid choice shows why and asks again without using up the task.
        ActionResult result = processWaiterChoice(cli.readWaiterChoice(), turn);
        while (!result.isSuccess()) {
            cli.showError(result.getMessage());
            result = processWaiterChoice(cli.readWaiterChoice(), turn);
        }

        result = processChefChoice(cli.readChefChoice(), turn);
        while (!result.isSuccess()) {
            cli.showError(result.getMessage());
            result = processChefChoice(cli.readChefChoice(), turn);
        }

        // Step 4: eating, then automatic payment by the Cashier
        restaurant.finishEating(turn);
        cashier.collectPayment(turn);

        // Step 5: waiting customers lose satisfaction; anyone at 0 leaves
        restaurant.applyWaitingPenalties();
    }

    // Switches on the player's input, not on customer type
    private ActionResult processWaiterChoice(WaiterChoice choice, int turn) {
        switch (choice.getAction()) {
            case SEAT:
                return waiter.seat(choice.getCustomerId(), choice.getTableId());
            case TAKE_ORDER:
                return waiter.takeOrder(choice.getCustomerId(), menu, turn);
            case SERVE:
                return waiter.serve(choice.getOrderId(), turn);
            default:
                return waiter.waitTurn();
        }
    }

    private ActionResult processChefChoice(ChefChoice choice, int turn) {
        if (choice.isWait()) {
            return chef.waitTurn();
        }
        return chef.cook(choice.getOrderId(), turn);
    }
}
