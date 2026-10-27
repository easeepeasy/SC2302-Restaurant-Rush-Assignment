// Utility class for handling money, which is stored in cents.
public final class Money {
    // Prevents objects of this class from being created.
    private Money() {
    }

    // Converts cents into dollar format with 2 decimal places.
    // For example, 1830 cents becomes $18.30.
    public static String format(int cents) {
        return String.format("$%d.%02d", cents / 100, Math.abs(cents % 100));
    }
}
