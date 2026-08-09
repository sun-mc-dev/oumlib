# Scheduler

`dev.oum.oumlib.scheduler` · Paper / Velocity / Folia

---

## Basics

OumLib picks the right scheduler for your platform. On Paper it uses Bukkit's scheduler, on Folia it uses the region scheduler, on Velocity it uses Velocity's scheduler. You just call `Scheduler` and it works.

### Run on Main Thread

```java
Scheduler.run(() -> {
    player.teleport(spawn);
});
```

### Run Later

```java
Scheduler.runLater(Duration.ofSeconds(5), () -> {
    player.sendMessage("5 seconds passed!");
});

// or in ticks
Scheduler.runLater(100L, () -> {
    // 100 ticks = 5 seconds
});
```

### Run Repeating

```java
Scheduler.runRepeating(Duration.ZERO, Duration.ofSeconds(1), () -> {
    // runs every second, starting now
});

// in ticks
Scheduler.runRepeating(0L, 20L, () -> {
    // every 20 ticks
});
```

### Run Async

```java
Scheduler.runAsync(() -> {
    // off the main thread
    String data = fetchFromAPI();
    Scheduler.run(() -> {
        // back on main thread
        player.sendMessage(data);
    });
});
```

### Virtual Threads

Java 21 virtual threads for lightweight async work:

```java
Scheduler.runVirtual(() -> {
    // runs on a virtual thread
    // great for I/O: database, HTTP, file reads
});
```

---

## TaskHandle

All `run*` methods return a `TaskHandle` you can cancel:

```java
TaskHandle task = Scheduler.runRepeating(Duration.ZERO, Duration.ofSeconds(1), () -> {
    // ...
});

// later
task.cancel();
```

---

## Promise

Async result with sync callback:

```java
Scheduler.supplyAsync(() -> {
    return database.loadProfile(uuid);
}).thenAcceptSync(profile -> {
    // runs on main thread with the result
    player.sendMessage("Welcome, " + profile.name());
}).exceptionally(ex -> {
    player.sendMessage("Failed to load profile");
    return null;
});
```

### Virtual Thread Promise

```java
Scheduler.supplyVirtual(() -> {
    return httpClient.get("https://api.example.com/data");
}).thenAcceptSync(data -> {
    player.sendMessage("Got: " + data);
});
```

---

## TaskChain

Chain multiple sync/async steps:

```java
Scheduler.<PlayerData>chain()
    .async(() -> database.load(uuid))          // load async
    .syncWith((data) -> {                       // process on main
        player.sendMessage("Loaded: " + data);
        return data;
    })
    .async(data -> database.save(data))        // save async
    .execute();
```

---

## TaskGroup

Manage a group of tasks:

```java
TaskGroup group = Scheduler.newGroup();

group.add(Scheduler.runRepeating(Duration.ZERO, Duration.ofSeconds(1), () -> { /* task 1 */ }));
group.add(Scheduler.runRepeating(Duration.ZERO, Duration.ofSeconds(2), () -> { /* task 2 */ }));

// cancel all at once
group.cancelAll();
```

---

## Countdown

A countdown timer that ticks every second:

```java
Countdown.create(30) // 30 seconds
    .onTick(remaining -> {
        Text.actionBar(player, "<yellow>Starting in " + remaining + "s");
    })
    .onComplete(() -> {
        Text.send(player, "<green>Go!");
    })
    .start();
```

---

## Folia Support

On Folia, location-bound and entity-bound tasks run on the correct region thread:

```java
// Run on the region that owns this location
Scheduler.runAt(location, () -> {
    // safe for this region
});

// Run on the entity's owning thread
Scheduler.runFor(entity, () -> {
    entity.setHealth(20);
});
```

On non-Folia servers, these just run on the main thread.
