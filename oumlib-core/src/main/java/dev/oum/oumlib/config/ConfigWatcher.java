package dev.oum.oumlib.config;

import dev.oum.oumlib.OumLib;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class ConfigWatcher {

    private static final long COALESCE_MILLIS = 150;
    private static final Map<Path, Map<String, Runnable>> watched = new ConcurrentHashMap<>();

    private static WatchService watchService;
    private static Thread thread;
    private static volatile boolean running;

    private ConfigWatcher() {
    }

    public static synchronized void watch(@NonNull Path directory, String fileName, Runnable onChange) {
        Path dir = directory.toAbsolutePath();
        ensureStarted();
        if (watchService == null) return;

        boolean newDir = !watched.containsKey(dir);
        watched.computeIfAbsent(dir, d -> new ConcurrentHashMap<>()).put(fileName, onChange);
        if (newDir) {
            try {
                dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {
                watched.remove(dir);
                OumLib.logError("Could not watch directory " + dir + " for config changes.", e);
            }
        }
    }

    public static synchronized void shutdown() {
        running = false;
        watched.clear();
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
            watchService = null;
        }
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    private static void ensureStarted() {
        if (running) return;
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            OumLib.logError("Could not start config watch service.", e);
            return;
        }
        running = true;
        thread = Thread.ofVirtual().name("oumlib-config-watcher").start(ConfigWatcher::loop);
    }

    private static void loop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                break;
            }

            Map<Path, Set<String>> changed = new HashMap<>();
            do {
                Path dir = (Path) key.watchable();
                for (WatchEvent<?> event : key.pollEvents()) {
                    changed.computeIfAbsent(dir, d -> new HashSet<>()).add(event.context().toString());
                }
                key.reset();
                try {
                    key = watchService.poll(COALESCE_MILLIS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ClosedWatchServiceException e) {
                    key = null;
                }
            } while (key != null);

            dispatch(changed);
        }
    }

    private static void dispatch(@NonNull Map<Path, Set<String>> changed) {
        changed.forEach((dir, files) -> {
            Map<String, Runnable> registered = watched.get(dir);
            if (registered == null) return;
            files.forEach(file -> {
                Runnable handler = registered.get(file);
                if (handler == null) return;
                try {
                    handler.run();
                } catch (Throwable t) {
                    OumLib.logError("Failed to reload config " + file, t);
                }
            });
        });
    }
}
