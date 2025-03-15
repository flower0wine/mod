package com.example.fabricmod;

import com.example.fabricmod.effect.ModEffects;
import com.example.fabricmod.enchantment.ModEnchantments;
import com.example.fabricmod.networking.ModPackets;
import com.example.fabricmod.registry.ModBlockEntities;
import net.fabricmc.api.ModInitializer;
import com.example.fabricmod.particle.SwordAuraParticleType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import com.example.fabricmod.effects.SwordAuraEffect;
import com.example.fabricmod.data.SwordAuraManager;
import com.example.fabricmod.item.ModItems;
import com.example.fabricmod.registry.ModBlocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.example.fabricmod.command.DropMultiplierCommand;

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

        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DropMultiplierCommand.register(dispatcher);
        });
    }
}