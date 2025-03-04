package com.example.fabricmod.item;

import com.example.fabricmod.data.BaseEntanglementData;
import com.example.fabricmod.data.GoldenEntanglementData;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import java.util.List;

public class GoldenQuantumEntanglementItem extends QuantumEntanglementItem {
    public GoldenQuantumEntanglementItem() {
        super(new FabricItemSettings()
            .maxCount(1)
            .rarity(Rarity.EPIC)
            .fireproof()); // 金色版本防火
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.fabricmod.golden_quantum_entanglement.tooltip")
            .formatted(Formatting.GOLD));
    }

    @Override
    public <T extends BaseEntanglementData> T getEntanglementData(World world) {
        return (T) GoldenEntanglementData.get(world);
    }
} 