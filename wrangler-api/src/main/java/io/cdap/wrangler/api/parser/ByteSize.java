package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;

public class ByteSize implements Token {
    private final String rawValue;
    private final long bytes;

    public ByteSize(String value) {
        this.rawValue = value;
        this.bytes = parseByteSize(value);
    }

    private long parseByteSize(String value) {
        String num = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toUpperCase();
        double number = Double.parseDouble(num);
        switch (unit) {
            case "KB": return (long) (number * 1024);
            case "MB": return (long) (number * 1024 * 1024);
            case "GB": return (long) (number * 1024 * 1024 * 1024);
            case "TB": return (long) (number * 1024 * 1024 * 1024 * 1024);
            case "B":
            default: return (long) number; // Default to bytes
        }
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public String value() {
        return rawValue;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toJson'");
    }
}