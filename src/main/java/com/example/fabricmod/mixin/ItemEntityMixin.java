package com.example.fabricmod.mixin;

import com.example.fabricmod.command.DropMultiplierCommand;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    private void onItemEntityConstruct(CallbackInfo ci) {
        if (DropMultiplierCommand.isMultiplierEnabled()) {
            ItemEntity self = (ItemEntity) (Object) this;
            ItemStack stack = self.getStack();
            stack.setCount(stack.getCount() * 300);
        }
    }
} 