# OumLib

[![](https://img.shields.io/github/v/release/sun-mc-dev/oumlib?color=orange&style=for-the-badge)](https://github.com/sun-mc-dev/oumlib/releases)
[![](https://img.shields.io/jitpack/v/github/sun-mc-dev/oumlib?color=yellow&style=for-the-badge)](https://jitpack.io/#sun-mc-dev/oumlib)
[![](https://img.shields.io/badge/Java-21+-orange?style=for-the-badge&logo=openjdk)](https://adoptium.net/)
[![](https://img.shields.io/badge/Folia-Compatible-gold?style=for-the-badge)](https://github.com/PaperMC/Folia)

A utility library for Paper and Velocity plugins. Shade it into your jar, call `OumLib.init(this)`, and you get commands, menus, configs, scheduling, cooldowns, events, regions, holograms, recipes, database access, and more — all with a fluent API and zero external dependencies at runtime.

Built on Java 21 virtual threads. Works on Paper, Folia, and Velocity.

---

## Docs

| Module               | What it does                                                         | Link                                      |
|:---------------------|:---------------------------------------------------------------------|:------------------------------------------|
| **Setup**            | Init lifecycle, shading, platform detection                          | [setup.md](docs/setup.md)                 |
| **Commands**         | Brigadier command builder with typed args, cooldowns, subcommands    | [commands.md](docs/commands.md)           |
| **Configuration**    | Record-based YAML configs with auto-reload                           | [configuration.md](docs/configuration.md) |
| **Menus & Items**    | Chest GUIs, paginated menus, ItemBuilder, DataComponents             | [inventories.md](docs/inventories.md)     |
| **Recipes**          | Shaped, shapeless, cooking, smithing, stonecutting DSL               | [recipes.md](docs/recipes.md)             |
| **Scheduler**        | Sync/async/virtual tasks, Promise, TaskChain, Countdown, Folia-aware | [scheduler.md](docs/scheduler.md)         |
| **Events**           | Functional event listeners with filters, expiry, one-shot            | [events.md](docs/events.md)               |
| **Cooldowns**        | CooldownManager, RateLimiter, persistent stores                      | [cooldowns.md](docs/cooldowns.md)         |
| **Text**             | MiniMessage helpers, placeholders, localization, text input          | [text.md](docs/text.md)                   |
| **PDC**              | Type-safe persistent data, DataKey, PdcModel records, PdcTree        | [pdc.md](docs/pdc.md)                     |
| **Metadata**         | In-memory volatile data with TTL auto-cleanup                        | [metadata.md](docs/metadata.md)           |
| **Holograms**        | Packet-based virtual displays with click handling                    | [holograms.md](docs/holograms.md)         |
| **Display Entities** | DisplayBuilder for text/item/block displays                          | [entities.md](docs/entities.md)           |
| **Regions**          | Cuboid, cylinder, sphere, polygon regions with enter/leave tracking  | [regions.md](docs/regions.md)             |
| **Math**             | Vector2D/3D, Volume3D, Noise, Easing, FastMath, MathEval             | [math.md](docs/math.md)                   |
| **Effects**          | Particle effects — lines, circles, helices, bezier curves            | [effects.md](docs/effects.md)             |
| **Database**         | Async SQLite/MySQL via HikariCP                                      | [database.md](docs/database.md)           |
| **Bridges**          | Vault, Nexo, ItemsAdder, MMOItems hooks                              | [bridges.md](docs/bridges.md)             |
| **Utilities**        | Duration parsing, location serialization, formatting                 | [utilities.md](docs/utilities.md)         |

---

## Installation

### Maven

```xml
<repository>
   <id>jitpack.io</id>
   <url>https://jitpack.io</url>
</repository>
```

```xml
<dependency>
    <groupId>com.github.sun-mc-dev.oumlib</groupId>
    <artifactId>oumlib-core</artifactId>
    <version>VERSION</version>
    <scope>compile</scope>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.sun-mc-dev.oumlib:oumlib-core:VERSION")
}
```

### Shading

You **must** shade and relocate OumLib into your plugin jar. This prevents version conflicts when multiple plugins use different OumLib versions on the same server.

**Maven:**
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.6.2</version>
            <configuration>
                <createDependencyReducedPom>false</createDependencyReducedPom>
                <relocations>
                    <relocation>
                        <pattern>dev.oum.oumlib</pattern>
                        <shadedPattern>your.plugin.package.libs.oumlib</shadedPattern>
                    </relocation>
                </relocations>
                <filters>
                    <filter>
                        <artifact>*:*</artifact>
                        <excludes>
                            <exclude>META-INF/*.SF</exclude>
                            <exclude>META-INF/*.DSA</exclude>
                            <exclude>META-INF/*.RSA</exclude>
                            <exclude>META-INF/MANIFEST.MF</exclude>
                        </excludes>
                    </filter>
                </filters>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Gradle (Shadow):**
```kotlin
plugins {
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

tasks.shadowJar {
    relocate("dev.oum.oumlib", "your.plugin.package.libs.oumlib")
}
```

---

## Quick Start

A small plugin that loads config, registers a command, opens a shop GUI, and handles purchases:

```java
public final class ShopPlugin extends JavaPlugin {
    private ConfigManager<ShopConfig> config;
    private Database db;

    @Override
    public void onEnable() {
        OumLib.init(this);

        config = ConfigManager.of(ShopConfig.class, "shop.yml",
            () -> new ShopConfig("<gold>Super Star</gold>", 100)
        ).enableAutoReload();

        db = Database.sqlite(new File(getDataFolder(), "data.db"));
        db.executeUpdate("CREATE TABLE IF NOT EXISTS economy (uuid TEXT PRIMARY KEY, balance INT)");

        CommandBuilder.create("shop")
            .description("Opens the shop")
            .executes(ctx -> {
                Player player = ctx.playerOrThrow();
                db.executeQuery("SELECT balance FROM economy WHERE uuid = ?",
                        player.getUniqueId().toString())
                    .thenAcceptSync(rows -> {
                        int balance = rows.isEmpty() ? 500 : (int) rows.getFirst().get("balance");
                        openShop(player, balance);
                    });
            })
            .register();
    }

    private void openShop(Player player, int balance) {
        ChestMenu.builder()
            .title("<dark_gray>Shop | " + balance + " coins</dark_gray>")
            .rows(3)
            .pattern(
                "#########",
                "#   P   #",
                "#########"
            )
            .bind('#', ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
            .bind('P', ItemBuilder.of(Material.NETHER_STAR)
                .name(config.get().itemTitle())
                .lore("<yellow>Price: " + config.get().itemPrice() + "</yellow>")
                .build())
            .onClick('P', click -> {
                Player p = click.player();
                if (balance < config.get().itemPrice()) {
                    Text.send(p, "<red>Not enough coins!");
                    p.closeInventory();
                    return;
                }
                int newBal = balance - config.get().itemPrice();
                db.executeUpdate(
                    "INSERT INTO economy VALUES (?,?) ON CONFLICT(uuid) DO UPDATE SET balance=?",
                    p.getUniqueId().toString(), newBal, newBal);
                Text.send(p, "<green>Purchased!");
                p.closeInventory();
            })
            .build()
            .open(player);
    }

    @Override
    public void onDisable() {
        if (db != null) db.close();
        OumLib.shutdown();
    }
}

public record ShopConfig(String itemTitle, int itemPrice) implements ConfigSection {}
```
