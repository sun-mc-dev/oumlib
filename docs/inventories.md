# Inventories

`dev.oum.oumlib.inventory` · Paper

---

## ChestMenu

Build a chest GUI with a pattern layout:

```java
ChestMenu.builder()
    .title("<dark_gray>Warps</dark_gray>")
    .rows(3)
    .pattern(
        "#########",
        "# A B C #",
        "#########"
    )
    .bind('#', ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
    .bind('A', ItemBuilder.of(Material.GRASS_BLOCK).name("<green>Spawn").build())
    .bind('B', ItemBuilder.of(Material.NETHERRACK).name("<red>Nether").build())
    .bind('C', ItemBuilder.of(Material.END_STONE).name("<dark_purple>End").build())
    .onClick('A', click -> click.player().performCommand("warp spawn"))
    .onClick('B', click -> click.player().performCommand("warp nether"))
    .onClick('C', click -> click.player().performCommand("warp end"))
    .build()
    .open(player);
```

### Click Handlers

Each bound character can have a click handler. The `ClickContext` gives you:

```java
.onClick('X', click -> {
    Player p = click.player();          // the player who clicked
    ClickAction action = click.action(); // LEFT, RIGHT, SHIFT_LEFT, etc.
    ItemStack item = click.item();      // the clicked item
})
```

### Close Handler

```java
.onClose(player -> {
    player.sendMessage("Menu closed!");
})
```

### Prevent Taking Items

By default, players can't take items from the menu. The whole inventory is locked.

---

## PaginatedMenu

For when you have a list of items and want pages:

```java
List<ItemStack> items = getShopItems(); // your list

PaginatedMenu.builder()
    .title("<dark_gray>Shop - Page <page>/<pages></dark_gray>")
    .rows(6)
    .items(items)
    .contentSlots(Layout.rectangle(1, 1, 4, 7)) // rows 1-4, columns 1-7
    .previousButton(ItemBuilder.of(Material.ARROW).name("<yellow>Previous Page").build(), 45)
    .nextButton(ItemBuilder.of(Material.ARROW).name("<yellow>Next Page").build(), 53)
    .border(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build())
    .onItemClick((click, item) -> {
        click.player().sendMessage("You clicked: " + item.getType());
    })
    .build()
    .open(player);
```

The `<page>` and `<pages>` placeholders in the title get replaced automatically.

---

## ItemBuilder

Fluent builder for creating items:

```java
ItemStack sword = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("<gradient:aqua:blue>Frost Blade</gradient>")
    .lore(
        "<gray>A blade forged in ice.",
        "",
        "<blue>+15 Attack Damage"
    )
    .enchant(Enchantment.SHARPNESS, 5)
    .unbreakable(true)
    .modelData(1001)
    .amount(1)
    .glow(true)
    .build();
```

### ItemBuilder Methods

| Method                     | What it does                               |
|:---------------------------|:-------------------------------------------|
| `.name(miniMessage)`       | Display name                               |
| `.lore(lines...)`          | Lore lines (MiniMessage)                   |
| `.enchant(enchant, level)` | Add enchantment                            |
| `.unbreakable(bool)`       | Set unbreakable                            |
| `.modelData(int)`          | Custom model data                          |
| `.amount(int)`             | Stack size                                 |
| `.glow(bool)`              | Enchantment glint without visible enchants |
| `.flags(flags...)`         | Item flags                                 |
| `.rarity(ItemRarity)`      | Item rarity                                |
| `.maxStackSize(int)`       | Override max stack size                    |
| `.skull(player)`           | Player head                                |
| `.skullTexture(base64)`    | Custom skull texture                       |
| `.skullUrl(url)`           | Skull from URL                             |
| `.pdc(key, value)`         | Store persistent data                      |
| `.pdc(key, component)`     | Store a Component in PDC                   |
| `.meta(consumer)`          | Modify raw ItemMeta                        |
| `.build()`                 | Creates the ItemStack                      |

### Skull Heads

```java
// Player head
ItemBuilder.of(Material.PLAYER_HEAD).skull(player).build();

// Custom texture via base64
ItemBuilder.of(Material.PLAYER_HEAD)
    .skullTexture("eyJ0ZXh0dXJlcyI6ey...")
    .build();

// Custom texture via URL
ItemBuilder.of(Material.PLAYER_HEAD)
    .skullUrl("https://textures.minecraft.net/texture/abc123")
    .build();
```

---

## DataComponents

Paper 1.20.6+ data component access:

```java
DataComponents.maxStackSize(item, 99);
DataComponents.rarity(item, ItemRarity.EPIC);
DataComponents.enchantGlint(item, true);
DataComponents.fireResistant(item, true);
DataComponents.hideTooltip(item, true);
DataComponents.unbreakable(item, true);
```

---

## ItemSerializer

Convert items to/from Base64 for storage:

```java
String encoded = ItemSerializer.toBase64(itemStack);
ItemStack decoded = ItemSerializer.fromBase64(encoded);
```

Also works with arrays:

```java
String encoded = ItemSerializer.arrayToBase64(itemArray);
ItemStack[] decoded = ItemSerializer.arrayFromBase64(encoded);
```

---

## PotionSerializer

Serialize/deserialize potion effects:

```java
String json = PotionSerializer.serialize(potionEffect);
PotionEffect effect = PotionSerializer.deserialize(json);

// Lists
String json = PotionSerializer.serializeList(effects);
List<PotionEffect> effects = PotionSerializer.deserializeList(json);
```

---

## Layout

Helper for generating slot lists:

```java
List<Integer> slots = Layout.rectangle(startRow, startCol, endRow, endCol);
List<Integer> border = Layout.border(rows);
```
