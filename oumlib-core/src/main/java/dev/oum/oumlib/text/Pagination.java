package dev.oum.oumlib.text;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Function;

public final class Pagination<T> {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final List<T> items;
    private final int pageSize;
    private final String header;
    private final Function<T, String> entryRenderer;
    private final String footer;

    @Contract(pure = true)
    private Pagination(@NonNull Builder<T> builder) {
        this.items = List.copyOf(builder.items);
        this.pageSize = builder.pageSize;
        this.header = builder.header;
        this.entryRenderer = builder.entryRenderer;
        this.footer = builder.footer;
    }

    @Contract(value = "_ -> new", pure = true)
    public static <T> @NonNull Builder<T> builder(List<T> items) {
        return new Builder<>(items);
    }

    public int totalPages() {
        return Math.max(1, (int) Math.ceil((double) items.size() / pageSize));
    }

    public void send(@NonNull Audience audience, int page) {
        int clamped = Math.clamp(page, 1, totalPages());
        audience.sendMessage(MM.deserialize(placeholders(header, clamped)));
        int start = (clamped - 1) * pageSize;
        int end = Math.min(start + pageSize, items.size());
        for (int i = start; i < end; i++) {
            audience.sendMessage(MM.deserialize(entryRenderer.apply(items.get(i))));
        }
        if (footer != null) {
            audience.sendMessage(MM.deserialize(placeholders(footer, clamped)));
        }
    }

    private @NonNull String placeholders(@NonNull String template, int page) {
        return template
                .replace("<page>", String.valueOf(page))
                .replace("<total>", String.valueOf(totalPages()))
                .replace("<next>", String.valueOf(Math.min(page + 1, totalPages())))
                .replace("<prev>", String.valueOf(Math.max(page - 1, 1)));
    }

    public static final class Builder<T> {

        private final List<T> items;
        private int pageSize = 8;
        private String header = "<gray>--- Page <page>/<total> ---";
        private Function<T, String> entryRenderer;
        private String footer;

        private Builder(List<T> items) {
            this.items = items;
        }

        public Builder<T> pageSize(int size) {
            this.pageSize = size;
            return this;
        }

        public Builder<T> header(String header) {
            this.header = header;
            return this;
        }

        public Builder<T> entry(Function<T, String> renderer) {
            this.entryRenderer = renderer;
            return this;
        }

        public Builder<T> entryRenderer(Function<T, String> renderer) {
            this.entryRenderer = renderer;
            return this;
        }

        public Builder<T> footer(String footer) {
            this.footer = footer;
            return this;
        }

        @Contract(" -> new")
        public @NonNull Pagination<T> build() {
            if (entryRenderer == null) throw new IllegalStateException("entry() is required.");
            return new Pagination<>(this);
        }
    }
}