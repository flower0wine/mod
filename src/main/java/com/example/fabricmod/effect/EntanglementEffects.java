package com.example.fabricmod.effect;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntanglementEffects {
    public static void spawnBindingEffect(World world, Entity entity, boolean isTransmitter) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        ParticleEffect particleType = isTransmitter ?
            ParticleTypes.SOUL_FIRE_FLAME :
            ParticleTypes.SOUL;

        serverWorld.spawnParticles(
            particleType,
            entity.getX(),
            entity.getY() + entity.getHeight() / 2,
            entity.getZ(),
            30, // 粒子数量
            0.3, // X扩散范围
            0.5, // Y扩散范围
            0.3, // Z扩散范围
            0.05 // 速度
        );

        // 播放音效
        world.playSound(
            null,
            entity.getBlockPos(),
            isTransmitter ? SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE : SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN,
            SoundCategory.PLAYERS,
            1.0F,
            isTransmitter ? 1.2F : 0.8F
        );
    }

    public static void spawnDamageTransferEffect(World world, Entity from, Entity to) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        // 创建从伤害源到目标的粒子线
        Vec3d start = from.getPos().add(0, from.getHeight() / 2, 0);
        Vec3d end = to.getPos().add(0, to.getHeight() / 2, 0);
        Vec3d direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        
        for (double d = 0; d < distance; d += 0.5) {
            Vec3d pos = start.add(direction.multiply(d));
            serverWorld.spawnParticles(
                ParticleTypes.WITCH,
                pos.x, pos.y, pos.z,
                1, 0, 0, 0,
                0
            );
        }

        // 播放传递音效
        world.playSound(
            null,
            from.getBlockPos(),
            SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE,
            SoundCategory.PLAYERS,
            1.0F,
            1.0F
        );
    }
} 