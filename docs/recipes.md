# Recipes

`dev.oum.oumlib.inventory.recipe` · Paper

---

## RecipeDSL

Register custom recipes with a fluent API. All recipes go through `OumLib.recipes()` and get cleaned up on shutdown.

### Shaped

```java
RecipeDSL.shaped("diamond_helmet_custom", Material.DIAMOND_HELMET)
    .pattern(
        "DDD",
        "D D",
        "   "
    )
    .ingredient('D', Material.DIAMOND)
    .register();
```

With a custom result item:

```java
ItemStack result = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("<aqua>Ice Blade")
    .enchant(Enchantment.SHARPNESS, 3)
    .build();

RecipeDSL.shaped("ice_blade", result)
    .pattern(
        " D ",
        " D ",
        " S "
    )
    .ingredient('D', Material.DIAMOND)
    .ingredient('S', Material.STICK)
    .register();
```

### Shapeless

```java
RecipeDSL.shapeless("golden_apple_easy", Material.GOLDEN_APPLE)
    .ingredient(Material.APPLE)
    .ingredient(Material.GOLD_INGOT, 4)
    .register();
```

### Smelting

```java
RecipeDSL.smelting("custom_iron", Material.IRON_INGOT)
    .input(Material.RAW_IRON)
    .experience(1.0f)
    .cookingTime(100) // ticks
    .register();
```

### Blasting

```java
RecipeDSL.blasting("fast_iron", Material.IRON_INGOT)
    .input(Material.RAW_IRON)
    .cookingTime(50)
    .register();
```

### Smoking

```java
RecipeDSL.smoking("cooked_beef_fast", Material.COOKED_BEEF)
    .input(Material.BEEF)
    .register();
```

### Campfire

```java
RecipeDSL.campfire("campfire_cod", Material.COOKED_COD)
    .input(Material.COD)
    .cookingTime(200)
    .register();
```

### Smithing

```java
RecipeDSL.smithing("netherite_sword_custom", customSword)
    .template(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
    .base(Material.DIAMOND_SWORD)
    .addition(Material.NETHERITE_INGOT)
    .register();
```

### Stonecutting

```java
RecipeDSL.stonecutting("stone_bricks_cut", Material.STONE_BRICKS)
    .input(Material.STONE)
    .register();
```

---

## Unregister

All recipes registered through `RecipeDSL` are tracked. They get unregistered when you call `OumLib.shutdown()`.

You can also unregister manually:

```java
OumLib.recipes().unregisterAll();
```
