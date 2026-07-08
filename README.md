# OumLib

[![](https://img.shields.io/github/v/release/sun-mc-dev/oumlib?color=orange&style=for-the-badge)](https://github.com/sun-mc-dev/oumlib/releases)
[![](https://img.shields.io/jitpack/v/github/sun-mc-dev/oumlib?color=yellow&style=for-the-badge)](https://jitpack.io/#sun-mc-dev/oumlib)
[![](https://img.shields.io/badge/Java-21+-orange?style=for-the-badge&logo=openjdk)](https://adoptium.net/)
[![](https://img.shields.io/badge/Folia-Compatible-gold?style=for-the-badge)](https://github.com/PaperMC/Folia)

OumLib is a lightweight, utility-centric library designed for Minecraft servers (Paper/Spigot) and proxy networks (Velocity). Built around Java 21 virtual threads, it provides modern, type-safe, and thread-aware abstractions to eliminate boilerplate code.

OumLib is designed to be shaded and relocated directly into your plugin JAR.

---

## Quick Navigation

| Module               | Description                                                  | Documentation                              |
|:---------------------|:-------------------------------------------------------------|:-------------------------------------------|
| **Setup & Platform** | Shaded setup lifecycle and platform detection utilities      | **[Setup Guide](docs/setup.md)**           |
| **Commands**         | Fluent Brigadier command builders with cooldown support      | **[Commands](docs/commands.md)**           |
| **Configuration**    | Automatic-reloading record configurations with comments      | **[Configuration](docs/configuration.md)** |
| **Menus & GUIs**     | Easy chest-layouts, button binding, paginated interfaces     | **[Inventories](docs/inventories.md)**     |
| **Scheduler**        | Virtual-thread loops, TaskGroups, and Folia adaptors         | **[Scheduler](docs/scheduler.md)**         |
| **Events**           | Chainable context-aware event registers with filters         | **[Events](docs/events.md)**               |
| **Math Utilities**   | FastMath shortcuts, Vector3D, Volume3D, MathEval expressions | **[Math](docs/math.md)**                   |
| **Visual Effects**   | Particle pathways: bezier curves, lines, helices             | **[Visual Effects](docs/effects.md)**      |
| **Display Entities** | Fluent transforms and DisplayBuilder controls                | **[Display Entities](docs/entities.md)**   |
| **Text & PAPI**      | Kyori MiniMessage presets, placeholder hooks                 | **[Text & Placeholders](docs/text.md)**    |
| **Database**         | Asynchronous database connectors for SQLite and MySQL        | **[Database](docs/database.md)**           |
| **Plugin Bridges**   | Auto-hooks for Economy, Permissions, Nexo and CustomItems    | **[Bridges](docs/bridges.md)**             |
| **General Utils**    | PDC wrappers, duration parses, location serializing          | **[Utilities](docs/utilities.md)**         |
| **Web Hookers**      | Asynchronous HTTP requests and Discord webhook builders      | **[Web & Discord](docs/web.md)**           |

---

## Installation

Declare the JitPack repository and OumLib dependency in your `pom.xml`.

### 1. Add Repository
```xml
<repository>
   <id>jitpack.io</id>
   <url>https://jitpack.io</url>
</repository>
```

### 2. Add Dependency
```xml
<dependency>
    <groupId>com.github.sun-mc-dev.oumlib</groupId>
    <artifactId>oumlib-core</artifactId>
    <version>VERSION</version>
    <scope>compile</scope>
</dependency>
```

### 3. Shading & Relocation
You must relocate OumLib inside your package space to prevent classpath conflicts with other plugins running different versions of OumLib on the same server.

Add this configured `maven-shade-plugin` to your `pom.xml`:

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

---

## Quick Start Example

Here is a real-world scenario showing how to load a player profile asynchronously from a SQLite database, register a command to open a GUI shop, and play custom leveling sound/particle effects upon purchase:

```java
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.command.Commands;
import dev.oum.oumlib.config.ConfigManager;
import dev.oum.oumlib.config.ConfigSection;
import dev.oum.oumlib.database.Database;
import dev.oum.oumlib.effect.Effects;
import dev.oum.oumlib.inventory.ChestMenu;
import dev.oum.oumlib.inventory.ItemBuilder;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.text.Text;
import dev.oum.oumlib.util.Permission;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public record ShopConfig(String itemTitle, int itemPrice) implements ConfigSection {}

public final class ProfileShopPlugin extends JavaPlugin {
    private ConfigManager<ShopConfig> config;
    private Database db;

    @Override
    public void onEnable() {
        OumLib.init(this);
        
        config = ConfigManager.of(ShopConfig.class, "shop.yml", 
            () -> new ShopConfig("<gold>Super Star</gold>", 100)
        ).enableAutoReload();

        db = Database.sqlite(new File(getDataFolder(), "profiles.db"));
        db.executeUpdate("CREATE TABLE IF NOT EXISTS economy (uuid VARCHAR(36) PRIMARY KEY, balance INT)");

        Commands.literal("shop")
            .permission(Permission.builder("myplugin.shop").build())
            .executes(context -> {
                if (!context.isPlayer()) {
                    return;
                }
                Player player = context.playerOrThrow();

                db.executeQuery("SELECT balance FROM economy WHERE uuid = ?", player.getUniqueId().toString())
                    .thenAcceptSync(rows -> {
                        int balance = rows.isEmpty() ? 500 : (int) rows.getFirst().get("balance");
                        openShopMenu(player, balance);
                    });
            }).register();
    }

    private void openShopMenu(Player player, int balance) {
        ChestMenu.builder()
            .title("<dark_gray>Server Shop | Balance: " + balance + "</dark_gray>")
            .rows(3)
            .pattern(
                "#########",
                "#   P   #",
                "#########"
            )
            .bind('#', ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
            .bind('P', ItemBuilder.of(Material.NETHER_STAR).name(config.get().itemTitle()).lore("<yellow>Price: " + config.get().itemPrice() + "</yellow>").build())
            .onClick('P', click -> {
                if (balance < config.get().itemPrice()) {
                    Text.send(click.player(), "<red>Insufficient balance!</red>");
                    click.player().closeInventory();
                    return;
                }

                int newBalance = balance - config.get().itemPrice();
                db.executeUpdate("INSERT INTO economy (uuid, balance) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET balance = ?", 
                    click.player().getUniqueId().toString(), newBalance, newBalance);

                Text.send(click.player(), "<green>Purchased successfully!</green>");
                click.player().closeInventory();

                Effects.sound(Sound.ENTITY_PLAYER_LEVELUP).volume(1.0F).pitch(1.2F).play(click.player());
                Effects.particle(Particle.HAPPY_VILLAGER).count(15).offset(0.5, 0.5, 0.5).spawn(click.player().getLocation());
            })
            .build()
            .open(player);
    }

    @Override
    public void onDisable() {
        if (db != null) {
            db.close();
        }
        OumLib.shutdown();
    }
}
```
