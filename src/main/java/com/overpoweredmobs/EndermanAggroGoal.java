package com.overpoweredmobs;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;

public class EndermanAggroGoal extends NearestAttackableTargetGoal<Player> {
    public EndermanAggroGoal(EnderMan enderman) {
        // Vanilla combat targeting respects follow range, visibility, and player game mode.
        super(enderman, Player.class, 10, true, false,
            (target, level) -> !enderman.hasIndirectPassenger(target));
    }

    @Override
    public boolean canUse() {
        return OverpoweredMobs.getConfig().isEnableAngryEndermen() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        // Read the current config so /opm reload also updates existing Endermen.
        return OverpoweredMobs.getConfig().isEnableAngryEndermen() && super.canContinueToUse();
    }
}
