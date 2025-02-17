package com.example.fabricmod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class PlayerSprintParticleMixin {
    private final Random random = Random.create();
    private boolean hasSprintParticles = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        
        if (player.isSprinting() && !hasSprintParticles) {
            // 获取玩家的移动方向
            double velX = player.getVelocity().x;
            double velY = player.getVelocity().y;
            double velZ = player.getVelocity().z;
            
            // 生成跟随的粒子
            for (int i = 0; i < 8; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.5;
                double offsetY = random.nextDouble() * 0.5;
                double offsetZ = (random.nextDouble() - 0.5) * 0.5;
                
                // 使用玩家的移动速度作为粒子的速度
                player.getWorld().addParticle(
                    ParticleTypes.CLOUD, // 使用云粒子
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    velX * 0.1, // 粒子X方向速度
                    velY * 0.1, // 粒子Y方向速度
                    velZ * 0.1  // 粒子Z方向速度
                );
            }
            hasSprintParticles = true;
        } else if (!player.isSprinting()) {
            hasSprintParticles = false;
        }
    }
} 