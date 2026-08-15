# Setup

`dev.oum.oumlib` · Paper / Velocity / Folia

---

## Paper

Call `OumLib.init(this)` in your `onEnable` and `OumLib.shutdown()` in `onDisable`.

```java
public final class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        OumLib.init(this)
            .preset(Preset.SUCCESS, "<green>")
            .preset(Preset.ERROR, "<red>")
            .preset(Preset.INFO, "<gray>")
            .commandErrorHandler((ctx, ex) -> {
                ctx.reply("<red>Something went wrong.");
                OumLib.logError("Command error", ex);
            });
    }

    @Override
    public void onDisable() {
        OumLib.shutdown();
    }
}
```

`init()` sets up:
- The scheduler adapter (Bukkit or Folia, detected automatically)
- The event bus
- Hologram registry and region tracker
- Recipe registry
- Volatile metadata store
- PlaceholderAPI and MiniPlaceholders hooks (if those plugins are present)

`shutdown()` cleans up everything — cancels tasks, removes holograms, stops config watchers.

---

## Velocity

Pass the `ProxyServer` and your plugin instance:

```java
@Plugin(id = "my-proxy-plugin")
public final class MyProxyPlugin {

    @Inject
    public MyProxyPlugin(ProxyServer server) {
        OumLib.init(server, this);
    }
}
```

Same shutdown call: `OumLib.shutdown()`.

---

## Platform Detection

```java
OumLib.isPaper();    // true on Paper/Folia
OumLib.isVelocity(); // true on Velocity
```

Use `OumLib.plugin()` to get the Bukkit `Plugin` instance, or `OumLib.proxy()` to get the `ProxyServer`.

---

## InitBuilder Options

The `init()` call returns an `InitBuilder` you can chain:

| Method                               | What it does                                   |
|:-------------------------------------|:-----------------------------------------------|
| `.preset(Preset.SUCCESS, "<green>")` | Registers a text preset for `Text.send()`      |
| `.commandErrorHandler(handler)`      | Sets the global error handler for all commands |

---

## Logging

OumLib provides a few logging shortcuts that work on both Paper and Velocity:

```java
OumLib.logInfo("Server started");
OumLib.logWarning("Low memory");
OumLib.logError("Database failed", exception);
OumLib.logDebug("Loading player data"); // only prints when debug mode is on
```

Toggle debug mode:
```java
OumLib.setDebug(true);
```

---

## Audience Helpers

```java
OumLib.players(); // all online players as an Audience
OumLib.console(); // the console as an Audience
```

Works on both Paper and Velocity.
