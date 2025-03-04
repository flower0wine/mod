package com.example.fabricmod.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.example.fabricmod.ExampleMod;

public class ModEntities {
    public static final EntityType<WeaponDisplayEntity> WEAPON_DISPLAY = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(ExampleMod.MOD_ID, "weapon_display"),
        FabricEntityTypeBuilder.<WeaponDisplayEntity>create(SpawnGroup.MISC, WeaponDisplayEntity::new)
            .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
            .trackRangeBlocks(10)
            .trackedUpdateRate(1)
            .build()
    );

    public static final EntityType<MeteorEntity> METEOR = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(ExampleMod.MOD_ID, "meteor"),
        FabricEntityTypeBuilder.<MeteorEntity>create(SpawnGroup.MISC, MeteorEntity::new)
            .dimensions(EntityDimensions.fixed(1.0f, 1.0f))
            .trackedUpdateRate(1)
            .build()
    );
}