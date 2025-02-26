package com.example.fabricmod.mixin;

import com.example.fabricmod.access.AntiGravityAccess;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player instanceof AntiGravityAccess && ((AntiGravityAccess) player).isAntiGravity()) {
            KeyboardInput input = (KeyboardInput) (Object) this;
            // 反转左右移动
            float originalMovementSideways = input.movementSideways;
            input.movementSideways = -originalMovementSideways;
        }
    }
} 