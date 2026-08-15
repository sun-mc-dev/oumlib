# Database

`dev.oum.oumlib.database` · Paper / Velocity

---

## Creating a Database

### SQLite

```java
Database db = Database.sqlite(new File(getDataFolder(), "data.db"));
```

### MySQL

```java
Database db = Database.mysql("localhost", 3306, "mydb", "user", "password");
```

Both use HikariCP connection pooling under the hood.

---

## Queries

All queries are async by default and return a `CompletableFuture`.

### Execute Update (INSERT, UPDATE, DELETE, CREATE)

```java
db.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, name TEXT, coins INT)");

db.executeUpdate("INSERT INTO players (uuid, name, coins) VALUES (?, ?, ?)",
    player.getUniqueId().toString(), player.getName(), 500);

db.executeUpdate("UPDATE players SET coins = coins + ? WHERE uuid = ?",
    100, player.getUniqueId().toString());
```

### Execute Query (SELECT)

```java
db.executeQuery("SELECT * FROM players WHERE uuid = ?", uuid.toString())
    .thenAcceptSync(rows -> {
        if (!rows.isEmpty()) {
            Map<String, Object> row = rows.getFirst();
            int coins = (int) row.get("coins");
            String name = (String) row.get("name");
            player.sendMessage("You have " + coins + " coins");
        }
    });
```

Each row is a `Map<String, Object>`. Column names are the keys.

---

## Sync Callback

`.thenAcceptSync()` runs the callback on the main thread — safe for Bukkit API calls:

```java
db.executeQuery("SELECT coins FROM players WHERE uuid = ?", uuid.toString())
    .thenAcceptSync(rows -> {
        // this runs on the main thread
        player.teleport(spawn);
    });
```

---

## Transactions

```java
db.transaction(connection -> {
    try (var stmt = connection.prepareStatement("UPDATE players SET coins = coins - ? WHERE uuid = ?")) {
        stmt.setInt(1, 100);
        stmt.setString(2, buyerUuid);
        stmt.executeUpdate();
    }
    try (var stmt = connection.prepareStatement("UPDATE players SET coins = coins + ? WHERE uuid = ?")) {
        stmt.setInt(1, 100);
        stmt.setString(2, sellerUuid);
        stmt.executeUpdate();
    }
});
```

If anything throws, the transaction rolls back.

---

## Batch Operations

```java
db.executeBatch("INSERT INTO players (uuid, name, coins) VALUES (?, ?, ?)", batch -> {
    for (Player p : Bukkit.getOnlinePlayers()) {
        batch.add(p.getUniqueId().toString(), p.getName(), 0);
    }
});
```

---

## Close

Always close the database when your plugin disables:

```java
@Override
public void onDisable() {
    if (db != null) db.close();
}
```
