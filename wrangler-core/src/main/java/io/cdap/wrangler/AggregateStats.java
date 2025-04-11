package io.cdap.wrangler;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.Collections;
import java.util.List;

public class AggregateStats implements Directive {
    public static final String NAME = "aggregate-stats";
    private String sizeCol, timeCol, totalSizeCol, totalTimeCol;
    private String sizeUnit = "MB", timeUnit = "s";

    @Override
    public UsageDefinition define() {
        return UsageDefinition.builder(NAME)
            .addArgument("sizeCol", true, "Source column with byte sizes", TokenType.COLUMN_NAME)
            .addArgument("timeCol", true, "Source column with time durations", TokenType.COLUMN_NAME)
            .addArgument("totalSizeCol", true, "Target column for total size", TokenType.COLUMN_NAME)
            .addArgument("totalTimeCol", true, "Target column for total time", TokenType.COLUMN_NAME)
            .addArgument("sizeUnit", false, "Output unit for size (default: MB)", TokenType.TEXT)
            .addArgument("timeUnit", false, "Output unit for time (default: s)", TokenType.TEXT)
            .build();
    }

    @Override
    public void initialize(Arguments args) {
        sizeCol = ((ColumnName) args.value("sizeCol")).value();
        timeCol = ((ColumnName) args.value("timeCol")).value();
        totalSizeCol = ((ColumnName) args.value("totalSizeCol")).value();
        totalTimeCol = ((ColumnName) args.value("totalTimeCol")).value();
        if (args.contains("sizeUnit")) sizeUnit = ((Text) args.value("sizeUnit")).value();
        if (args.contains("timeUnit")) timeUnit = ((Text) args.value("timeUnit")).value();
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) {
        long totalBytes = 0;
        long totalNanos = 0;

        for (Row row : rows) {
            Object size = row.getValue(sizeCol);
            Object time = row.getValue(timeCol);
            if (size instanceof String) {
                totalBytes += new ByteSize((String) size).getBytes();
            }
            if (time instanceof String) {
                totalNanos += new TimeDuration((String) time).getNanos();
            }
        }

        double finalSize = convertSize(totalBytes, sizeUnit);
        double finalTime = convertTime(totalNanos, timeUnit);

        Row result = new Row();
        result.add(totalSizeCol, finalSize);
        result.add(totalTimeCol, finalTime);
        return Collections.singletonList(result);
    }

    private double convertSize(long bytes, String unit) {
        switch (unit.toUpperCase()) {
            case "KB": return bytes / 1024.0;
            case "MB": return bytes / (1024.0 * 1024.0);
            case "GB": return bytes / (1024.0 * 1024.0 * 1024.0);
            case "TB": return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
            case "B":
            default: return bytes;
        }
    }

    private double convertTime(long nanos, String unit) {
        switch (unit.toUpperCase()) {
            case "MS": return nanos / 1_000_000.0;
            case "S": return nanos / 1_000_000_000.0;
            case "M": return nanos / (60.0 * 1_000_000_000.0);
            case "H": return nanos / (3600.0 * 1_000_000_000.0);
            case "D": return nanos / (24.0 * 3600.0 * 1_000_000_000.0);
            case "NS":
            default: return nanos;
        }
    }
}