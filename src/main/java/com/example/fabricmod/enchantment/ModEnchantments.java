package com.example.fabricmod.enchantment;

import com.example.fabricmod.ExampleMod;
import com.example.fabricmod.enchantments.SwordAuraEnchantment;
import com.example.fabricmod.enchantments.ThunderMasteryEnchantment;
import com.example.fabricmod.enchantments.AntiGravityEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static Enchantment THUNDER_MASTERY;
    public static Enchantment SWORD_AURA;
    public static Enchantment ANTI_GRAVITY;

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

        ANTI_GRAVITY = Registry.register(
            Registries.ENCHANTMENT,
            new Identifier(ExampleMod.MOD_ID, "anti_gravity"),
            new AntiGravityEnchantment()
        );
    }
} 