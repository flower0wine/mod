package com.example.fabricmod.registry;

import com.example.fabricmod.ExampleMod;
import com.example.fabricmod.block.entity.MysteriousBoxBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    // 方块实体类型

    public static final BlockEntityType<MysteriousBoxBlockEntity> MYSTERIOUS_BOX_ENTITY = 
        FabricBlockEntityTypeBuilder.create(MysteriousBoxBlockEntity::new, ModBlocks.MYSTERIOUS_BOX).build();

    /**
     * 注册所有方块实体
     */
    public static void registerBlockEntities() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, 
            new Identifier(ExampleMod.MOD_ID, "mysterious_box"), 
            MYSTERIOUS_BOX_ENTITY);
    }
} 