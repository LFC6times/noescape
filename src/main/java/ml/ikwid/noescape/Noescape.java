package ml.ikwid.noescape;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.ikwid.noescape.config.SimpleConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class Noescape implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("noescape");
    private MinecraftServer minecraftServer;
    public static int x1, z1, x2, z2;
    private int ticks = 0;
    public static boolean justKill;
    public static boolean vanillaWB;
    public static double worldBorderDmg;
    public static boolean allowCreative;

    public static SimpleConfig CONFIG;
    @Override
    public void onInitialize() {
        CONFIG = SimpleConfig.of("noescape").provider(this::provider).request();

        if(CONFIG.get("x1") == null || CONFIG.get("z1") == null || CONFIG.get("x2") == null || CONFIG.get("z2") == null) {
            LOGGER.info("Not all options are filled in, disabling noescape. Fill in values for x1, z1, x2, and z2 in config.properties in this format: \"x1=10\"");
        } else {
            x1 = Math.min(CONFIG.getOrDefault("x1", -29999984), CONFIG.getOrDefault("x2", 29999984));
            z1 = Math.min(CONFIG.getOrDefault("z1", -29999984), CONFIG.getOrDefault("z2", 29999984));
            x2 = Math.max(CONFIG.getOrDefault("x1", -29999984), CONFIG.getOrDefault("x2", 29999984));
            z2 = Math.max(CONFIG.getOrDefault("x1", -29999984), CONFIG.getOrDefault("x2", 29999984));

            // Sets the coordinate values for the border
            justKill = CONFIG.getOrDefault("justKill", false);
            vanillaWB = CONFIG.getOrDefault("vanillaWorldBorder", true);
            worldBorderDmg = CONFIG.getOrDefault("worldBorderDmg", 0.2);
            allowCreative = CONFIG.getOrDefault("allowCreative", true);
        }

        ServerTickEvents.END_WORLD_TICK.register((endTick) -> {
            if(++ticks % 20 == 0) {
                if(!vanillaWB) {
                    damageViolatorsNoVanilla();
                } else {
                    damageViolatorsVanilla();
                }
                ticks = 0;
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> this.minecraftServer = server);
    }
    private String provider(String filename) {
        return """
                # Default config
                # Boundaries of Border defined as x1, z1, x2, z2. Note that the vanilla 5 block buffer is not included, add it yourself if you wish.

                # justKill being set to true will kill the player as if ./kill was used, if false will deal 2^31 - 1 damage if vanillaWorldBorder is false.

                # vanillaWorldBorder (requires justKill to be false) will act like a vanilla world border in terms of dmg.

                # worldBorderDmg (requires vanillaWorldBorder to be true) sets the damage per second per block outside of the border, if left unset/empty defaults to vanilla values
                
                # allowCreative allows creative players to escape (if true)
               """;
    }

    private void damageViolatorsNoVanilla() {
        List<ServerPlayerEntity> serverPlayerEntities = minecraftServer.getPlayerManager().getPlayerList();
        for(ServerPlayerEntity serverPlayerEntity : serverPlayerEntities) {
            double x = serverPlayerEntity.getX();
            double z = serverPlayerEntity.getZ();

            if(allowCreative && serverPlayerEntity.isCreative()) {
                continue;
            }

            if(x < x1 || x > x2 || z < z1 || z > z2) {
                if(justKill) {
                    serverPlayerEntity.kill();
                } else {
                    serverPlayerEntity.damage(DamageSource.IN_WALL, 2147483647); // 2^31 - 1
                }
            }
        }
    }

    private void damageViolatorsVanilla() {
        List<ServerPlayerEntity> serverPlayerEntities = minecraftServer.getPlayerManager().getPlayerList();
        for(ServerPlayerEntity serverPlayerEntity : serverPlayerEntities) {
            double x = serverPlayerEntity.getX();
            double z = serverPlayerEntity.getZ();

            if(allowCreative && serverPlayerEntity.isCreative()) {
                continue;
            }

            if(x < x1 || x > x2 || z < z1 || z > z2) {
                if(x < x1 || x > x2) {
                    if(z < z1 || z > z2) {
                        serverPlayerEntity.damage(DamageSource.IN_WALL, (float) (distanceFormula(x, z, (x < x1 ? x1 : x2), (z < z1 ? z1 : z2)) * worldBorderDmg));
                    }
                }
            }
        }
    }

    public static double distanceFormula(double x1, double z1, double x2, double z2) {
        return Math.sqrt((x2 - x1) * (z2 - z1) + (z2 - z1) * (z2 - z1));
    }

    public static int reloadConfig(CommandContext<ServerCommandSource> context) {
        if(CONFIG.get("x1") == null || CONFIG.get("z1") == null || CONFIG.get("x2") == null || CONFIG.get("z2") == null) {
            LOGGER.info("Not all options are filled in, disabling noescape. Fill in values for x1, z1, x2, and z2 in config.properties in this format: \"x1=10\"");
        } else {
            x1 = Math.min(CONFIG.getOrDefault("x1", -29999984), CONFIG.getOrDefault("x2", 29999984));
            z1 = Math.min(CONFIG.getOrDefault("z1", -29999984), CONFIG.getOrDefault("z2", 29999984));
            x2 = Math.max(CONFIG.getOrDefault("x1", -29999984), CONFIG.getOrDefault("x2", 29999984));
            z2 = Math.max(CONFIG.getOrDefault("x1", -29999984), CONFIG.getOrDefault("x2", 29999984));

            // Sets the coordinate values for the border
            justKill = CONFIG.getOrDefault("justKill", false);
            vanillaWB = CONFIG.getOrDefault("vanillaWorldBorder", false);
            worldBorderDmg = CONFIG.getOrDefault("worldBorderDmg", 0.2);
        }
        return 1;
    }

    public static int create(CommandContext<ServerCommandSource> context) {
        x1 = getInteger(context, "x1");
        z1 = getInteger(context, "z1");
        x2 = getInteger(context, "x2");
        z2 = getInteger(context, "z2");

        try {
            CONFIG.writeToConfig(generateConfig());
        } catch(IOException ignored) {

        }

        return 1;
    }

    public static int remove(CommandContext<ServerCommandSource> context) {
        CONFIG.delete();
        return 1;
    }

    public static int toggle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String arg = StringArgumentType.getString(context, "setting");
        switch (arg) {
            case "justKill" -> {
                justKill = !justKill;
                context.getSource().getPlayer().sendMessage(Text.of("toggled justKill to " + justKill), false);
            }
            case "vanillaWorldBorder" -> {
                vanillaWB = !vanillaWB;
                context.getSource().getPlayer().sendMessage(Text.of("toggled vanillaWorldBorder to " + vanillaWB), false);
            }
            case "allowCreative" -> {
                allowCreative = !allowCreative;
                context.getSource().getPlayer().sendMessage(Text.of("toggled allowCreative to " + allowCreative), false);
            }
            default -> context.getSource().getPlayer().sendMessage(Text.of("that's not a setting"), false);
        }

        try {
            CONFIG.writeToConfig(generateConfig());
        } catch(IOException ignored) {

        }

        return 1;
    }

    public static int setWBDmg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        worldBorderDmg = DoubleArgumentType.getDouble(context, "dmg");
        context.getSource().getPlayer().sendMessage(Text.of("set the world border dmg to " + worldBorderDmg), false);

        try {
            CONFIG.writeToConfig(generateConfig());
        } catch(IOException ignored) {

        }

        return 1;
    }

    public static String generateConfig() {
        return String.format("""
                 # Default config
                 # Boundaries of Border defined as x1, z1, x2, z2. Note that the vanilla 5 block buffer is not included, add it yourself if you wish.
                 x1=%d
                 z1=%d
                 x2=%d
                 z2=%d
                 
                 # justKill being set to true will kill the player as if ./kill was used, if false will deal 2^31 - 1 damage if vanillaWorldBorder is false.
                 justKill=%s
                 # vanillaWorldBorder (requires justKill to be false) will act like a vanilla world border in terms of dmg.
                 vanillaWorldBorder=%s
                 # worldBorderDmg (requires vanillaWorldBorder to be true) sets the damage per second per block outside of the border, if left unset/empty defaults to vanilla values
                 worldBorderDmg=%f
                 # allowCreative allows creative players to escape (if true)
                 allowCreative=%s
                                
                """, x1, z1, x2, z2, justKill, vanillaWB, worldBorderDmg, allowCreative);
    }
}
