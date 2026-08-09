# Bridges

`dev.oum.oumlib.bridge` · Paper

---

## What Are Bridges?

Bridges are wrappers around third-party plugins. They let you interact with Vault, ItemsAdder, Nexo, MMOItems, etc. through a unified API. If the plugin isn't installed, the bridge just returns defaults — no crashes.

---

## Economy

Works with Vault and PlayerPoints.

```java
EconomyBridge eco = EconomyBridge.detect();

double balance = eco.getBalance(player);
boolean success = eco.withdraw(player, 100.0);
eco.deposit(player, 50.0);
boolean hasEnough = eco.has(player, 200.0);
```

If neither Vault nor PlayerPoints is installed, all operations return safe defaults (balance = 0, withdraw = false, etc.).

---

## Items

Get items from custom item plugins using a single API:

```java
ItemStack item = ItemBridge.getItem("nexo:ruby_sword");
ItemStack item = ItemBridge.getItem("itemsadder:custom_gem");
ItemStack item = ItemBridge.getItem("mmoitems:SWORD:FIRE_BLADE");
ItemStack item = ItemBridge.getItem("oraxen:amethyst_pickaxe");
ItemStack item = ItemBridge.getItem("mythicmobs:SkeletonKingSword");
ItemStack item = ItemBridge.getItem("headdb:12345");
ItemStack item = ItemBridge.getItem("minecraft:diamond_sword");
```

The prefix before the `:` tells OumLib which provider to use. Supported providers:

| Prefix        | Plugin                                |
|:--------------|:--------------------------------------|
| `nexo:`       | Nexo                                  |
| `itemsadder:` | ItemsAdder                            |
| `mmoitems:`   | MMOItems (format: `mmoitems:TYPE:ID`) |
| `oraxen:`     | Oraxen                                |
| `mythicmobs:` | MythicMobs                            |
| `headdb:`     | HeadDatabase                          |
| `minecraft:`  | Vanilla Minecraft                     |

If the plugin isn't installed, `getItem()` returns `null`.

---

## Permissions

Type-safe permission builder for Paper:

```java
Permission perm = Permission.builder("myplugin.admin")
    .description("Admin access")
    .defaultValue(PermissionDefault.OP)
    .child("myplugin.admin.ban", true)
    .child("myplugin.admin.kick", true)
    .build();
```

Use with commands:

```java
CommandBuilder.create("ban")
    .permission(perm)
    .executes(ctx -> { /* ... */ })
    .register();
```

### Permission Bridge

Check and modify permissions at runtime:

```java
PermissionBridge.has(player, "myplugin.vip");
PermissionBridge.addPermission(player, "myplugin.fly");
PermissionBridge.removePermission(player, "myplugin.fly");

// Group operations (requires LuckPerms or similar)
PermissionBridge.getGroup(player);
PermissionBridge.setGroup(player, "vip");
PermissionBridge.addGroup(player, "donor");
PermissionBridge.removeGroup(player, "donor");
```

---

## Statistics Bridge

Read Minecraft statistics:

```java
int blocksMined = StatisticsBridge.get(player, Statistic.MINE_BLOCK, Material.DIAMOND_ORE);
int kills = StatisticsBridge.get(player, Statistic.KILL_ENTITY, EntityType.ZOMBIE);
int deaths = StatisticsBridge.get(player, Statistic.DEATHS);
int playTime = StatisticsBridge.get(player, Statistic.PLAY_ONE_MINUTE); // in ticks
```
