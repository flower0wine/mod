package com.example.fabricmod;

import com.example.fabricmod.effect.ModEffects;
import com.example.fabricmod.enchantment.ModEnchantments;
import com.example.fabricmod.networking.ModPackets;
import com.example.fabricmod.registry.ModBlockEntities;
import net.fabricmc.api.ModInitializer;
import com.example.fabricmod.particle.SwordAuraParticleType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import com.example.fabricmod.effects.SwordAuraEffect;
import com.example.fabricmod.data.SwordAuraManager;
import com.example.fabricmod.block.CustomBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.fabricmod.item.ModItems;
import com.example.fabricmod.registry.ModBlocks;

public class ExampleMod implements ModInitializer {

    public static final String MOD_ID = "fabricmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // 首先注册数据包管理器
        SwordAuraManager.register();

        // 注册附魔
        ModEnchantments.registerEnchantments();
        
        // 注册物品
        ModItems.registerItems();

        // 注册数据包
        ModPackets.registerC2SPackets();
        
        // 注册粒子
        SwordAuraParticleType.register();

        // 注册方块
        ModBlocks.register();

        // 注册方块实体
        ModBlockEntities.registerBlockEntities();

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            SwordAuraEffect.tickAuras();
        });

        // 注册效果
        ModEffects.registerEffects();
    }
}