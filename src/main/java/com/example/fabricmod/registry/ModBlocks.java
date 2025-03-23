package com.example.fabricmod.registry;

import com.example.fabricmod.ExampleMod;
import com.example.fabricmod.block.CustomBlock;
import com.example.fabricmod.block.MysteriousBoxBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemGroups;


public class ModBlocks {
    public static final CustomBlock SWORD_AURA_CRYSTAL = new CustomBlock();
    public static final Block MYSTERIOUS_BOX = new MysteriousBoxBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
        .strength(4.0f)
        .requiresTool()
        .nonOpaque()  // 如果模型不是完整方块
    );

    public static void registerBlocks() {
        Registry.register(Registries.BLOCK, new Identifier(ExampleMod.MOD_ID, "sword_aura_crystal"), SWORD_AURA_CRYSTAL);
        Registry.register(Registries.BLOCK, new Identifier(ExampleMod.MOD_ID, "mysterious_box"), MYSTERIOUS_BOX);
    }

    public static void registerBlockItems() {
        Registry.register(Registries.ITEM, new Identifier(ExampleMod.MOD_ID, "sword_aura_crystal"),
            new BlockItem(SWORD_AURA_CRYSTAL, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(ExampleMod.MOD_ID, "mysterious_box"),
            new BlockItem(MYSTERIOUS_BOX, new FabricItemSettings()));
    }

    public static void register() {
        registerBlocks();
        registerBlockItems();

        // 将物品添加到创造模式物品栏
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(SWORD_AURA_CRYSTAL);
            content.add(MYSTERIOUS_BOX);
        });
    }
} 