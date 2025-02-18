package com.example.fabricmod;

import com.example.fabricmod.enchantment.ModEnchantments;
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
import com.example.fabricmod.item.MagicWandItem;
import com.example.fabricmod.enchantments.ThunderMasteryEnchantment;

public class ExampleMod implements ModInitializer {

    public static final String MOD_ID = "fabricmod";

    // 创建方块实例
    public static final CustomBlock SWORD_AURA_CRYSTAL = new CustomBlock();

    // 创建物品实例
    public static final MagicWandItem MAGIC_WAND = new MagicWandItem();

    @Override
    public void onInitialize() {
        // 首先注册数据包管理器
        SwordAuraManager.register();

        // 注册附魔
        ModEnchantments.registerEnchantments();
        
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
            new Identifier(MOD_ID, "sword_aura_crystal"), 
            SWORD_AURA_CRYSTAL);
        
        // 注册方块物品
        BlockItem blockItem = new BlockItem(SWORD_AURA_CRYSTAL, new FabricItemSettings());
        Registry.register(Registries.ITEM, 
            new Identifier(MOD_ID, "sword_aura_crystal"),
            blockItem);

        // 将物品添加到创造模式物品栏
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(SWORD_AURA_CRYSTAL);
        });

        // 注册魔法棒
        Registry.register(Registries.ITEM, 
            new Identifier(MOD_ID, "magic_wand"), 
            MAGIC_WAND);
            
        // 将魔法棒添加到创造模式物品栏
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(MAGIC_WAND);
        });

    }
}