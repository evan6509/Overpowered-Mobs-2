package com.overpoweredmobs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.OverpoweredMobsLogger;
import com.overpoweredmobs.BloodMoonManager;
import com.overpoweredmobs.CavalryHelper;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Set;

public class OPMCommand {
    private static final String[] STATUS_ATTRS = {"health", "damage", "speed", "armor", "followRange", "xp"};
    private static final Set<String> SETTABLE_ATTRS = Set.of(
        "health", "damage", "speed", "armor", "followRange", "xp", "spawnchance"
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("opm")
            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
            .then(Commands.literal("set")
                .then(Commands.argument("mob", StringArgumentType.word())
                    .then(Commands.argument("attribute", StringArgumentType.word())
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 100.0))
                            .executes(OPMCommand::executeSet)))))
            .then(Commands.literal("reload")
                .executes(OPMCommand::executeReload))
            .then(Commands.literal("status")
                .executes(OPMCommand::executeStatus))
            .then(Commands.literal("reset")
                .executes(OPMCommand::executeReset))
            .then(Commands.literal("test")
                .executes(OPMCommand::executeTest))
            .then(Commands.literal("bloodmoon")
                .executes(OPMCommand::executeBloodMoon))
            .then(Commands.literal("cavalry")
                .then(Commands.argument("rider", StringArgumentType.word())
                    .then(Commands.argument("mount", StringArgumentType.word())
                        .executes(OPMCommand::executeCavalry))))
        );
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx) {
        String mobStr = StringArgumentType.getString(ctx, "mob");
        String attr = StringArgumentType.getString(ctx, "attribute");
        double value = DoubleArgumentType.getDouble(ctx, "value");

        if (!SETTABLE_ATTRS.contains(attr)) {
            ctx.getSource().sendFailure(Component.literal(
                "Unknown attribute: " + attr + ". Valid attributes: " + String.join(", ", SETTABLE_ATTRS)
            ));
            return 0;
        }

        if (attr.equals("spawnchance") && value > 1.0) {
            ctx.getSource().sendFailure(Component.literal("spawnchance must be between 0.0 and 1.0"));
            return 0;
        }
        if (!attr.equals("spawnchance") && value < 0.1) {
            ctx.getSource().sendFailure(Component.literal("Multipliers must be at least 0.1"));
            return 0;
        }

        EntityType<?> type = findEntityType(mobStr);
        if (type == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown entity type: " + mobStr));
            return 0;
        }

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        OverpoweredConfig.MobConfig cfg = config.getFor(type).copy();
        cfg.set(attr, value);
        config.setFor(type, cfg);
        config.save();

        ctx.getSource().sendSuccess(() ->
            Component.literal("Set " + mobStr + " " + attr + " to " + value), true);
        return 1;
    }

    private static int executeReload(CommandContext<CommandSourceStack> ctx) {
        OverpoweredMobs.loadConfig();
        ctx.getSource().sendSuccess(() ->
            Component.literal("Config reloaded"), true);
        return 1;
    }

    private static int executeStatus(CommandContext<CommandSourceStack> ctx) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        ctx.getSource().sendSuccess(() ->
            Component.literal("=== Default multipliers ==="), false);

        OverpoweredConfig.MobConfig defaults = config.getDefaults();
        for (String attr : STATUS_ATTRS) {
            double val = defaults.get(attr);
            ctx.getSource().sendSuccess(() ->
                Component.literal("  " + attr + ": " + val), false);
        }

        ctx.getSource().sendSuccess(() ->
            Component.literal("=== Per-mob overrides ==="), false);

        for (Map.Entry<String, OverpoweredConfig.MobConfig> entry : config.getMobs().entrySet()) {
            String key = entry.getKey();
            OverpoweredConfig.MobConfig mc = entry.getValue();
            ctx.getSource().sendSuccess(() ->
                Component.literal("  " + key + ":"), false);
            for (String attr : STATUS_ATTRS) {
                double val = mc.get(attr);
                ctx.getSource().sendSuccess(() ->
                    Component.literal("    " + attr + ": " + val), false);
            }
        }
        return 1;
    }

    private static int executeReset(CommandContext<CommandSourceStack> ctx) {
        OverpoweredConfig.reset();
        ctx.getSource().sendSuccess(() ->
            Component.literal("Config reset to defaults"), true);
        return 1;
    }

    private static int executeTest(CommandContext<CommandSourceStack> ctx) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        boolean now = !config.isTestMode();
        config.setTestMode(now);
        config.save();
        ctx.getSource().sendSuccess(() ->
            Component.literal("Test mode " + (now ? "enabled" : "disabled") + " — configured random chances forced to 100%"), true);
        OverpoweredMobsLogger.info("Test mode " + (now ? "enabled" : "disabled"));
        return 1;
    }

    private static int executeCavalry(CommandContext<CommandSourceStack> ctx) {
        String riderStr = StringArgumentType.getString(ctx, "rider");
        String mountStr = StringArgumentType.getString(ctx, "mount");

        EntityType<?> riderType = findEntityType(riderStr);
        if (riderType == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown entity type: " + riderStr));
            return 0;
        }
        EntityType<?> mountType = findEntityType(mountStr);
        if (mountType == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown entity type: " + mountStr));
            return 0;
        }

        if (!(ctx.getSource().getLevel() instanceof ServerLevel level)) return 0;
        Vec3 pos = ctx.getSource().getPosition();
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(BlockPos.containing(pos));

        var mountEntity = mountType.create(level, EntitySpawnReason.COMMAND);
        if (!(mountEntity instanceof Mob mount)) {
            ctx.getSource().sendFailure(Component.literal("Failed to create mount"));
            return 0;
        }

        var riderEntity = riderType.create(level, EntitySpawnReason.COMMAND);
        if (!(riderEntity instanceof Mob rider)) {
            ctx.getSource().sendFailure(Component.literal("Failed to create rider"));
            return 0;
        }

        mount.setPos(pos);
        mount.addTag(OverpoweredMobs.CAVALRY_MOUNT_TAG);
        finalizeCavalryMob(mount, level, difficulty);

        rider.setPos(pos);
        // Mount first so spawn initialization cannot schedule random cavalry for this rider.
        if (!CavalryHelper.attachRider(rider, mount)) {
            rider.discard();
            mount.discard();
            ctx.getSource().sendFailure(Component.literal("Failed to attach rider to mount"));
            return 0;
        }
        finalizeCavalryMob(rider, level, difficulty);
        mount.positionRider(rider);

        if (!level.addFreshEntity(mount) || !level.addFreshEntity(rider)) {
            rider.discard();
            mount.discard();
            ctx.getSource().sendFailure(Component.literal("Failed to spawn cavalry"));
            return 0;
        }

        ctx.getSource().sendSuccess(() ->
            Component.literal("Spawned " + riderStr + " riding " + mountStr), true);
        return 1;
    }

    private static void finalizeCavalryMob(Mob mob, ServerLevel level, DifficultyInstance difficulty) {
        // Preserve the vanilla baby roll without creating an unrelated chicken mount.
        SpawnGroupData spawnData = mob instanceof Zombie
            ? new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds(level.getRandom()), false)
            : null;
        mob.finalizeSpawn(level, difficulty, EntitySpawnReason.COMMAND, spawnData);
    }

    private static int executeBloodMoon(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getLevel() instanceof ServerLevel level)) return 0;
        if (!OverpoweredMobs.getConfig().isEnableBloodMoon()) {
            ctx.getSource().sendFailure(Component.literal("Blood moon is disabled in the config"));
            return 0;
        }

        BloodMoonManager.trigger(level);
        ctx.getSource().sendSuccess(() -> Component.literal("Blood moon triggered"), true);
        return 1;
    }

    private static EntityType<?> findEntityType(String str) {
        if (!str.contains(":")) str = "minecraft:" + str;
        Identifier id = Identifier.tryParse(str);
        if (id == null) return null;
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
    }
}
