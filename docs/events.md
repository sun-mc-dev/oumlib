# Events

`dev.oum.oumlib.event` · Paper / Velocity

---

## Listening to Events

```java
Events.listen(PlayerJoinEvent.class, event -> {
    event.getPlayer().sendMessage("Welcome!");
});
```

That's it. No `@EventHandler`, no `implements Listener`, no registration boilerplate.

---

## Builder Style

For more control, use the builder:

```java
Events.listen(PlayerMoveEvent.class)
    .ignoreCancelled()
    .filter(e -> e.hasChangedBlock())
    .handler(event -> {
        // only fires when the player actually moves to a new block
    });
```

---

## Filters

Chain multiple filters:

```java
Events.listen(EntityDamageByEntityEvent.class)
    .filter(e -> e.getDamager() instanceof Player)
    .filter(e -> e.getDamage() > 5.0)
    .handler(event -> {
        Player attacker = (Player) event.getDamager();
        attacker.sendMessage("Big hit!");
    });
```

### Player Filter Shortcut

```java
Events.listen(PlayerInteractEvent.class)
    .playerFilter(PlayerInteractEvent::getPlayer, p -> p.hasPermission("myplugin.use"))
    .handler(event -> {
        // only fires for players with the permission
    });
```

---

## One-Shot Listeners

Fire once and automatically unregister:

```java
Events.listenOnce(PlayerJoinEvent.class, event -> {
    Bukkit.broadcastMessage("First player joined!");
});
```

Or with the builder:

```java
Events.listen(PlayerDeathEvent.class)
    .maxFires(1)
    .handler(event -> {
        // fires once, then unregisters
    });
```

---

## Expiry

Auto-unregister after a duration:

```java
Events.listen(PlayerMoveEvent.class)
    .expireAfter(Duration.ofMinutes(5))
    .handler(event -> {
        // only active for 5 minutes
    });
```

Or expire on a condition:

```java
Events.listen(PlayerMoveEvent.class)
    .expireIf(event -> someGameState.isOver())
    .handler(event -> {
        // unregisters when the game ends
    });
```

---

## Priority

```java
Events.listen(PlayerJoinEvent.class)
    .priority(EventPriority.HIGH)
    .handler(event -> { /* ... */ });
```

Available: `LOWEST`, `LOW`, `NORMAL`, `HIGH`, `HIGHEST`, `MONITOR`.

---

## Cancelled Events

```java
// Skip cancelled events (default Bukkit behavior)
Events.listen(PlayerInteractEvent.class)
    .ignoreCancelled()
    .handler(event -> { /* ... */ });

// Only run if the event WAS cancelled
Events.listen(PlayerInteractEvent.class)
    .onlyIfCancelled()
    .handler(event -> { /* ... */ });
```

---

## Async

Run the handler off the main thread:

```java
Events.listen(AsyncChatEvent.class)
    .async()
    .handler(event -> {
        // runs async
    });
```

---

## Unregistering

The `handler()` call returns a `ListenerHandle`:

```java
ListenerHandle handle = Events.listen(PlayerMoveEvent.class, event -> { /* ... */ });

// later
handle.unregister();
```

---

## Velocity Events

Works the same way on Velocity:

```java
Events.listen(PostLoginEvent.class, event -> {
    event.getPlayer().sendMessage(Component.text("Welcome to the network!"));
});
```
