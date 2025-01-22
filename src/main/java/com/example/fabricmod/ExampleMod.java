package com.example.fabricmod;

import com.example.fabricmod.enchantments.SwordAuraEnchantment;
import com.example.fabricmod.mixin.PlayerHoldMixin;
import com.example.fabricmod.networking.ModPackets;
import net.fabricmc.api.ModInitializer;
import com.example.fabricmod.particle.SwordAuraParticleType;
import com.example.fabricmod.player.PlayerHoldManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import com.example.fabricmod.effects.SwordAuraEffect;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.GameRules;
import com.example.fabricmod.data.SwordAuraManager;

public class ExampleMod implements ModInitializer {

  @Override
  public void onInitialize() {
    // 首先注册数据包管理器
    SwordAuraManager.register();
    
    // 然后是其他初始化
    SwordAuraEnchantment.initialize();
    SwordAuraParticleType.register();
    ServerTickEvents.START_SERVER_TICK.register(server -> {
      SwordAuraEffect.tickAuras();
    });

    ServerPlayNetworking.registerGlobalReceiver(
            ModPackets.MOUSE_HOLD_STATE,
            (server, player, handler, buf, responseSender) -> {
                boolean isPressed = buf.readBoolean();
                server.execute(() -> {
                    PlayerHoldManager.handleMouseState(player, isPressed);
                });
            }
        );
  }
}