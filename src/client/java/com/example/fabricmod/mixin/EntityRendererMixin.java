package com.example.fabricmod.mixin;

import com.example.fabricmod.access.AntiGravityAccess;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class EntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(method = "setupTransforms", at = @At("TAIL"))
    private void onSetupTransforms(T entity, MatrixStack matrices, float animationProgress, 
                                 float bodyYaw, float tickDelta, CallbackInfo ci) {
        if (entity instanceof AntiGravityAccess && ((AntiGravityAccess) entity).isAntiGravity()) {
            // 在实体的中心点进行旋转
            float height = entity.getHeight();
            matrices.translate(0.0, height, 0.0);
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
            matrices.translate(0.0, -height, 0.0);
        }
    }
} 