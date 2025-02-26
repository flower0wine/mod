package com.example.fabricmod.mixin;

import com.example.fabricmod.access.AntiGravityAccess;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        GameRenderer gameRenderer = (GameRenderer) (Object) this;
        if (gameRenderer.getClient().player instanceof AntiGravityAccess && 
            ((AntiGravityAccess) gameRenderer.getClient().player).isAntiGravity()) {
            // 旋转整个世界视图180度
            matrix.multiply(new org.joml.Quaternionf().rotationZ((float) Math.PI));
        }
    }
} 