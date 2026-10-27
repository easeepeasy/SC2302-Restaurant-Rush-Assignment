
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Stores the list of food items available on the restaurant's menu.
public class Menu {

    private final List<MenuItem> items = new ArrayList<>();

    // Creates the default menu with the 3 required dishes.
    // Prices are stored in cents.
    public static Menu baseMenu() {
        Menu menu = new Menu();
        menu.addItem(new Dish("Salad", 900, 1));
        menu.addItem(new Dish("Burger", 1500, 1));
        menu.addItem(new Dish("Pasta", 1800, 2));
        return menu;
    }

    // Returns the menu items without allowing the list to be modified directly.
    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(MenuItem item) {
        items.add(item);
    }

    // Searches for a dish by name, ignoring uppercase and lowercase differences.
    // Returns null if the dish is not found.
    public MenuItem findByName(String name) {
        for (MenuItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }
}
