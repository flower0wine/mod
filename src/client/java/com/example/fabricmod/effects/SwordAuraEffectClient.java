package com.example.fabricmod.effects;

import com.example.fabricmod.particle.generator.SwordAuraParticleGenerator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

public class SwordAuraEffectClient {
    public static void createAura(ClientWorld world, Vec3d position, Vec3d direction, int level, double scaleMultiplier) {
        if (world == null) return;
        
        // 计算右向量，用于生成月牙形状
        Vec3d right = direction.crossProduct(new Vec3d(0, 1, 0)).normalize();
        
        // 调用粒子生成器生成粒子
        SwordAuraParticleGenerator.generateAuraParticles(
            world,
            position.x,
            position.y + 1.0, // 稍微提高一点高度，使效果更明显
            position.z,
            direction,
            right,
            level,
            "normal",
            scaleMultiplier
        );
    }

    public static void createCrossAura(ClientWorld world, Vec3d position, Vec3d direction, int level, double rotationAngle, double scaleMultiplier) {
        if (world == null) return;
        
        // 计算基础右向量
        Vec3d baseRight = direction.crossProduct(new Vec3d(0, 1, 0)).normalize();
        // 计算上向量
        Vec3d up = baseRight.crossProduct(direction).normalize();
        
        // 计算两个旋转后的右向量（一个向上旋转，一个向下旋转）
        Vec3d right1 = baseRight.multiply(Math.cos(rotationAngle))
            .add(up.multiply(Math.sin(rotationAngle)))
            .normalize();
            
        Vec3d right2 = baseRight.multiply(Math.cos(-rotationAngle))
            .add(up.multiply(Math.sin(-rotationAngle)))
            .normalize();
        
        // 生成两道交叉的剑气
        SwordAuraParticleGenerator.generateAuraParticles(
            world,
            position.x,
            position.y + 1.0,
            position.z,
            direction,
            right1,
            level,
            "cross",
            scaleMultiplier
        );
        
        SwordAuraParticleGenerator.generateAuraParticles(
            world,
            position.x,
            position.y + 1.0,
            position.z,
            direction,
            right2,
            level,
            "cross",
            scaleMultiplier
        );
    }
}