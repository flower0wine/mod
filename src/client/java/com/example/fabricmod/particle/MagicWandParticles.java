package com.example.fabricmod.particle;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.Random;

public class MagicWandParticles {
    private static final Random random = new Random();

    public static void spawnLightningParticles(World world, Vec3d pos, int sharpnessLevel) {
        // 根据锋利等级选择粒子颜色
        ParticleEffect particleType = switch(sharpnessLevel) {
            case 1 -> ParticleTypes.SOUL_FIRE_FLAME;  // 蓝色火焰
            case 2 -> ParticleTypes.DRAGON_BREATH;    // 紫色粒子
            case 3 -> ParticleTypes.END_ROD;          // 白色粒子
            case 4 -> ParticleTypes.GLOW;             // 黄色发光粒子
            case 5 -> ParticleTypes.FIREWORK;         // 绚丽的烟花粒子
            default -> ParticleTypes.FLAME;           // 默认红色火焰
        };

        // 球形爆炸粒子
        for (int i = 0; i < 40; i++) {
            double phi = random.nextDouble() * Math.PI * 2;    // 水平角度 (0 到 2π)
            double theta = random.nextDouble() * Math.PI;      // 垂直角度 (0 到 π)
            double speed = 0.3 + random.nextDouble() * 0.2;    // 粒子速度
            
            // 计算球形扩散的方向向量
            double vx = Math.sin(theta) * Math.cos(phi) * speed;
            double vy = Math.cos(theta) * speed;
            double vz = Math.sin(theta) * Math.sin(phi) * speed;
            
            world.addParticle(
                ParticleTypes.FLAME,
                pos.x,
                pos.y + 0.5,
                pos.z,
                vx, vy, vz
            );
        }

        // 球形爆炸粒子
        for (int i = 0; i < 40; i++) {
            double phi = random.nextDouble() * Math.PI * 2;    // 水平角度 (0 到 2π)
            double theta = random.nextDouble() * Math.PI;      // 垂直角度 (0 到 π)
            double speed = 0.3 + random.nextDouble() * 0.2;    // 粒子速度
            
            // 计算球形扩散的方向向量
            double vx = Math.sin(theta) * Math.cos(phi) * speed;
            double vy = Math.cos(theta) * speed;
            double vz = Math.sin(theta) * Math.sin(phi) * speed;
            
            world.addParticle(
                ParticleTypes.FIREWORK,
                pos.x,
                pos.y + 0.5,
                pos.z,
                vx, vy, vz
            );
        }
        
        // 电火花粒子
        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * 1.5;
            
            // 随机位置
            double x = pos.x + Math.cos(angle) * radius;
            double y = pos.y + random.nextDouble() * 2.0;
            double z = pos.z + Math.sin(angle) * radius;
            
            // 随机速度
            double vx = (random.nextDouble() - 0.5) * 0.4;
            double vy = (random.nextDouble() - 0.5) * 0.4;
            double vz = (random.nextDouble() - 0.5) * 0.4;
            
            world.addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                x, y, z,
                vx, vy, vz
            );
        }
        
        // 添加一些烟雾效果
        for (int i = 0; i < 30; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * 1.0;
            
            double vx = Math.cos(angle) * 0.1;
            double vy = random.nextDouble() * 0.2;
            double vz = Math.sin(angle) * 0.1;
            
            world.addParticle(
                ParticleTypes.LARGE_SMOKE,
                pos.x + vx * 2,
                pos.y,
                pos.z + vz * 2,
                vx * 0.2,
                vy,
                vz * 0.2
            );
        }
        
        // 中心闪光效果
        world.addParticle(
            ParticleTypes.FLASH,
            pos.x,
            pos.y + 0.5,
            pos.z,
            0, 0, 0
        );
    }
}