package com.example.fabricmod.item;

import com.example.fabricmod.data.BaseEntanglementData;
import com.example.fabricmod.data.MythicalEntanglementData;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import java.util.List;

public class MythicalQuantumEntanglementItem extends QuantumEntanglementItem {
    public MythicalQuantumEntanglementItem() {
        super(new FabricItemSettings()
            .maxCount(1)
            .rarity(Rarity.EPIC)  // 使用自定义神话级别
            .fireproof());
    }

    @Override
    public Text getName(ItemStack stack) {
        // 直接返回红色文本
        return super.getName(stack).copy().formatted(Formatting.RED);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.fabricmod.mythical_quantum_entanglement.tooltip")
            .formatted(Formatting.RED));
    }

    @Override
    public <T extends BaseEntanglementData> T getEntanglementData(World world) {
        return (T) MythicalEntanglementData.get(world);
    }
} 