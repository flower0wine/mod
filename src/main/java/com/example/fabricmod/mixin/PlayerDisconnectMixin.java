package com.example.fabricmod.mixin;

import com.example.fabricmod.player.PlayerHoldManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class PlayerDisconnectMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onDisconnected", at = @At("HEAD"))
    private void onDisconnect(Text reason, CallbackInfo ci) {
        if (player != null) {
            PlayerHoldManager.removePlayer(player.getUuid());
        }
    }
} 