package com.overpoweredmobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DistanceSpeedGoal extends Goal {

    private static final int FAR_RECALC = 40;
    private static final int CLOSE_RECALC = 5;

    private static final float FAR_TURN = 4.0f;
    private static final float CLOSE_TURN = 15.0f;

    private static final double SMOOTHING = 0.15;
    private static final double STUCK_THRESHOLD_SQ = 0.01;
    private static final int STUCK_TICK_LIMIT = 20;

    private final Mob mob;
    private final double closeSpeed;
    private final double farSpeed;
    private final double slowRange;
    private Vec3 lastPos;
    private int stuckTicks;
    private int recalcCounter;
    private double currentModifier = 1.0;

    public DistanceSpeedGoal(Mob mob, double closeSpeed, double farSpeed, double slowRange) {
        this.mob = mob;
        this.closeSpeed = closeSpeed;
        this.farSpeed = farSpeed;
        this.slowRange = slowRange;
        this.lastPos = mob.position();
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(mob.level() instanceof ServerLevel level)) return false;
        Player nearest = findNearestPlayer(level);
        return nearest != null && mob.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        if (!(mob.level() instanceof ServerLevel level)) return false;
        Player nearest = findNearestPlayer(level);
        return nearest != null && mob.isAlive();
    }

    @Override
    public void tick() {
        if (!(mob.level() instanceof ServerLevel level)) return;

        Player player = findNearestPlayer(level);
        if (player == null) return;

        double dist = mob.distanceTo(player);

        double targetMps = (dist >= slowRange) ? farSpeed : closeSpeed;
        double actualSpeed = mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double targetModifier = (actualSpeed > 0) ? targetMps / actualSpeed : 1.0;

        currentModifier += (targetModifier - currentModifier) * SMOOTHING;

        mob.getNavigation().setSpeedModifier(currentModifier);

        recalcCounter++;
        boolean stuck = checkStuck();
        int recalcInterval = (dist >= slowRange) ? FAR_RECALC : CLOSE_RECALC;
        if (recalcCounter >= recalcInterval || stuck) {
            recalcCounter = 0;
            mob.getNavigation().moveTo(player, currentModifier);
        }

        float turnRate = (dist >= slowRange) ? FAR_TURN : CLOSE_TURN;
        capYaw(player, turnRate);
    }

    private Player findNearestPlayer(ServerLevel level) {
        double followRange = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        double searchRange = Math.max(slowRange + 10.0, followRange);
        return level.getNearestPlayer(mob, searchRange);
    }

    private boolean checkStuck() {
        Vec3 currentPos = mob.position();
        if (currentPos.distanceToSqr(lastPos) < STUCK_THRESHOLD_SQ) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
        }
        lastPos = currentPos;
        return stuckTicks >= STUCK_TICK_LIMIT;
    }

    private void capYaw(Player player, float maxTurn) {
        double dx = player.getX() - mob.getX();
        double dz = player.getZ() - mob.getZ();
        float targetYaw = (float) Mth.atan2(dz, dx) * Mth.RAD_TO_DEG - 90.0f;

        float currentYaw = mob.getYRot();
        float diff = Mth.wrapDegrees(targetYaw - currentYaw);
        diff = Mth.clamp(diff, -maxTurn, maxTurn);

        mob.setYRot(currentYaw + diff);
        mob.setYHeadRot(mob.getYRot());
    }
}
