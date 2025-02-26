package com.example.fabricmod.audio;

import com.example.fabricmod.enchantment.ModEnchantments;
import com.example.fabricmod.gui.AudioDeviceScreen;
import com.example.fabricmod.keybinding.KeyBindings;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VoiceJumpController {
    private static final TarsosAudioProcessor audioProcessor = new TarsosAudioProcessor();
    private static boolean isRunning = false;
    private static final float MIN_JUMP_THRESHOLD = 0.1f;   // 最小声音阈值
    private static final float MAX_JUMP_HEIGHT = 3.0f;      // 最大跳跃高度（方块）
    private static final float MIN_VELOCITY = -2.0f;        // 最小垂直速度限制
    private static final float DECELERATION = 0.2f;        // 减速率
    private static float currentSensitivity = 50.0f;  // 默认敏感度
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceJumpController.class);

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || !client.player.isAlive()) {
                if (isRunning) {
                    stop();
                }
                return;
            }

            ItemStack boots = client.player.getInventory().getArmorStack(0);
            // 检查靴子是否带有声控附魔
            if (EnchantmentHelper.getLevel(ModEnchantments.VOICE_CONTROL, boots) == 0) {
                if (isRunning) {
                    stop();
                }
                return;
            } else {
                // 获取靴子上保存的音频设备信息
                AudioDevice device = getSelectedAudioDevice(boots);
                if (device != null) {
                    start(device);
                } else {
                    LOGGER.error("无法获取音频设备，跳跃控制未启动");
                }
            }

            if (isRunning) {
                checkAndUpdateJump(client);
            }
        });
    }

    private static void checkAndUpdateJump(MinecraftClient client) {
        if (client.player == null) return;

        // 获取当前音量（0-1范围）
        float volume = audioProcessor.getCurrentVolume();
        
        // 只在超过阈值时处理
        if (volume > MIN_JUMP_THRESHOLD) {
            // 将音量映射到跳跃速度
            // volume 范围：MIN_JUMP_THRESHOLD 到 1.0
            // 目标速度范围：0 到 MAX_JUMP_HEIGHT
            float normalizedVolume = (volume - MIN_JUMP_THRESHOLD) / (1.0f - MIN_JUMP_THRESHOLD);
            float targetVelocity = normalizedVolume * MAX_JUMP_HEIGHT;
            
            // 获取当前速度
            Vec3d velocity = client.player.getVelocity();
            
            // 如果目标速度大于当前速度，则更新
            if (targetVelocity > velocity.y) {
                // 应用新速度，保持水平速度不变
                client.player.setVelocity(velocity.x, targetVelocity, velocity.z);
                
                LOGGER.debug("声控更新：音量={}, 归一化音量={}, 目标速度={}", 
                            volume, normalizedVolume, targetVelocity);
            } else {
                // 如果当前正在下落，应用减速
                float newVelocityY = (float)Math.max(MIN_VELOCITY, velocity.y - DECELERATION);
                client.player.setVelocity(velocity.x, newVelocityY, velocity.z);
            }
        }
    }

    public static void start(AudioDevice device) {
        if (!isRunning) {
            audioProcessor.setDBMin(currentSensitivity);
            audioProcessor.start(device.info());
            isRunning = true;
            LOGGER.info("声控跳跃已启动，使用设备：{}，敏感度：{}", device.name(), currentSensitivity);
        }
    }

    public static void stop() {
        if (isRunning) {
            audioProcessor.stop();
            isRunning = false;
            LOGGER.info("声控跳跃已停止");
        }
    }

    public static void updateSensitivity(float sensitivity) {
        currentSensitivity = sensitivity;
        if (isRunning) {
            audioProcessor.setDBMin(sensitivity);
            LOGGER.info("更新声控跳跃敏感度：{}", sensitivity);
        }
    }

    private static AudioDevice getSelectedAudioDevice(ItemStack boots) {
        List<AudioDevice> devices = TarsosAudioProcessor.getAvailableInputs();
        if (devices.isEmpty()) {
            LOGGER.error("没有找到可用的音频输入设备！");
            return null;
        }

        // 尝试从靴子的 NBT 中读取设备信息
        NbtCompound nbt = boots.getNbt();
        if (nbt != null && nbt.contains("SelectedAudioDevice")) {
            int savedIndex = nbt.getInt("SelectedAudioDevice");
            String savedName = nbt.getString("AudioDeviceName");
            
            // 查找保存的设备
            for (AudioDevice device : devices) {
                if (device.index() == savedIndex && device.name().equals(savedName)) {
                    LOGGER.info("使用已保存的音频设备：{}", device.name());
                    
                    // 读取并设置敏感度
                    if (nbt != null && nbt.contains("VoiceSensitivity")) {
                        currentSensitivity = nbt.getFloat("VoiceSensitivity");
                    }
                    
                    return device;
                }
            }
            LOGGER.warn("未找到已保存的音频设备，将使用默认设备");
        } else {
            // 发送提示消息给玩家
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                // 获取按键名称
                String keyName = KeyBindings.audioDeviceKey.getBoundKeyLocalizedText().getString();
                client.player.sendMessage(
                    Text.translatable("message.fabricmod.audio_device.hint", keyName)
                        .formatted(Formatting.WHITE),
                    true  // ActionBar显示
                );
            }
        }

        // 如果没有保存设备信息或找不到已保存的设备，使用默认设备
        // 优先使用立体声混音设备，如果没有则使用第一个设备
        AudioDevice defaultDevice = devices.stream()
            .filter(AudioDevice::isStereoMix)
            .findFirst()
            .orElse(devices.get(0));
        
        LOGGER.info("使用默认音频设备：{}", defaultDevice.name());
        return defaultDevice;
    }
}