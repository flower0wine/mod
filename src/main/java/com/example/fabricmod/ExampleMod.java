package com.example.fabricmod;

import com.example.fabricmod.enchantments.SwordAuraEnchantment;
import com.example.fabricmod.networking.ModPackets;
import net.fabricmc.api.ModInitializer;

import com.example.fabricmod.particle.SwordAuraParticleType;
import com.example.fabricmod.player.PlayerHoldManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import com.example.fabricmod.effects.SwordAuraEffect;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.example.fabricmod.data.SwordAuraManager;
import com.example.fabricmod.block.CustomBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

public class ExampleMod implements ModInitializer {

  // 创建方块实例
  public static final CustomBlock SWORD_AURA_CRYSTAL = new CustomBlock();

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

    // 注册方块
    Registry.register(Registries.BLOCK, 
        new Identifier("fabricmod", "sword_aura_crystal"), 
        SWORD_AURA_CRYSTAL);
    
    // 注册方块物品
    BlockItem blockItem = new BlockItem(SWORD_AURA_CRYSTAL, new FabricItemSettings());
    Registry.register(Registries.ITEM, 
        new Identifier("fabricmod", "sword_aura_crystal"),
        blockItem);

    // 将物品添加到创造模式物品栏
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
        content.add(SWORD_AURA_CRYSTAL);
    });

  }
}