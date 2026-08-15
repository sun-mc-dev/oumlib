# Metadata

`dev.oum.oumlib.pdc.metadata` · Paper

---

## VolatileData

In-memory key-value store attached to players, entities, or any UUID. Unlike PDC, this data is **not saved to disk** — it's gone when the server stops or the player quits.

Good for temporary state: combat tags, cooldown flags, GUI state, session data.

```java
VolatileData.set(player, "in-combat", true);
VolatileData.set(player, "last-hit-time", System.currentTimeMillis());

boolean inCombat = VolatileData.get(player, "in-combat", false);
```

---

## Auto-Cleanup

Volatile data for a player is automatically removed when they quit. No manual cleanup needed.

---

## TTL (Time-To-Live)

Set data that expires after a duration:

```java
VolatileData.set(player, "speed-boost", true, Duration.ofSeconds(30));

// 30 seconds later, "speed-boost" is automatically removed
```

---

## Remove / Check

```java
VolatileData.remove(player, "in-combat");
boolean has = VolatileData.has(player, "in-combat");
```

---

## Clear

```java
VolatileData.clear(player);    // remove all data for this player
VolatileData.clearAll();       // remove everything (called on shutdown)
```

---

## Use Cases

- **Combat tagging:** Set a `"combat"` flag when a player attacks. Remove it after 15 seconds. If they try to log out while tagged, cancel it.
- **GUI state:** Store what page a player is on in a paginated menu.
- **Temporary buffs:** Mark a player as having a speed boost for 30 seconds.
- **Cooldown flags:** Quick boolean checks without the full `CooldownManager` overhead.
