package restaurant.model;

// Represents a dish on the menu with a fixed price and preparation time.
public class Dish implements MenuItem {
    private final String name;
    private final int priceCents;
    private final int prepUnits;

    public Dish(String name, int priceCents, int prepUnits) {
        // Make sure the price and preparation time are valid.
        if (priceCents < 0 || prepUnits < 1) {
            throw new IllegalArgumentException("Invalid dish: " + name);
        }
        this.name = name;
        this.priceCents = priceCents;
        this.prepUnits = prepUnits;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriceCents() {
        return priceCents;
    }

    @Override
    public int getPrepUnits() {
        return prepUnits;
    }

    // Displays the dish name, price and number of preparation units.
    @Override
    public String toString() {
        return name + " (" + String.format("$%d.%02d", priceCents / 100, priceCents % 100) + ", " + prepUnits + " unit"
                + (prepUnits == 1 ? "" : "s") + ")";
    }
}
