package de.echosmp.echosmp;

/** Parst Preise wie 40000, 40k, 2.5m und 1b sicher in ganze Münzen. */
public final class PriceParser {
    private PriceParser() {
    }

    public static long parse(String input) {
        if (input == null) {
            return 0L;
        }
        String value = input.toLowerCase().replace(',', '.').trim();
        long multiplier = 1L;
        if (value.endsWith("k")) {
            multiplier = 1_000L;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("m")) {
            multiplier = 1_000_000L;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("b")) {
            multiplier = 1_000_000_000L;
            value = value.substring(0, value.length() - 1);
        }
        try {
            double parsed = Double.parseDouble(value);
            if (!Double.isFinite(parsed) || parsed <= 0 || parsed > Long.MAX_VALUE / (double) multiplier) {
                return 0L;
            }
            long result = Math.round(parsed * multiplier);
            return result > 0 ? result : 0L;
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }
}
