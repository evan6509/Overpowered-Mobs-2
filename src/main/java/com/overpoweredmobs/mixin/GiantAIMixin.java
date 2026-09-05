package com.overpoweredmobs.mixin;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Giant.class)
public abstract class GiantAIMixin extends Monster {

    protected GiantAIMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        Giant giant = (Giant) (Object) this;
        var attack = giant.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(20.0);
        }
        var goals = giant.getGoalSelector();
        goals.addGoal(1, new MeleeAttackGoal(giant, 1.0, true));
        goals.addGoal(2, new LookAtPlayerGoal(giant, Player.class, 16.0f));
        goals.addGoal(3, new WaterAvoidingRandomStrollGoal(giant, 0.8));
        goals.addGoal(4, new RandomLookAroundGoal(giant));
        targetSelector.addGoal(1, new HurtByTargetGoal(giant));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(giant, Player.class, true));
    }
}
