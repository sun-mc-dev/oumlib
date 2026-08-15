package dev.oum.oumlib.cooldown;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Locale;

public final class CooldownFormatter {

    public static final CooldownFormatter DEFAULT = builder().build();
    public static final CooldownFormatter COMPACT = builder().compact(true).build();
    public static final CooldownFormatter PRECISE = builder().showFractionsUnderOneSecond(true).build();

    private final boolean compact;
    private final boolean showFractionsUnderOneSecond;
    private final String daysSuffix;
    private final String hoursSuffix;
    private final String minutesSuffix;
    private final String secondsSuffix;

    @Contract(pure = true)
    private CooldownFormatter(@NonNull Builder builder) {
        this.compact = builder.compact;
        this.showFractionsUnderOneSecond = builder.showFractionsUnderOneSecond;
        this.daysSuffix = builder.daysSuffix;
        this.hoursSuffix = builder.hoursSuffix;
        this.minutesSuffix = builder.minutesSuffix;
        this.secondsSuffix = builder.secondsSuffix;
    }

    public static @NonNull Builder builder() {
        return new Builder();
    }

    public @NonNull String format(@NonNull Duration duration) {
        long totalMillis = Math.max(0, duration.toMillis());
        if (totalMillis == 0) {
            return "0" + secondsSuffix;
        }

        if (showFractionsUnderOneSecond && totalMillis < 1000) {
            double secs = totalMillis / 1000.0;
            return String.format(Locale.ROOT, "%.1f%s", secs, secondsSuffix);
        }

        long totalSeconds = (totalMillis + 999) / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (compact) {
            if (days > 0) {
                return String.format(Locale.ROOT, "%d:%02d:%02d:%02d", days, hours, minutes, seconds);
            }
            if (hours > 0) {
                return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
            }
            return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
        }

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(daysSuffix).append(' ');
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append(hoursSuffix).append(' ');
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append(minutesSuffix).append(' ');
        }
        sb.append(seconds).append(secondsSuffix);

        return sb.toString().trim();
    }

    public static final class Builder {
        private boolean compact = false;
        private boolean showFractionsUnderOneSecond = false;
        private String daysSuffix = "d";
        private String hoursSuffix = "h";
        private String minutesSuffix = "m";
        private String secondsSuffix = "s";

        private Builder() {
        }

        public @NonNull Builder compact(boolean compact) {
            this.compact = compact;
            return this;
        }

        public @NonNull Builder showFractionsUnderOneSecond(boolean show) {
            this.showFractionsUnderOneSecond = show;
            return this;
        }

        public @NonNull Builder suffixes(@NonNull String days, @NonNull String hours, @NonNull String minutes, @NonNull String seconds) {
            this.daysSuffix = days;
            this.hoursSuffix = hours;
            this.minutesSuffix = minutes;
            this.secondsSuffix = seconds;
            return this;
        }

        public @NonNull CooldownFormatter build() {
            return new CooldownFormatter(this);
        }
    }
}
