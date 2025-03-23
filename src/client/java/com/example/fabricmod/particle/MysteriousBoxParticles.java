package com.example.fabricmod.client.particle;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.random.Random;

public class MysteriousBoxParticles {
    private static final Random RANDOM = Random.create();

    public static void spawnEjectParticles(ClientWorld world, double x, double y, double z, int itemCount) {
        // 基础粒子效果（魔法圈）
        for (int i = 0; i < 20; i++) {
            double angle = i * Math.PI * 2 / 20;
            double particleX = x + Math.cos(angle) * 0.5;
            double particleZ = z + Math.sin(angle) * 0.5;
            world.addParticle(
                ParticleTypes.ENCHANT,
                particleX, y, particleZ,
                0, 0.1, 0
            );
        }

        // 为每个物品生成独特的粒子效果
        for (int item = 0; item < itemCount; item++) {
            double offsetX = RANDOM.nextDouble() * 0.4 - 0.2;
            double offsetZ = RANDOM.nextDouble() * 0.4 - 0.2;
            
            // 物品喷射轨迹的粒子
            for (int i = 0; i < 10; i++) {
                world.addParticle(
                    ParticleTypes.END_ROD,
                    x + offsetX, y, z + offsetZ,
                    RANDOM.nextDouble() * 0.2 - 0.1,
                    RANDOM.nextDouble() * 0.2 + 0.1,
                    RANDOM.nextDouble() * 0.2 - 0.1
                );
            }
        }

        // 爆发效果
        for (int i = 0; i < 15; i++) {
            world.addParticle(
                ParticleTypes.CLOUD,
                x, y, z,
                RANDOM.nextDouble() * 0.2 - 0.1,
                RANDOM.nextDouble() * 0.1,
                RANDOM.nextDouble() * 0.2 - 0.1
            );
        }
    }
} 