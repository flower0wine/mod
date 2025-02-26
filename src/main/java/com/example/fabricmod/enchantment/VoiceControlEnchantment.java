package com.example.fabricmod.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ArmorItem;

public class VoiceControlEnchantment extends Enchantment {
    public VoiceControlEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMinPower(int level) {
        return 15;
    }

    @Override
    public int getMaxPower(int level) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        // 只允许鞋子使用此附魔
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem)stack.getItem()).getType() == ArmorItem.Type.BOOTS;
    }
} 