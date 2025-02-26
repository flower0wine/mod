package com.example.fabricmod.mixin;

import com.example.fabricmod.access.AntiGravityAccess;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseHandlerMixin {
    @Shadow
    private double cursorDeltaY;
    
    @Shadow
    private double cursorDeltaX;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouse(CallbackInfo ci) {
        ClientPlayerEntity player = client.player;
        
        if (player instanceof AntiGravityAccess && ((AntiGravityAccess) player).isAntiGravity()) {
            // 反转鼠标Y轴（上下看）和X轴（左右看）
            cursorDeltaY = -cursorDeltaY;
            cursorDeltaX = -cursorDeltaX;
        }
    }
} 