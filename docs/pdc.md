# PDC (Persistent Data Container)

`dev.oum.oumlib.pdc` · Paper

---

## PDC Helper

Read and write persistent data on any block, entity, or item without dealing with `NamespacedKey` and `PersistentDataType` every time.

```java
PDC.set(player, "coins", 500);
int coins = PDC.get(player, "coins", PersistentDataType.INTEGER, 0);

PDC.set(player, "vip", true);
boolean vip = PDC.get(player, "vip", PersistentDataType.BOOLEAN, false);

PDC.remove(player, "coins");
PDC.has(player, "coins");
```

---

## DataKey

Type-safe keys so you never mix up types:

```java
DataKey<Integer, Integer> COINS = DataKey.ofInt("coins");
DataKey<String, String> RANK = DataKey.ofString("rank");
DataKey<Boolean, Boolean> VIP = DataKey.ofBoolean("vip");
DataKey<Double, Double> MULTIPLIER = DataKey.ofDouble("multiplier");
```

Then use them:

```java
PDC.set(player, COINS, 500);
int coins = PDC.get(player, COINS, 0);

PDC.set(player, RANK, "warrior");
String rank = PDC.get(player, RANK, "default");
```

### Available DataKey Types

| Factory                      | Stored Type   | Java Type |
|:-----------------------------|:--------------|:----------|
| `DataKey.ofInt("key")`       | INTEGER       | `int`     |
| `DataKey.ofString("key")`    | STRING        | `String`  |
| `DataKey.ofBoolean("key")`   | BOOLEAN       | `boolean` |
| `DataKey.ofDouble("key")`    | DOUBLE        | `double`  |
| `DataKey.ofFloat("key")`     | FLOAT         | `float`   |
| `DataKey.ofLong("key")`      | LONG          | `long`    |
| `DataKey.ofByte("key")`      | BYTE          | `byte`    |
| `DataKey.ofByteArray("key")` | BYTE_ARRAY    | `byte[]`  |
| `DataKey.ofIntArray("key")`  | INTEGER_ARRAY | `int[]`   |
| `DataKey.ofLongArray("key")` | LONG_ARRAY    | `long[]`  |

---

## PdcModel

Map a Java record to PDC storage. Define a record, and OumLib handles serialization/deserialization:

```java
public record PlayerStats(int kills, int deaths, double kdr) {}

PdcModel<PlayerStats> model = PdcModel.of(PlayerStats.class, "stats");

// Save
model.set(player, new PlayerStats(10, 3, 3.33));

// Load
PlayerStats stats = model.get(player);
if (stats != null) {
    int kills = stats.kills();
}

// Remove
model.remove(player);
```

Works with nested records too. Stored as JSON under the hood.

---

## PdcHolder

Wrap any PDC holder for a fluent API:

```java
PdcHolder holder = PdcHolder.of(player);

holder.setInt("level", 5);
holder.setString("class", "mage");
holder.setBoolean("active", true);
holder.setDouble("speed", 1.5);

int level = holder.getInt("level", 0);
String cls = holder.getString("class", "none");

// Components
holder.setComponent("display-name", Text.parse("<gold>Steve"));
Component name = holder.getComponent("display-name");

// Lists
holder.setList("friends", List.of("Alex", "Steve"));
List<String> friends = holder.getList("friends");
```

### Change Listeners

Get notified when PDC values change:

```java
PDC.addListener((holder, key, oldValue, newValue) -> {
    OumLib.logInfo("PDC changed: " + key + " = " + newValue);
});
```

---

## PdcItem

Same as PdcHolder but for items:

```java
PdcItem item = PdcItem.of(itemStack);

item.setInt("durability", 100);
int dur = item.getInt("durability", 0);

// Don't forget — item PDC returns a new ItemStack
ItemStack updated = item.toItemStack();
```

---

## PdcProperty

Observable, type-safe PDC property:

```java
PdcProperty<Integer> level = PdcProperty.ofInt("level", 1);

level.set(player, 5);
int lvl = level.get(player);

level.onChange((p, oldVal, newVal) -> {
    Text.send(p, "<gold>Level up! " + oldVal + " → " + newVal);
});
```

---

## PdcFlags

Bitfield flags stored as a single integer in PDC:

```java
PdcFlags flags = PdcFlags.of("player-flags");

int FLY = 0;
int VANISH = 1;
int GOD = 2;

flags.set(player, FLY, true);
flags.set(player, VANISH, true);

boolean canFly = flags.get(player, FLY);   // true
boolean isGod = flags.get(player, GOD);    // false
```

Stores multiple boolean flags in a single integer. Efficient for lots of on/off toggles.

---

## PdcTree

Nested PDC data, like a mini filesystem:

```java
PdcTree tree = PdcTree.of(player, "quests");

tree.set("main.tutorial.completed", true);
tree.set("main.tutorial.step", 3);
tree.set("side.mining.progress", 75);

boolean done = tree.getBoolean("main.tutorial.completed", false);
int step = tree.getInt("main.tutorial.step", 0);
```
