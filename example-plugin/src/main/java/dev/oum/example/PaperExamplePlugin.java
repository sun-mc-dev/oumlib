package dev.oum.example;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.cooldown.CooldownFormatter;
import dev.oum.oumlib.cooldown.CooldownManager;
import dev.oum.oumlib.cooldown.RateLimiter;
import dev.oum.oumlib.entity.display.DisplayAnimation;
import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.entity.hologram.Hologram;
import dev.oum.oumlib.inventory.ItemBuilder;
import dev.oum.oumlib.pdc.metadata.VolatileData;
import dev.oum.oumlib.pdc.DataKey;
import dev.oum.oumlib.pdc.PDC;
import dev.oum.oumlib.pdc.PdcProperty;
import dev.oum.oumlib.inventory.recipe.RecipeDSL;
import dev.oum.oumlib.math.region.CuboidRegion;
import dev.oum.oumlib.math.region.event.PlayerEnterRegionEvent;
import dev.oum.oumlib.math.region.event.PlayerLeaveRegionEvent;
import dev.oum.oumlib.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class PaperExamplePlugin extends JavaPlugin {

    public record WeaponMetadata(int level, double damageMultiplier, List<String> specialTraits, UUID crafter) {}

    private final CooldownManager<UUID> abilityCooldowns = CooldownManager.<UUID>create()
            .defaultFormatter(CooldownFormatter.PRECISE);

    private final RateLimiter<UUID> chatRateLimiter = RateLimiter.slidingWindow(3, Duration.ofSeconds(2));

    private static final DataKey<String, String> ITEM_TIER = DataKey.string("item_tier");
    private static final DataKey<Integer, Integer> ITEM_POWER = DataKey.integer("item_power");
    private static final DataKey<Byte, Boolean> IN_COMBAT = DataKey.bool("in_combat");

    @Override
    public void onEnable() {
        OumLib.init(this);
        ExampleAnnouncer.initialize();

        registerCustomRecipes();
        setupSpawnRegion();
        setupSpawnHologram();

        Events.listen(PlayerJoinEvent.class, event -> {
            event.joinMessage(null);
            Player player = event.getPlayer();
            ExampleAnnouncer.handlePlayerJoin(player.getName());

            PdcProperty<Integer, Integer> mana = PDC.property(player, ITEM_POWER, 100);
            mana.observe((oldVal, newVal) -> {
                Text.actionBar(player, "<aqua>Mana synced: " + newVal + "</aqua>");
            });

            PDC.flags(player).add("first_login", "verified");
        });

        Events.listen(PlayerEnterRegionEvent.class, event -> {
            Text.actionBar(event.getPlayer(), "<green>Entered protected area: " + event.getRegionId());
        });

        Events.listen(PlayerLeaveRegionEvent.class, event -> {
            Text.actionBar(event.getPlayer(), "<red>Left protected area: " + event.getRegionId());
        });

        Events.listen(PlayerInteractEvent.class, event -> {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();
            if (item == null || item.getType() != Material.BLAZE_ROD) return;

            if (abilityCooldowns.isOnCooldown(player.getUniqueId())) {
                String remaining = abilityCooldowns.formatRemaining(player.getUniqueId());
                Text.actionBar(player, "<red>Ability on cooldown! Wait " + remaining);
                return;
            }

            abilityCooldowns.apply(player.getUniqueId(), Duration.ofSeconds(5));
            VolatileData.set(player, IN_COMBAT, true, Duration.ofSeconds(10));
            Text.actionBar(player, "<gold>Cast Flame Surge! (In combat for 10s)");

            PDC.set(item, ITEM_POWER, PDC.getOrDefault(item, ITEM_POWER, 0) + 1);
            PDC.flags((PersistentDataHolder) item).toggle("empowered");
        });
    }

    private void registerCustomRecipes() {
        WeaponMetadata meta = new WeaponMetadata(10, 2.5, List.of("ignite", "vampirism"), UUID.randomUUID());

        ItemStack superSword = ItemBuilder.of(Material.DIAMOND_SWORD)
                .name("<gradient:#ff5555:#ffff55>Infernal Blade</gradient>")
                .rarity(ItemRarity.EPIC)
                .maxStackSize(1)
                .fireResistant(true)
                .pdc(meta)
                .pdc(ITEM_TIER, "LEGENDARY")
                .pdc(ITEM_POWER, 100)
                .build();

        RecipeDSL.shaped("super_sword", superSword)
                .shape(" D ", " D ", " S ")
                .set('D', Material.DIAMOND_BLOCK)
                .set('S', Material.NETHERITE_INGOT)
                .register();

        RecipeDSL.shapeless("compressed_gold", new ItemStack(Material.RAW_GOLD_BLOCK))
                .add(Material.RAW_GOLD, 9)
                .register();
    }

    private void setupSpawnRegion() {
        World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
        if (world == null) return;

        CuboidRegion spawnCuboid = new CuboidRegion(world.getName(), -20, 50, -20, 20, 100, 20);
        OumLib.regions().register("spawn_safezone", spawnCuboid);
    }

    private void setupSpawnHologram() {
        World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
        if (world == null) return;

        Location holoLoc = new Location(world, 0.5, 70, 0.5);
        Hologram holo = Hologram.builder(holoLoc)
                .line("<gradient:#ff5555:#ffff55><bold>OumLib Network</bold></gradient>")
                .dynamicLine(player -> Text.parse(
                        "<gray>Welcome, <yellow>" + player.getName() + "</yellow>! Ping: <green>" + player.getPing() + "ms</green>"
                ))
                .itemLine(Material.NETHER_STAR)
                .line("<dark_gray>--------------------</dark_gray>")
                .line("<yellow>Click to open menu!</yellow>")
                .onClick((player, h, lineIndex, clickType) -> {
                    PaperAnnouncerHelper.openMenu(player);
                })
                .buildAndRegister();

        if (holo.getLine(2) != null) {
            DisplayAnimation.bobbing(Objects.requireNonNull(holo.getLine(2)).getDisplay(), 0.15, 40);
        }
    }

    @Override
    public void onDisable() {
        OumLib.shutdown();
    }
}
