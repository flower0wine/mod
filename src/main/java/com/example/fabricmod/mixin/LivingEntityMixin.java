package com.example.fabricmod.mixin;

import com.example.fabricmod.manager.FreezeLookManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (FreezeLookManager.INSTANCE.isEntityFrozen(entity)) {
            // 保持位置不变
            entity.setVelocity(Vec3d.ZERO);
            entity.setPos(entity.prevX, entity.prevY, entity.prevZ);
            
            // 保持旋转角度不变
            entity.setYaw(entity.prevYaw);
            entity.setPitch(entity.prevPitch);
            entity.setBodyYaw(entity.prevBodyYaw);
            entity.setHeadYaw(entity.prevHeadYaw);
            
            // 禁用重力
            entity.setNoGravity(true);
            
            // 取消这一tick的更新
            ci.cancel();
        }
    }
} 