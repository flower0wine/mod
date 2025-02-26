package com.example.fabricmod.audio;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

public class AudioVisualizer {
    private static final TarsosAudioProcessor audioProcessor = new TarsosAudioProcessor();
    private static boolean isRunning = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(AudioVisualizer.class);
    
    // 调整参数
    private static final float AMPLITUDE_SCALE = 8.0f;      // 振幅缩放
    private static final double BAND_SPACING = 0.2;         // 频段间距
    private static final double DISTANCE = 5.0;             // 距离玩家的距离
    private static final int MAX_WIDTH_BLOCKS = 100;        // 最大宽度

    // 固定的方向向量（指向北方）
    private static final Vec3d FIXED_DIRECTION = new Vec3d(0, 0, -1);
    
    private static Vec3d[] bandPositions = null;
    private static int currentBandCount = 0;
    private static float[] previousHeights = null;
    private static final float SMOOTHING_FACTOR = 0.6f;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isRunning) {
                updateVisualization(client);
            }
        });
    }

    private static void updateVisualization(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        // 获取频谱数据
        float[] spectrum = audioProcessor.getSpectrum();
        
        // 初始化或更新频段位置
        if (bandPositions == null || currentBandCount != spectrum.length) {
            initializeBandPositions(client, spectrum.length);
        }
        
        // 初始化平滑数组
        if (previousHeights == null || previousHeights.length != spectrum.length) {
            previousHeights = new float[spectrum.length];
            Arrays.fill(previousHeights, 0.0f);
        }

        // 更新并显示每个频段
        for (int i = 0; i < 300; i++) {
            float magnitude = spectrum[i];

            if (magnitude * AMPLITUDE_SCALE < 0.2) {
                continue;
            }

            // 应用平滑处理
            magnitude = previousHeights[i] + (magnitude - previousHeights[i]) * SMOOTHING_FACTOR;
            previousHeights[i] = magnitude;
            
            float height = magnitude * AMPLITUDE_SCALE;
            Vec3d position = bandPositions[i];

            // 生成垂直方向的粒子效果
            for (double y = 0; y <= height; y += 0.2) {
                client.world.addParticle(
                    ParticleTypes.END_ROD,
                    true,
                    position.x,
                    position.y + y,
                    position.z,
                    0,
                    0,
                    0
                );
            }
        }
    }

    private static void initializeBandPositions(MinecraftClient client, int bandCount) {
        if (client.player == null) return;
        
        currentBandCount = bandCount;
        bandPositions = new Vec3d[bandCount];
        
        // 计算总宽度
        double totalWidth = Math.min(bandCount * BAND_SPACING, MAX_WIDTH_BLOCKS);
        double startX = -totalWidth / 2;
        
        // 获取玩家位置
        Vec3d playerPos = client.player.getPos();
        Vec3d offset = FIXED_DIRECTION.multiply(DISTANCE);
        
        // 计算基准点
        Vec3d basePoint = playerPos.add(offset);
        
        // 计算每个频段的位置
        for (int i = 0; i < bandCount; i++) {
            double x = basePoint.x + startX + (i * BAND_SPACING);
            bandPositions[i] = new Vec3d(x, basePoint.y, basePoint.z);
        }
    }

    public static void start(int deviceIndex) {
        if (!isRunning) {
            List<AudioDevice> devices = TarsosAudioProcessor.getAvailableInputs();
            if (deviceIndex >= 0 && deviceIndex < devices.size()) {
                AudioDevice device = devices.get(deviceIndex);
                audioProcessor.start(device.info());
                isRunning = true;
                LOGGER.info("音频可视化已启动，使用设备：{}", device.name());
            } else {
                LOGGER.error("无效的设备索引：{}", deviceIndex);
            }
        }
    }

    public static void stop() {
        if (isRunning) {
            audioProcessor.stop();
            isRunning = false;
            previousHeights = null;
            bandPositions = null;
            LOGGER.info("音频可视化已停止");
        }
    }
}