package com.example.fabricmod.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.AbstractBlock.Settings;

public class CustomBlock extends Block {
    public CustomBlock() {
        super(Settings.copy(Blocks.IRON_BLOCK)  // 使用铁块的属性作为基础
            .strength(4.0f)        // 设置硬度
            .requiresTool()        // 需要工具才能挖掘
            .luminance(state -> 15)  // 发光等级，0-15，使用 lambda 表达式
        );
    }
} 