package com.example.fabricmod.registry;

import com.example.fabricmod.ExampleMod;
import com.example.fabricmod.block.CustomBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemGroups;


public class ModBlocks {
    public static final CustomBlock SWORD_AURA_CRYSTAL = new CustomBlock();

    public static void registerBlocks() {
        Registry.register(Registries.BLOCK, new Identifier(ExampleMod.MOD_ID, "sword_aura_crystal"), SWORD_AURA_CRYSTAL);
    }

    public static void registerBlockItems() {
        Registry.register(Registries.ITEM, new Identifier(ExampleMod.MOD_ID, "sword_aura_crystal"),
            new BlockItem(SWORD_AURA_CRYSTAL, new FabricItemSettings()));
    }

    public static void register() {
        registerBlocks();
        registerBlockItems();

        // 将物品添加到创造模式物品栏
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(SWORD_AURA_CRYSTAL);
        });
    }
} 