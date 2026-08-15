# Text

`dev.oum.oumlib.text` · Paper / Velocity

---

## Sending Messages

All text uses MiniMessage formatting. No `ChatColor`, no legacy codes.

```java
Text.send(player, "<green>You earned <gold><amount></gold> coins!", "amount", 50);
```

The key-value pairs at the end become MiniMessage placeholders. You can pass as many as you want:

```java
Text.send(player, "<player> killed <target>!", "player", killer.getName(), "target", victim.getName());
```

### Send with a Record

Instead of key-value pairs, pass a record and its fields become placeholders automatically:

```java
record KillData(String killer, String victim, int reward) {}

Text.send(player, "<killer> killed <victim> for <reward> coins!", new KillData("Steve", "Alex", 100));
```

### Send Multiple Lines

```java
Text.sendLines(player, List.of(
    "<gold>==============",
    "<white>Welcome back!",
    "<gold>=============="
), "player", player.getName());
```

---

## Parsing

Convert a MiniMessage string to a Component:

```java
Component comp = Text.parse("<gradient:red:gold>Hello!</gradient>");
Component comp = Text.parse("<player>'s stats", Placeholder.parsed("player", name));
```

Reverse — Component back to MiniMessage string:

```java
String mm = Text.serialize(component);
```

Strip all tags:

```java
String plain = Text.strip("<red>Hello <bold>world</bold>"); // "Hello world"
```

---

## Action Bar

```java
Text.actionBar(player, "<yellow>+5 XP", "xp", 5);
```

---

## Titles

```java
Text.title(player, "<gold>Level Up!", "<gray>You are now level 10");

// with custom timing
Text.title(player, "<red>GAME OVER", "<gray>Better luck next time",
    Duration.ofMillis(300), Duration.ofSeconds(3), Duration.ofMillis(500));
```

---

## Boss Bar

```java
BossBar bar = Text.bossBar(player, "<red>Boss Health", 0.75f,
    BossBar.Color.RED, BossBar.Overlay.PROGRESS);
```

Temporary boss bar that hides itself:

```java
Text.bossBarTemporary(player, "<green>Quest Complete!", 1.0f,
    BossBar.Color.GREEN, BossBar.Overlay.PROGRESS,
    Duration.ofSeconds(5));
```

---

## Broadcasting

Send to all players:

```java
Text.broadcast("<red>Server restarting in 5 minutes!");
Text.broadcastActionBar("<yellow>Double XP active!");
Text.broadcastTitle("<gold>Event Started!", "<gray>Good luck!");
```

---

## Text Builder

Build clickable/hoverable components:

```java
Component msg = Text.builder("<blue>Click here")
    .click(ClickEvent.runCommand("/help"))
    .hover("<gray>Click for help")
    .build();
player.sendMessage(msg);
```

Shortcut:

```java
Component link = Text.clickable("<blue>[Click]",
    ClickEvent.openUrl("https://example.com"),
    "<gray>Opens a link");
```

---

## Presets

Register message prefixes during init:

```java
OumLib.init(this)
    .preset(Preset.SUCCESS, "<green>✔ </green>")
    .preset(Preset.ERROR, "<red>✖ </red>")
    .preset(Preset.INFO, "<gray>ℹ </gray>");
```

Then use them:

```java
Text.Preset.success(player, "Item purchased!");   // "✔ Item purchased!" in green
Text.Preset.error(player, "Not enough coins!");    // "✖ Not enough coins!" in red
Text.Preset.info(player, "Your balance: 500");     // "ℹ Your balance: 500" in gray

// broadcast versions
Text.Preset.successBroadcast("Server saved!");
```

---

## Placeholders

Register custom placeholders that resolve in any `Text.send()` call:

```java
OumLib.placeholders("myplugin")
    .register("level", player -> String.valueOf(getLevel(player)))
    .register("coins", player -> String.valueOf(getCoins(player)));
```

Then use `%myplugin_level%` or `%myplugin_coins%` in any message. Works with PlaceholderAPI and MiniPlaceholders if those plugins are installed.

---

## Localization

Send messages based on the player's client language:

```java
Localization.translateFor(player, "welcome-message");
```

Register translations:

```java
Localization.register("en", "welcome-message", "<green>Welcome!");
Localization.register("ko", "welcome-message", "<green>환영합니다!");
```

In commands:

```java
ctx.sendTranslated("welcome-message");
```

---

## TextInput

Capture chat input from a player:

```java
TextInput.request(player, "<gray>Type the item name:", input -> {
    player.sendMessage("You typed: " + input);
});
```

With a timeout:

```java
TextInput.request(player, "<gray>Enter amount:", Duration.ofSeconds(30), input -> {
    int amount = Integer.parseInt(input);
    // ...
}, () -> {
    player.sendMessage("Timed out!");
});
```

---

## Console ASCII Art

Print startup banners to console:

```java
Text.ascii(true,
    "<gradient:gold:yellow>  ___  _   _ __  __ ",
    "<gradient:gold:yellow> / _ \\| | | |  \\/  |",
    "<gradient:gold:yellow>| | | | | | | |\\/| |",
    "<gradient:gold:yellow>| |_| | |_| | |  | |",
    "<gradient:gold:yellow> \\___/ \\___/|_|  |_|"
);
```

---

## Format Utilities

Duration parsing and formatting live in `Format`:

```java
Duration d = Format.parseDuration("1h30m");    // 1 hour 30 minutes
String s = Format.formatDuration(d);           // "1h 30m"
String compact = Format.formatDurationCompact(d); // "01:30:00"
```
