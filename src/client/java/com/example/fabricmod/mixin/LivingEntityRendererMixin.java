package com.example.fabricmod.mixin;

import com.example.fabricmod.access.AntiGravityAccess;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "setupTransforms", at = @At("TAIL"))
    private void onSetupTransforms(LivingEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, CallbackInfo ci) {
        if (entity instanceof AntiGravityAccess && ((AntiGravityAccess) entity).isAntiGravity()) {
            // 在实体的中心点进行旋转
           matrices.translate(0, entity.getHeight(), 0);
            // 使用 Quaternionf 进行旋转
           matrices.multiply(new Quaternionf().rotationZ((float) Math.PI));
        }
    }
} 