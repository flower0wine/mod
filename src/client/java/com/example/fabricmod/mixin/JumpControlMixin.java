package com.example.fabricmod.mixin;

import com.example.fabricmod.enchantment.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class JumpControlMixin {
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack boots = player.getInventory().getArmorStack(0);
        
        // 如果鞋子有声控附魔，取消普通跳跃
        if (EnchantmentHelper.getLevel(ModEnchantments.VOICE_CONTROL, boots) > 0) {
            ci.cancel();
        }
    }
}