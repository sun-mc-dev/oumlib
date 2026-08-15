# Utilities

`dev.oum.oumlib.text`, `dev.oum.oumlib.math`, `dev.oum.oumlib.inventory`

---

## Duration Parsing

Parse human-readable duration strings:

```java
Duration d = Format.parseDuration("1h30m");     // 1 hour 30 minutes
Duration d = Format.parseDuration("5s");         // 5 seconds
Duration d = Format.parseDuration("2d12h");      // 2 days 12 hours
Duration d = Format.parseDuration("500ms");      // 500 milliseconds
```

Format a Duration back to a string:

```java
String s = Format.formatDuration(duration);         // "1h 30m"
String s = Format.formatDurationCompact(duration);   // "01:30:00"
```

---

## Number Formatting

```java
Format.compact(1500);      // "1.5K"
Format.compact(2300000);   // "2.3M"
Format.percent(0.756);     // "75.6%"
Format.ordinal(1);         // "1st"
Format.ordinal(22);        // "22nd"
```

---

## Location Serialization

Serialize locations to strings for config/database storage:

```java
String s = Locations.serialize(location);    // "world,10.5,64.0,20.3,90.0,0.0"
Location loc = Locations.deserialize(s);
```

Center a location on a block:

```java
Location centered = Locations.center(location);     // x.5, y, z.5
Location block = Locations.blockLocation(location); // integer coords
```

---

## Item Serialization

Convert items to/from Base64:

```java
String encoded = ItemSerializer.toBase64(itemStack);
ItemStack item = ItemSerializer.fromBase64(encoded);

// arrays
String encoded = ItemSerializer.arrayToBase64(items);
ItemStack[] items = ItemSerializer.arrayFromBase64(encoded);
```

---

## Potion Serialization

```java
String json = PotionSerializer.serialize(potionEffect);
PotionEffect effect = PotionSerializer.deserialize(json);

String json = PotionSerializer.serializeList(effects);
List<PotionEffect> list = PotionSerializer.deserializeList(json);
```

---

## Random / Chance

```java
Chance.percent(25);    // true 25% of the time
Chance.oneIn(10);      // true 1/10 of the time
```

---

## Weighted Random Selection

```java
WeightedSelector<String> loot = WeightedSelector.<String>create()
    .add("common_sword", 50)
    .add("rare_bow", 30)
    .add("legendary_staff", 5);

String drop = loot.select();
```

---

## Math Expression Evaluator

Evaluate math strings at runtime — useful for config-driven formulas:

```java
double result = MathEval.evaluate("2 + 3 * 4");         // 14.0
double result = MathEval.evaluate("100 * (1 + 0.05)^10"); // compound interest
double result = MathEval.evaluate("sqrt(144)");           // 12.0
```

---

## Pagination

Paginate a list for chat output:

```java
Pagination<String> pages = Pagination.of(allItems, 10); // 10 per page

List<String> page1 = pages.getPage(1);
int totalPages = pages.totalPages();
boolean hasNext = pages.hasNext(1);
```
