package me.rkycse.coderush.util;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Get the current time as epoch milliseconds.
     * @return Current time in milliseconds (UTC)
     */
    public static long getCurrentEpochMillis() {
        return Instant.now().toEpochMilli();
    }

    /**
     * Convert epoch milliseconds to LocalDateTime (UTC).
     * @param epochMillis Epoch time in milliseconds
     * @return LocalDateTime in UTC
     */
    public static LocalDateTime convertEpochToDateTime(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis)
                .atZone(DEFAULT_ZONE)
                .toLocalDateTime();
    }

    /**
     * Format LocalDateTime to a readable string.
     * @param dateTime LocalDateTime to format
     * @return Formatted date-time string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }

    /**
     * Utility to convert epoch and format the result.
     * @param epochMillis Epoch time in milliseconds
     * @return Formatted Date-Time string in UTC
     */
    public static String formatEpochMillis(long epochMillis) {
        return formatDateTime(convertEpochToDateTime(epochMillis));
    }

}

