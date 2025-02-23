package com.example.fabricmod.effect;

import com.example.fabricmod.ExampleMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEffects {
    public static StatusEffect GAMBLERS_BLESSING;

    public static void registerEffects() {
        GAMBLERS_BLESSING = Registry.register(
            Registries.STATUS_EFFECT,
            new Identifier(ExampleMod.MOD_ID, "gamblers_blessing"),
            new GamblersBlessingEffect()
        );
    }
} 