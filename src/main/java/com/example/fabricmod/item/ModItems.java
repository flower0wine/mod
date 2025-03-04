package com.example.fabricmod.item;

import com.example.fabricmod.ExampleMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    // 静态变量声明
    public static MagicWandItem MAGIC_WAND;
    public static GamblerCardItem GAMBLER_CARD;
    public static QuantumEntanglementItem QUANTUM_ENTANGLEMENT;
    public static GoldenQuantumEntanglementItem GOLDEN_QUANTUM_ENTANGLEMENT;
    public static MythicalQuantumEntanglementItem MYTHICAL_QUANTUM_ENTANGLEMENT;

    public static void registerItems() {
        // 注册物品到注册表
        MAGIC_WAND = Registry.register(
            Registries.ITEM,
            new Identifier(ExampleMod.MOD_ID, "magic_wand"),
            new MagicWandItem()
        );

        GAMBLER_CARD = Registry.register(
            Registries.ITEM,
            new Identifier(ExampleMod.MOD_ID, "gambler_card"),
            new GamblerCardItem()
        );

        QUANTUM_ENTANGLEMENT = Registry.register(
            Registries.ITEM,
            new Identifier(ExampleMod.MOD_ID, "quantum_entanglement"),
            new QuantumEntanglementItem()
        );

        GOLDEN_QUANTUM_ENTANGLEMENT = Registry.register(
            Registries.ITEM,
            new Identifier(ExampleMod.MOD_ID, "golden_quantum_entanglement"),
            new GoldenQuantumEntanglementItem()
        );

        MYTHICAL_QUANTUM_ENTANGLEMENT = Registry.register(
            Registries.ITEM,
            new Identifier(ExampleMod.MOD_ID, "mythical_quantum_entanglement"),
            new MythicalQuantumEntanglementItem()
        );

        // 将物品添加到创造模式物品栏
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(MAGIC_WAND);
            content.add(GAMBLER_CARD);
            content.add(QUANTUM_ENTANGLEMENT);
            content.add(GOLDEN_QUANTUM_ENTANGLEMENT);
            content.add(MYTHICAL_QUANTUM_ENTANGLEMENT);
        });
    }
} 