# Display Entities

`dev.oum.oumlib.entity.display` · Paper

---

## DisplayBuilder

Build text, item, and block display entities with a fluent API. These are real server-side entities (1.19.4+), unlike holograms which are packet-based.

### Text Display

```java
DisplayBuilder.text()
    .location(location)
    .text("<gold>Hello World!")
    .backgroundColor(Color.fromARGB(128, 0, 0, 0)) // semi-transparent black
    .billboard(Display.Billboard.CENTER)
    .scale(1.5f, 1.5f, 1.5f)
    .spawn();
```

### Item Display

```java
DisplayBuilder.item()
    .location(location)
    .item(new ItemStack(Material.DIAMOND_SWORD))
    .transform(ItemDisplay.ItemDisplayTransform.FIXED)
    .billboard(Display.Billboard.VERTICAL)
    .scale(2f, 2f, 2f)
    .spawn();
```

### Block Display

```java
DisplayBuilder.block()
    .location(location)
    .block(Material.DIAMOND_BLOCK.createBlockData())
    .scale(0.5f, 0.5f, 0.5f)
    .spawn();
```

---

## Common Options

All display types share these:

| Method                          | What it does                                |
|:--------------------------------|:--------------------------------------------|
| `.location(loc)`                | Where to spawn                              |
| `.scale(x, y, z)`               | Size multiplier                             |
| `.translation(x, y, z)`         | Offset from origin                          |
| `.billboard(type)`              | `FIXED`, `CENTER`, `VERTICAL`, `HORIZONTAL` |
| `.glowing(bool)`                | Glowing outline                             |
| `.glowColor(color)`             | Glow color                                  |
| `.shadow(radius, strength)`     | Shadow settings                             |
| `.viewRange(float)`             | How far away players can see it             |
| `.interpolationDuration(ticks)` | Smooth animation duration                   |
| `.spawn()`                      | Spawns the entity and returns it            |

---

## Virtual Displays

Packet-based displays that only specific players can see. No real entity on the server.

### Virtual Text Display

```java
VirtualTextDisplay display = VirtualTextDisplay.create(location)
    .text("<green>Only you can see this!")
    .billboard(Display.Billboard.CENTER)
    .show(player);
```

### Virtual Item Display

```java
VirtualItemDisplay display = VirtualItemDisplay.create(location)
    .item(new ItemStack(Material.GOLDEN_APPLE))
    .show(player);
```

### Virtual Block Display

```java
VirtualBlockDisplay display = VirtualBlockDisplay.create(location)
    .block(Material.EMERALD_BLOCK.createBlockData())
    .show(player);
```

### Update / Move / Destroy

```java
display.teleport(newLocation);
display.destroy(player);
display.destroyAll();
```

---

## Display Animations

Animate display entities with interpolation:

```java
DisplayAnimation.animate(display)
    .scale(2f, 2f, 2f)
    .translation(0f, 2f, 0f)
    .duration(20) // ticks
    .play();
```

---

## Entities Helper

The `Entities` utility class has helpers for working with entities:

```java
Entities.nearbyPlayers(location, 10);           // players within 10 blocks
Entities.nearbyEntities(location, 5, type);     // entities of a type
Entities.closestPlayer(location, 50);           // nearest player
```
