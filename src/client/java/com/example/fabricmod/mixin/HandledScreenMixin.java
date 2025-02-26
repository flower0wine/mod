package com.example.fabricmod.mixin;

import com.example.fabricmod.audio.AudioDevice;
import com.example.fabricmod.audio.TarsosAudioProcessor;
import com.example.fabricmod.enchantment.ModEnchantments;
import com.example.fabricmod.gui.AudioDeviceScreen;
import com.example.fabricmod.keybinding.KeyBindings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Invoker("getSlotAt")
    abstract Slot invokeGetSlotAt(double x, double y);

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        // 检查是否是我们的快捷键
        if (KeyBindings.audioDeviceKey.matchesKey(keyCode, scanCode)) {
            double mouseX = client.mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
            double mouseY = client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
            
            // 获取当前悬浮的物品槽
            Slot hoveredSlot = invokeGetSlotAt(mouseX, mouseY);
            
            // 检查是否有悬浮的物品槽
            if (hoveredSlot != null && hoveredSlot.hasStack()) {
                ItemStack hoveredItem = hoveredSlot.getStack();
                
                // 检查是否是带有声控附魔的靴子
                if (EnchantmentHelper.getLevel(ModEnchantments.VOICE_CONTROL, hoveredItem) > 0) {
                    List<AudioDevice> devices = TarsosAudioProcessor.getAvailableInputs();
                    client.setScreen(new AudioDeviceScreen(hoveredItem, devices));
                    cir.setReturnValue(true);
                }
            }
        }
    }
}