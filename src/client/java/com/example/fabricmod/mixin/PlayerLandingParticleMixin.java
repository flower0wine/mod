package com.example.fabricmod.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.particle.ParticleTypes;

@Mixin(ClientPlayerEntity.class)
public class PlayerLandingParticleMixin {
    private boolean wasInAir = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        
        // 检测玩家是否从空中落地
        if (wasInAir && player.isOnGround()) {
            // 生成圆形火焰粒子
            double radius = 2.0;
            int particleCount = 36;
            
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                
                // 计算向外扩散的速度
                double speedMultiplier = 0.2;
                double velocityX = offsetX * speedMultiplier;
                double velocityZ = offsetZ * speedMultiplier;
                
                player.getWorld().addParticle(
                    ParticleTypes.END_ROD,
                    player.getX() + offsetX,
                    player.getY() + 0.1,
                    player.getZ() + offsetZ,
                    velocityX,
                    0.0,
                    velocityZ
                );
            }
        }
        
        wasInAir = !player.isOnGround();
    }
} 