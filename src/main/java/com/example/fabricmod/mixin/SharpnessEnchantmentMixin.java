package com.example.fabricmod.mixin;

import com.example.fabricmod.item.MagicWandItem;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.PowerEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DamageEnchantment.class)
public class SharpnessEnchantmentMixin extends Enchantment {

    protected SharpnessEnchantmentMixin(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes) {
        super(weight, target, slotTypes);
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof MagicWandItem || super.isAcceptableItem(stack);
    }

    @Override
    public boolean canAccept(Enchantment other) {
        return other instanceof PowerEnchantment || super.canAccept(other);
    }
} 