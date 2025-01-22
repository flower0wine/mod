package com.example.fabricmod.particle;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import com.example.fabricmod.data.SwordAuraManager;
import java.util.List;

public class SwordAuraParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private static final double BASE_MOVEMENT_SPEED = 0.2;
    private static final double BASE_MAX_DISTANCE = 15.0;
    private final int level;
    private final double startX, startY, startZ; // 记录初始位置
    private double distanceTraveled; // 已移动距离
    private double maxDistance; // 最大移动距离
    private final String configType; // 添加配置类型字段

    public SwordAuraParticle(ClientWorld world, double x, double y, double z,
                            float velocityX, float velocityY, float velocityZ,
                            float targetOffsetX, float targetOffsetY, float targetOffsetZ,
                            int level, SpriteProvider spriteProvider, String configType) {
        super(world, x, y, z, 0, 0, 0);
        this.level = level;
        this.spriteProvider = spriteProvider;
        this.configType = configType;

        // 记录初始位置
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.distanceTraveled = 0;

        Registry<SwordAuraManager.SwordAuraData> registry = world.getRegistryManager().get(SwordAuraManager.SWORD_AURA_KEY);
        SwordAuraManager.SwordAuraData config = registry.get(new Identifier("fabricmod", configType));
        
        // 根据等级调整移动速度
        double adjustedSpeed = config.movement().moveSpeed() * (1 + level * config.movement().speedMultiplier());
        
        // 设置速度
        this.velocityX = velocityX * adjustedSpeed;
        this.velocityY = velocityY * adjustedSpeed;
        this.velocityZ = velocityZ * adjustedSpeed;
        
        // 根据等级设置最大移动距离
        this.maxDistance = config.movement().maxDistance() + level * config.movement().distancePerLevel();
        
        // 设置粒子基本属性
        this.maxAge = 200;
        this.scale = config.particle().particleScale();
        this.alpha = 0.8f;
        this.collidesWithWorld = false;
        
        // 根据等级设置颜色
        if (level > 0 && level <= config.particle().levelColors().size()) {
            List<Float> color = config.particle().levelColors().get(level - 1);
            this.setColor(color.get(0), color.get(1), color.get(2));
        } else {
            List<Float> color = config.particle().baseColor();
            this.setColor(color.get(0), color.get(1), color.get(2));
        }
        
        this.setSpriteForAge(spriteProvider);
    }

    public SwordAuraParticle(ClientWorld world, double x, double y, double z, 
                            double velocityX, double velocityY, double velocityZ,
                            SpriteProvider spriteProvider, int level, String configType) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        
        Registry<SwordAuraManager.SwordAuraData> registry = world.getRegistryManager().get(SwordAuraManager.SWORD_AURA_KEY);
        SwordAuraManager.SwordAuraData config = registry.get(new Identifier("fabricmod", configType));
        
        this.spriteProvider = spriteProvider;
        this.level = level;
        this.configType = configType;
        
        this.scale = config.particle().particleScale();
        
        // 记录初始位置
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.distanceTraveled = 0;
        
        // 根据等级调整移动速度
        double adjustedSpeed = config.movement().moveSpeed() * (1 + level * config.movement().speedMultiplier());
        
        // 设置速度
        this.velocityX = velocityX * adjustedSpeed;
        this.velocityY = velocityY * adjustedSpeed;
        this.velocityZ = velocityZ * adjustedSpeed;
        
        // 根据等级设置最大移动距离
        this.maxDistance = config.movement().maxDistance() + level * config.movement().distancePerLevel();
        
        // 设置粒子基本属性
        this.maxAge = 200;
        this.collidesWithWorld = false;
        
        // 根据等级设置颜色
        if (level > 0 && level <= config.particle().levelColors().size()) {
            List<Float> color = config.particle().levelColors().get(level - 1);
            this.setColor(color.get(0), color.get(1), color.get(2));
        } else {
            List<Float> color = config.particle().baseColor();
            this.setColor(color.get(0), color.get(1), color.get(2));
        }
        
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        
        // 移动粒子
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        
        // 计算已移动距离
        double dx = this.x - this.startX;
        double dy = this.y - this.startY;
        double dz = this.z - this.startZ;
        this.distanceTraveled = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        // 检查是否达到最大距离
        if (this.distanceTraveled >= this.maxDistance) {
            this.markDead();
            return;
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }
}