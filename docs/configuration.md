# Configuration

`dev.oum.oumlib.config` · Paper / Velocity

---

## How It Works

Configs are Java records that implement `ConfigSection`. You define defaults, OumLib writes the YAML file, and gives you a type-safe object to read from.

```java
public record Settings(
    String prefix,
    int maxPlayers,
    boolean debug,
    double spawnRadius
) implements ConfigSection {}
```

```java
ConfigManager<Settings> config = ConfigManager.of(
    Settings.class,
    "settings.yml",
    () -> new Settings("<gold>[Server]</gold>", 50, false, 10.0)
);
```

This creates `settings.yml` in your plugin's data folder (if it doesn't exist) with the defaults. On load, it reads the file and maps values back into the record.

### Reading Values

```java
Settings s = config.get();
String prefix = s.prefix();
int max = s.maxPlayers();
```

---

## Auto-Reload

Watches the file for changes and reloads automatically:

```java
ConfigManager<Settings> config = ConfigManager.of(
    Settings.class, "settings.yml", () -> new Settings(/* defaults */)
).enableAutoReload();
```

You can also add a callback when the config reloads:

```java
config.onReload(newSettings -> {
    OumLib.logInfo("Config reloaded! Debug is now: " + newSettings.debug());
});
```

### Manual Reload

```java
config.reload();
```

### Save

Write the current values back to disk:

```java
config.save();
```

---

## Nested Records

Records inside records work fine:

```java
public record DatabaseConfig(String host, int port, String database) implements ConfigSection {}

public record MainConfig(
    String serverName,
    DatabaseConfig database
) implements ConfigSection {}
```

This produces:

```yaml
server-name: "My Server"
database:
  host: "localhost"
  port: 3306
  database: "mydb"
```

---

## Comments

Use the `@Comment` annotation to add comments above fields in the YAML output:

```java
public record Settings(
    @Comment("The prefix shown before messages")
    String prefix,

    @Comment("Max players allowed in the arena")
    int maxPlayers
) implements ConfigSection {}
```

Produces:

```yaml
# The prefix shown before messages
prefix: "<gold>[Server]</gold>"

# Max players allowed in the arena
max-players: 50
```

---

## Supported Types

- Primitives: `int`, `double`, `float`, `long`, `short`, `boolean`
- `String`
- `Component` (stored as MiniMessage strings)
- `List<String>`, `List<Integer>`, `List<Double>`, etc.
- `Map<String, Object>`
- Nested `Record` types that implement `ConfigSection`
- Enums

---

## Migrations

For when you rename or restructure config fields between versions:

```java
ConfigManager<Settings> config = ConfigManager.of(Settings.class, "settings.yml", () -> defaults)
    .migrations(registry -> {
        registry.rename("old-field-name", "new-field-name");
        registry.remove("deprecated-field");
    });
```

> **Note:** Field names in YAML use kebab-case (`max-players`), not camelCase (`maxPlayers`). OumLib handles the conversion automatically.
