package io.cdap.wrangler.api;

public class TimeDuration {
  
}
package io.cdap.wrangler.api.parser;

public class TimeDuration implements Token {
    private final String rawValue;
    private final long nanos;

    public TimeDuration(String value) {
        this.rawValue = value;
        this.nanos = parseTimeDuration(value);
    }

    private long parseTimeDuration(String value) {
        String num = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toUpperCase();
        double number = Double.parseDouble(num);
        switch (unit) {
            case "MS": return (long) (number * 1_000_000);
            case "S": return (long) (number * 1_000_000_000);
            case "M": return (long) (number * 60 * 1_000_000_000);
            case "H": return (long) (number * 3600 * 1_000_000_000);
            case "D": return (long) (number * 24 * 3600 * 1_000_000_000);
            case "NS":
            default: return (long) number; // Default to nanoseconds
        }
    }

    public long getNanos() {
        return nanos;
    }

    @Override
    public String value() {
        return rawValue;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }
}