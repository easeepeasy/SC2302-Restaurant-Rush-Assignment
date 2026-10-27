// Interface for food items that customers can order.
// All menu items have a name, price and preparation time.
public interface MenuItem {
    String getName();

    // Returns the price in cents.
    int getPriceCents();

    // Returns the number of preparation units needed by the chef.
    int getPrepUnits();
}
