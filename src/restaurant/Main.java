
package restaurant;

import restaurant.control.GameController;
import restaurant.model.GameConfig;

// Entry point for the Stage 1 command-line game.
public class Main {
    public static void main(String[] args) {
        GameController game = new GameController(GameConfig.base());
        game.run();
    }
}
