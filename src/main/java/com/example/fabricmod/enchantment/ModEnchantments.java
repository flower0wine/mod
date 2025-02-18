package com.example.fabricmod.enchantment;

import com.example.fabricmod.ExampleMod;
import com.example.fabricmod.enchantments.SwordAuraEnchantment;
import com.example.fabricmod.enchantments.ThunderMasteryEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static Enchantment THUNDER_MASTERY;
    public static Enchantment SWORD_AURA;

    public static void registerEnchantments() {
        THUNDER_MASTERY = Registry.register(
            Registries.ENCHANTMENT,
            new Identifier(ExampleMod.MOD_ID, "thunder_mastery"),
            new ThunderMasteryEnchantment()
        );
        
        SWORD_AURA = Registry.register(
            Registries.ENCHANTMENT,
            new Identifier(ExampleMod.MOD_ID, "sword_aura"),
            new SwordAuraEnchantment()
        );
    }
} 