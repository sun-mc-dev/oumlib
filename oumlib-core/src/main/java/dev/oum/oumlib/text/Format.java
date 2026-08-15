package dev.oum.oumlib.text;

import dev.oum.oumlib.math.FormatMath;
import org.jspecify.annotations.NonNull;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Format {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###.##");

    private Format() {
    }

    public static @NonNull String duration(@NonNull Duration duration) {
        long seconds = duration.getSeconds();
        if (seconds <= 0) return "0s";

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (secs > 0 || sb.isEmpty()) sb.append(secs).append("s");

        return sb.toString().trim();
    }

    public static @NonNull String digitalTime(@NonNull Duration duration) {
        long seconds = duration.getSeconds();
        if (seconds <= 0) return "00:00";

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }

    public static String number(double number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String compactNumber(double number) {
        return FormatMath.compact(number);
    }

    public static @NonNull Duration parseDuration(@NonNull String input) {
        String cleaned = input.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        long totalSeconds = 0;
        Pattern pattern = Pattern.compile("(\\d+)([dhms])");
        Matcher matcher = pattern.matcher(cleaned);
        boolean matched = false;
        while (matcher.find()) {
            matched = true;
            long val = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "d" -> totalSeconds += val * 24 * 60 * 60;
                case "h" -> totalSeconds += val * 60 * 60;
                case "m" -> totalSeconds += val * 60;
                case "s" -> totalSeconds += val;
            }
        }
        if (!matched) {
            try {
                return Duration.ofSeconds(Long.parseLong(cleaned));
            } catch (NumberFormatException e) {
                return Duration.ZERO;
            }
        }
        return Duration.ofSeconds(totalSeconds);
    }

    public static @NonNull String percentage(double value, double max) {
        if (max <= 0) return "0.0%";
        double pct = (value / max) * 100.0;
        return String.format(Locale.ROOT, "%.1f%%", pct);
    }

    public static @NonNull String roman(int number) {
        if (number <= 0) return "";
        return FormatMath.toRoman(number);
    }

    public static @NonNull String ordinal(int number) {
        int mod100 = number % 100;
        int mod10 = number % 10;
        if (mod100 >= 11 && mod100 <= 13) {
            return number + "th";
        }
        return switch (mod10) {
            case 1 -> number + "st";
            case 2 -> number + "nd";
            case 3 -> number + "rd";
            default -> number + "th";
        };
    }
}
