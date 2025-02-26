package com.example.fabricmod.keybinding;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding audioDeviceKey;
    
    public static void register() {
        // 注册音频设备选择按键（默认为 V 键）
        audioDeviceKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fabricmod.audio_device",      // 翻译键
            InputUtil.Type.KEYSYM,             // 按键类型
            GLFW.GLFW_KEY_V,                   // 默认按键
            "category.fabricmod.main"          // 按键分类
        ));
    }
}