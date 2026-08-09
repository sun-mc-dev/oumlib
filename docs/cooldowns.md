# Cooldowns

`dev.oum.oumlib.cooldown` · Paper / Velocity

---

## CooldownManager

A thread-safe, generic cooldown tracker. Key it on whatever you want — UUIDs, strings, integers, etc.

```java
CooldownManager<UUID> cooldowns = CooldownManager.create();

// Apply a 30-second cooldown
cooldowns.apply(player.getUniqueId(), Duration.ofSeconds(30));

// Check
if (cooldowns.isOnCooldown(player.getUniqueId())) {
    String remaining = cooldowns.formatRemaining(player.getUniqueId());
    player.sendMessage("Wait " + remaining);
    return;
}
```

### Test and Apply (Atomic)

Checks and applies in one call. Returns `true` if the action was allowed, `false` if on cooldown:

```java
if (!cooldowns.testAndApply(player.getUniqueId(), Duration.ofSeconds(10))) {
    player.sendMessage("Too fast!");
    return;
}
// action goes here
```

### Bypass Predicate

Skip cooldown for certain keys:

```java
CooldownManager<UUID> cooldowns = CooldownManager.<UUID>create()
    .bypassPredicate(uuid -> {
        Player p = Bukkit.getPlayer(uuid);
        return p != null && p.hasPermission("myplugin.cooldown.bypass");
    });
```

### Expiry Listeners

Run code when a cooldown expires:

```java
cooldowns.onExpire((uuid, cooldown) -> {
    Player p = Bukkit.getPlayer(uuid);
    if (p != null) {
        Text.send(p, "<green>Your ability is ready again!");
    }
});
```

### Custom Formatter

```java
cooldowns.defaultFormatter(CooldownFormatter.COMPACT); // "1h 30m 5s"
```

### Extend / Reduce

```java
cooldowns.extend(uuid, Duration.ofSeconds(10));  // add 10s
cooldowns.reduce(uuid, Duration.ofSeconds(5));   // remove 5s
```

### Persistent Store

Save cooldowns across restarts:

```java
CooldownManager<UUID> cooldowns = CooldownManager.<UUID>create()
    .store(new MyCooldownStore());

// load on startup
cooldowns.loadAllFromStore().join();
```

Implement `CooldownStore<K>` with `save()`, `remove()`, `loadAll()`, and `clear()`.

### Other Methods

| Method                   | What it does                       |
|:-------------------------|:-----------------------------------|
| `isOnCooldown(key)`      | Check if on cooldown               |
| `test(key)`              | Same as `isOnCooldown`             |
| `remainingMillis(key)`   | Remaining time in ms               |
| `remainingDuration(key)` | Remaining time as `Duration`       |
| `formatRemaining(key)`   | Formatted string like `2m 30s`     |
| `get(key)`               | Returns `Optional<Cooldown<K>>`    |
| `reset(key)`             | Remove the cooldown                |
| `cleanUp()`              | Remove all expired entries         |
| `clear()`                | Remove everything                  |
| `asMap()`                | Unmodifiable view of all cooldowns |

---

## RateLimiter

Token-bucket rate limiter for things like chat spam or action throttling:

```java
RateLimiter<UUID> limiter = RateLimiter.<UUID>create()
    .maxTokens(5)
    .refillRate(1, Duration.ofSeconds(2))  // 1 token every 2 seconds
    .build();

if (!limiter.tryConsume(player.getUniqueId())) {
    player.sendMessage("Slow down!");
    return;
}
```

The bucket starts full. Each action consumes a token. Tokens refill at the rate you set.

---

## Cooldown Record

The `Cooldown<K>` record holds the data for a single cooldown:

```java
Cooldown<UUID> cd = cooldowns.get(uuid).orElse(null);
if (cd != null) {
    cd.key();              // the key
    cd.startTime();        // when it was applied
    cd.expireTime();       // when it expires
    cd.remainingMillis();  // ms left
    cd.remainingDuration();// Duration left
    cd.isExpired();        // true if expired
    cd.isActive();         // true if still going
    cd.metadata();         // optional attached data
}
```
