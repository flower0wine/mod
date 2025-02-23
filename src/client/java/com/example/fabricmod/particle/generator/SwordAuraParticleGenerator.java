package com.example.fabricmod.particle.generator;

import com.example.fabricmod.particle.SwordAuraParticleEffect;
import com.example.fabricmod.particle.SwordAuraParticleType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import com.example.fabricmod.data.SwordAuraManager;
import com.example.fabricmod.ExampleMod;

import java.util.Random;

public class SwordAuraParticleGenerator {
    public static void generateAuraParticles(ClientWorld world, double centerX, double centerY, double centerZ,
                                             Vec3d direction, Vec3d right, int level, String configType, double scaleMultiplier) {
        Registry<SwordAuraManager.SwordAuraData> registry = world.getRegistryManager().get(SwordAuraManager.SWORD_AURA_KEY);
        SwordAuraManager.SwordAuraData config = registry.get(new Identifier(ExampleMod.MOD_ID, configType));
        
        // 月牙形状参数
        int baseParticleCount = (int)((config.particle().particleCount() + level * config.particle().particlesPerLevel()) * scaleMultiplier);
        double arcRadius = (config.shape().arcRadius() + level * config.shape().radiusPerLevel()) * scaleMultiplier;
        double arcAngle = config.shape().arcAngle() + level * config.shape().arcAnglePerLevel();
        double innerRadiusRatio = config.shape().innerRadiusRatio();
        
        Random random = new Random();
        
        for (int i = 0; i < baseParticleCount; i++) {
            double angle = -arcAngle/2 + (arcAngle * i / (baseParticleCount-1));
            double normalizedAngle = (angle + arcAngle/2) / arcAngle;
            double widthMultiplier = Math.sin(normalizedAngle * Math.PI);
            
            double randomRadius = arcRadius * (innerRadiusRatio + 
                (1 - innerRadiusRatio) * random.nextDouble() * widthMultiplier);
            
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            
            Vec3d basePos = new Vec3d(centerX, centerY, centerZ)
                .add(direction.multiply(randomRadius * cos))
                .add(right.multiply(randomRadius * sin));
            
            int particlesAtThisPoint = (int)(widthMultiplier * (3 + level * 2)) + 1;
            
            for (int j = 0; j < particlesAtThisPoint; j++) {
                Vec3d particlePos = basePos.add(
                    (random.nextDouble() - 0.5) * 0.1 * widthMultiplier,
                    (random.nextDouble() - 0.5) * 0.1 * widthMultiplier,
                    (random.nextDouble() - 0.5) * 0.1 * widthMultiplier
                );
                
                world.addParticle(
                    new SwordAuraParticleEffect(
                        SwordAuraParticleType.SWORD_AURA,
                        (float)direction.x, (float)direction.y, (float)direction.z,
                        (float)right.x, (float)right.y, (float)right.z,
                        level,
                        configType
                    ),
                    particlePos.x, particlePos.y, particlePos.z,
                    direction.x, direction.y, direction.z
                );
            }
        }
    }
} 