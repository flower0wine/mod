package com.example.fabricmod.mixin;

import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

import java.util.List;
import java.util.Set;

import com.example.fabricmod.access.LightningAccess;

@Mixin(LightningEntity.class)
public class LightningEntityMixin implements LightningAccess {
    @Shadow private int ambientTick;
    
    @Shadow private boolean cosmetic;
    
    @Shadow @Final private Set<Entity> struckEntities;
    
    @Unique
    private static final TrackedData<Integer> SHARPNESS_LEVEL = 
        DataTracker.registerData(LightningEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LightningEntity lightning = (LightningEntity)(Object)this;
        if (this.ambientTick >= 0 && !this.cosmetic && getSharpnessLevel() >= 0) {
            // 如果是魔法棒的闪电
            List<Entity> list = lightning.getWorld().getOtherEntities(lightning,
                new Box(lightning.getX() - 3.0D, lightning.getY() - 3.0D, lightning.getZ() - 3.0D,
                       lightning.getX() + 3.0D, lightning.getY() + 6.0D + 3.0D, lightning.getZ() + 3.0D),
                Entity::isAlive);
            
            float damage = 5.0f + (getSharpnessLevel() * 2.5f);  // 基础伤害5点，每级锋利增加2.5点
            
            for (Entity entity : list) {
                if (!this.struckEntities.contains(entity)) {
                    entity.damage(lightning.getDamageSources().lightningBolt(), damage);
                }
            }
            
            this.struckEntities.addAll(list);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("SharpnessLevel", getSharpnessLevel());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomNbt(NbtCompound nbt, CallbackInfo ci) {
        setSharpnessLevel(nbt.getInt("SharpnessLevel"));
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initCustomDataTracker(CallbackInfo ci) {
        ((LightningEntity)(Object)this).getDataTracker().startTracking(SHARPNESS_LEVEL, -1);
    }

    @Override
    public void setSharpnessLevel(int level) {
        ((LightningEntity)(Object)this).getDataTracker().set(SHARPNESS_LEVEL, level);
    }

    @Override
    public int getSharpnessLevel() {
        return ((LightningEntity)(Object)this).getDataTracker().get(SHARPNESS_LEVEL);
    }
}