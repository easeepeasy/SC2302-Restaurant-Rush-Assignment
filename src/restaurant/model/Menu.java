package restaurant.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Stores the list of food items available on the restaurant's menu.
public class Menu {

    private final List<MenuItem> items;

    public Menu(List<MenuItem> items) {
        this.items = new ArrayList<>(items);
    }

    // Returns the menu items without allowing the list to be modified directly.
    public List<MenuItem> listItems() {
        return Collections.unmodifiableList(items);
    }

    public MenuItem itemAt(int index) {
        return items.get(index);
    }
}
