package com.overpoweredmobs;

import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

public class DistanceSpeedGoal extends Goal {
    private static final Identifier SPEED_MODIFIER_ID =
        Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "distance_speed");
    private static final double TICKS_PER_SECOND = 20.0;
    private static final double SMOOTHING = 0.15;

    private final Mob mob;
    private double currentModifier;

    public DistanceSpeedGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (!(mob.level() instanceof ServerLevel level)) return false;
        Player nearest = findNearestPlayer(level);
        return OverpoweredMobs.getConfig().isEnableDistanceSpeed()
            && nearest != null
            && mob.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        if (!(mob.level() instanceof ServerLevel level)) return false;
        Player nearest = findNearestPlayer(level);
        return OverpoweredMobs.getConfig().isEnableDistanceSpeed()
            && nearest != null
            && mob.isAlive();
    }

    @Override
    public void tick() {
        if (!(mob.level() instanceof ServerLevel level)) return;

        Player player = findNearestPlayer(level);
        if (player == null) return;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        double dist = mob.distanceTo(player);
        double targetMps = (dist >= config.getAggroSlowRange())
            ? config.getAggroFarSpeed() : config.getAggroCloseSpeed();
        var speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;

        speed.removeModifier(SPEED_MODIFIER_ID);
        double unmodifiedSpeed = speed.getValue();
        if (unmodifiedSpeed <= 0.0) return;

        double targetAttributeValue = targetMps / TICKS_PER_SECOND;
        double targetModifier = targetAttributeValue / unmodifiedSpeed - 1.0;
        currentModifier += (targetModifier - currentModifier) * SMOOTHING;
        speed.addOrUpdateTransientModifier(new AttributeModifier(
            SPEED_MODIFIER_ID,
            currentModifier,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        ));
    }

    @Override
    public void stop() {
        var speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(SPEED_MODIFIER_ID);
        currentModifier = 0.0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private Player findNearestPlayer(ServerLevel level) {
        double followRange = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        double searchRange = Math.max(OverpoweredMobs.getConfig().getAggroSlowRange() + 10.0, followRange);
        return level.getNearestPlayer(mob, searchRange);
    }
}
