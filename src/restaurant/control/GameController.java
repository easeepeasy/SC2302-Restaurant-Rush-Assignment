package restaurant.control;

import java.util.List;
import java.util.Set;

import restaurant.model.ActionResult;
import restaurant.model.Cashier;
import restaurant.model.Chef;
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
        arrivals = new ArrivalSchedule(Set.of(1, 4, 7, 10, 13, 16));
        waiter = new Waiter();
        chef = new Chef();
        cashier = new Cashier();
    }

    // Plays all turns (no early ending), then shows Victory or Defeat.
    public void run() {
        for (int turn = 1; turn <= config.MAX_TURNS; turn++) {
            runTurn(turn);
        }
        ScoreBoard score = restaurant.getScoreBoard();
        cli.showResult(score.evaluate(config), score, config.MAX_TURNS);
    }

    
    private void runTurn(int turn) {
        // Step 1: arrivals
        arrivals.customerAt(turn).ifPresent(restaurant::enqueue);

        // Start of turn: reset staff tasks and display state.
        waiter.resetForTurn();
        chef.resetForTurn();
        cashier.resetForTurn();
        cli.showState(restaurant, turn, List.of(waiter, chef, cashier));

        // Waiter action; invalid choices do not consume the task.
        ActionResult result = processWaiterChoice(
                cli.readWaiterChoice(restaurant, turn), turn);

        while (!result.isSuccess()) {
            cli.showError(result.getMessage());
            result = processWaiterChoice(
                    cli.readWaiterChoice(restaurant, turn), turn);
        }
        cli.showMessage(result.getMessage());

        // Chef action; updated state includes the Waiter's action.
        result = processChefChoice(cli.readChefChoice(restaurant), turn);

        while (!result.isSuccess()) {
            cli.showError(result.getMessage());
            result = processChefChoice(cli.readChefChoice(restaurant), turn);
        }
        cli.showMessage(result.getMessage());

        // Eating and automatic payment.
        restaurant.finishEating(turn);
        ActionResult paymentResult = cashier.collectOldest(restaurant, turn);

        if (paymentResult.isSuccess()) {
            cli.showMessage(paymentResult.getMessage());
        }

        // Satisfaction penalties and abandonment.
        restaurant.applyWaitingPenalties();
    }


    // Switches on the player's input, not on customer type
    private ActionResult processWaiterChoice(WaiterChoice choice, int turn) {
        switch (choice.getAction()) {
            case SEAT:
                return waiter.seat(choice.getCustomerId(), choice.getTableId(), restaurant);
            case TAKE_ORDER:
                return waiter.takeOrder(choice.getCustomerId(), restaurant, menu, turn);
            case SERVE:
                return waiter.serve(choice.getOrderId(), restaurant, turn);
            default:
                waiter.waitTurn();
                return ActionResult.ok("Waiter waits.");
        }
    }

    private ActionResult processChefChoice(ChefChoice choice, int turn) {
        if (choice.isWait()) {
            chef.waitTurn();
            return ActionResult.ok("Chef waits.");
        }
        return chef.prepare(choice.getOrderId(), restaurant, turn);
    }
}
