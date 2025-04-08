package com.example.fabricmod.mixin;

import com.example.fabricmod.manager.FreezeLookManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (FreezeLookManager.INSTANCE.isEntityFrozen(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void onPushAway(Entity entity, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (FreezeLookManager.INSTANCE.isEntityFrozen(self) || 
            FreezeLookManager.INSTANCE.isEntityFrozen(entity)) {
            ci.cancel();
        }
    }
} 