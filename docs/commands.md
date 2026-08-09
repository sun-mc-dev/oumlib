# Commands

`dev.oum.oumlib.command` · Paper / Velocity

---

## Basic Command

```java
CommandBuilder.create("hello")
    .description("Says hello")
    .executes(ctx -> {
        ctx.reply("<green>Hello, world!");
    })
    .register();
```

That's it. Works on both Paper and Velocity. On Paper it registers through Brigadier. On Velocity it uses the Velocity command API. You don't have to care which.

---

## Arguments

Add typed arguments with the `Arguments` factory:

```java
CommandBuilder.create("give-coins")
    .argument(Arguments.player("target"))
    .argument(Arguments.integer("amount", 1, 10000))
    .executes(ctx -> {
        Player target = ctx.args().get("target");
        int amount = ctx.args().get("amount");
        ctx.reply("<green>Gave " + amount + " coins to " + target.getName());
    })
    .register();
```

### Available Argument Types

| Method                                      | Type            | Notes                        |
|:--------------------------------------------|:----------------|:-----------------------------|
| `Arguments.word("name")`                    | `String`        | Single word                  |
| `Arguments.string("name")`                  | `String`        | Greedy (rest of input)       |
| `Arguments.integer("name")`                 | `Integer`       | Optional min/max             |
| `Arguments.decimal("name")`                 | `Double`        | Optional min/max             |
| `Arguments.bool("name")`                    | `Boolean`       |                              |
| `Arguments.floatArg("name")`                | `Float`         | Optional min/max             |
| `Arguments.longArg("name")`                 | `Long`          | Optional min/max             |
| `Arguments.player("name")`                  | `Player`        | Tab-completes online players |
| `Arguments.players("name")`                 | `List<Player>`  | Multiple players             |
| `Arguments.offlinePlayer("name")`           | `OfflinePlayer` |                              |
| `Arguments.world("name")`                   | `World`         | Paper only                   |
| `Arguments.material("name")`                | `Material`      |                              |
| `Arguments.enumValue("name", MyEnum.class)` | `Enum`          | Any enum class               |
| `Arguments.duration("name")`                | `Duration`      | Parses `1h30m`, `5s`, etc.   |
| `Arguments.entity("name")`                  | `Entity`        | Paper only                   |
| `Arguments.entities("name")`                | `List<Entity>`  | Paper only                   |
| `Arguments.finePosition("name")`            | `Location`      | Paper only, exact coords     |
| `Arguments.blockPosition("name")`           | `BlockPosition` | Paper only                   |
| `Arguments.key("name")`                     | `NamespacedKey` | Paper only                   |

### Custom Suggestions

```java
Arguments.word("kit")
    .suggests(ctx -> List.of("starter", "warrior", "mage"))
```

---

## Subcommands

```java
CommandBuilder.create("arena")
    .subcommand(sub -> sub
        .literal("join")
        .argument(Arguments.word("name"))
        .executes(ctx -> {
            String name = ctx.args().get("name");
            ctx.reply("<green>Joining arena: " + name);
        })
    )
    .subcommand(sub -> sub
        .literal("leave")
        .executes(ctx -> ctx.reply("<yellow>Left the arena"))
    )
    .register();
```

---

## Permissions

```java
// Using a Permission object (from bridge module)
CommandBuilder.create("admin")
    .permission(Permission.builder("myplugin.admin").build())
    .executes(ctx -> { /* ... */ })
    .register();
```

---

## Cooldowns

Built-in cooldown support — no extra wiring needed:

```java
CommandBuilder.create("daily")
    .cooldown(Duration.ofHours(24))
    .cooldownMessage("<red>Come back in <remaining>!")
    .executes(ctx -> {
        ctx.reply("<gold>Here's your daily reward!");
    })
    .register();
```

The `<remaining>` placeholder gets replaced with a formatted countdown like `23h 59m`.

You can share a cooldown across commands:

```java
CooldownManager<UUID> sharedCooldown = CooldownManager.create();

CommandBuilder.create("cmd1")
    .cooldown(Duration.ofSeconds(30), sharedCooldown)
    // ...

CommandBuilder.create("cmd2")
    .cooldown(Duration.ofSeconds(30), sharedCooldown)
    // ...
```

To let certain players bypass the cooldown:

```java
CommandBuilder.create("heal")
    .cooldown(Duration.ofMinutes(5))
    .cooldownBypass(ctx -> ctx.sender().hasPermission("myplugin.heal.bypass"))
    .executes(ctx -> { /* ... */ })
    .register();
```

---

## Aliases

```java
CommandBuilder.create("teleport")
    .aliases("tp", "goto")
    .executes(ctx -> { /* ... */ })
    .register();
```

---

## Error Handling

Per-command exception handler:

```java
CommandBuilder.create("risky")
    .onException((ctx, ex) -> {
        ctx.reply("<red>That command failed. Check console.");
        OumLib.logError("Command /risky failed", ex);
    })
    .executes(ctx -> {
        // if this throws, the handler above catches it
    })
    .register();
```

Or set a global handler during init:

```java
OumLib.init(this)
    .commandErrorHandler((ctx, ex) -> {
        ctx.reply("<red>An error occurred.");
    });
```

---

## CommandContext

The `ctx` object passed to your executor has these helpers:

| Method                           | What it does                            |
|:---------------------------------|:----------------------------------------|
| `ctx.sender()`                   | The `Audience` who ran the command      |
| `ctx.isPlayer()`                 | Whether the sender is a player          |
| `ctx.isConsole()`                | Whether the sender is the console       |
| `ctx.playerOrThrow()`            | Returns the player or throws            |
| `ctx.args()`                     | The `ArgumentMap` with parsed arguments |
| `ctx.label()`                    | The command label used                  |
| `ctx.reply(miniMessage)`         | Sends a MiniMessage string              |
| `ctx.reply(component)`           | Sends a Component                       |
| `ctx.sendActionBar(msg)`         | Action bar message                      |
| `ctx.sendTitle(title, subtitle)` | Title screen                            |
| `ctx.sendTranslated(key)`        | Sends a localized message               |
| `ctx.clearTitle()`               | Clears the title                        |
