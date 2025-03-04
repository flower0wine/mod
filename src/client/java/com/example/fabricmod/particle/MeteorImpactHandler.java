package com.example.fabricmod.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class MeteorImpactHandler {
    public static void createImpactWave(World world, double x, double y, double z) {
        Random random = world.getRandom();
        
        // 创建多层能量波
        for (int ring = 0; ring < 5; ring++) { // 增加到5层
            double radius = 1.5 + ring * 1.5; // 每层半径递增
            int particles = 35 + ring * 15; // 每层粒子数递增
            
            for (int i = 0; i < particles; i++) {
                double angle = (i * 2 * Math.PI) / particles;
                
                // 添加随机偏移
                double radiusOffset = random.nextDouble() * 0.3 - 0.15;
                double angleOffset = random.nextDouble() * 0.1 - 0.05;
                
                // 计算粒子位置
                double px = x + Math.cos(angle + angleOffset) * (radius + radiusOffset);
                double pz = z + Math.sin(angle + angleOffset) * (radius + radiusOffset);
                
                // 向外的速度
                double vx = Math.cos(angle) * (0.2 + random.nextDouble() * 0.1);
                double vy = 0.05 + random.nextDouble() * 0.05;
                double vz = Math.sin(angle) * (0.2 + random.nextDouble() * 0.1);
                
                // 主要能量波 - 使用末地烛光粒子
                world.addParticle(
                    ParticleTypes.END_ROD,
                    px, y + 0.1 + random.nextDouble() * 0.1, pz,
                    vx * 0.3, vy * 0.5, vz * 0.3
                );
                
                // 添加灵魂火焰作为装饰
                if (random.nextFloat() < 0.3) {
                    world.addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        px, y + 0.2, pz,
                        vx * 0.4, 0.1, vz * 0.4
                    );
                }
                
                // 添加闪电效果
                if (random.nextFloat() < 0.1) {
                    world.addParticle(
                        ParticleTypes.ELECTRIC_SPARK,
                        px, y + 0.3, pz,
                        vx * 0.2, 0.15, vz * 0.2
                    );
                }
            }
            
            // 每层额外添加爆炸效果
            for (int i = 0; i < 5 + ring * 2; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double r = radius * random.nextDouble();
                double px = x + Math.cos(angle) * r;
                double pz = z + Math.sin(angle) * r;
                
                world.addParticle(
                    ParticleTypes.EXPLOSION,
                    px, y + 0.5 + random.nextDouble(), pz,
                    0, 0.1, 0
                );
            }
        }
        
        // 中心点爆发效果
        for (int i = 0; i < 40; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 0.4 + random.nextDouble() * 0.3;
            
            // 中心点灵魂火焰
            world.addParticle(
                ParticleTypes.SOUL_FIRE_FLAME,
                x, y + 0.5, z,
                Math.cos(angle) * speed,
                0.3 + random.nextDouble() * 0.2,
                Math.sin(angle) * speed
            );
            
            // 中心点末地烛光
            if (i % 2 == 0) {
                world.addParticle(
                    ParticleTypes.END_ROD,
                    x, y + 0.3, z,
                    Math.cos(angle) * speed * 0.7,
                    0.4 + random.nextDouble() * 0.2,
                    Math.sin(angle) * speed * 0.7
                );
            }
        }
        
        // 添加上升的能量柱效果
        for (int i = 0; i < 30; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double r = random.nextDouble() * 0.5;
            double px = x + Math.cos(angle) * r;
            double pz = z + Math.sin(angle) * r;
            
            world.addParticle(
                ParticleTypes.END_ROD,
                px, y, pz,
                0, 0.5 + random.nextDouble() * 0.5, 0
            );
        }
    }
} 