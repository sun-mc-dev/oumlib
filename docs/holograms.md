# Holograms

`dev.oum.oumlib.entity.hologram` · Paper

---

## Creating a Hologram

Holograms are packet-based — they don't exist as real entities on the server. Only the player(s) you show them to can see them.

```java
Hologram hologram = Hologram.builder()
    .location(spawnLocation)
    .line("<gold>Welcome to the Server!")
    .line("<gray>Online: <white>" + Bukkit.getOnlinePlayers().size())
    .build();
```

### Show / Hide

```java
hologram.show(player);    // show to one player
hologram.showAll();       // show to all online players
hologram.hide(player);    // hide from one player
hologram.hideAll();       // hide from everyone
```

---

## Updating Lines

```java
hologram.setLine(0, "<gold>Updated Title!");
hologram.setLine(1, "<gray>Players: <white>" + count);
```

Lines are 0-indexed from the top.

### Add / Remove Lines

```java
hologram.addLine("<yellow>New line at the bottom");
hologram.removeLine(2);
```

---

## Moving

```java
hologram.teleport(newLocation);
```

---

## Click Handling

Handle when players click (interact with) the hologram:

```java
Hologram hologram = Hologram.builder()
    .location(location)
    .line("<green>[Click Me]")
    .onClick(player -> player.sendMessage("You clicked the hologram!"))
    .build();
```

---

## Auto-Registration

Register with the hologram registry for automatic visibility management:

```java
OumLib.holograms().register("spawn-hologram", hologram);
```

Registered holograms are automatically shown to players when they join and hidden when they quit. They're also cleaned up on `OumLib.shutdown()`.

### Unregister

```java
OumLib.holograms().unregister("spawn-hologram");
```

---

## Updating on a Timer

A common pattern — update hologram text every few seconds:

```java
Hologram holo = Hologram.builder()
    .location(location)
    .line("<gold>Server Stats")
    .line("<gray>Loading...")
    .build();

OumLib.holograms().register("stats", holo);

Scheduler.runRepeating(Duration.ZERO, Duration.ofSeconds(5), () -> {
    holo.setLine(1, "<gray>Online: <white>" + Bukkit.getOnlinePlayers().size());
    holo.setLine(0, "<gold>TPS: <white>" + String.format("%.1f", Bukkit.getTPS()[0]));
});
```
