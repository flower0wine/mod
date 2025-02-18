package com.example.fabricmod.client.render;

import com.example.fabricmod.item.MagicWandItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.world.World;
import java.util.Random;
import net.minecraft.item.ItemStack;

public class MagicWandRenderer {
    private static final Random random = new Random();

    public static void renderWandEffects(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null) return;

        // 渲染所有掉落在地上的魔法棒的粒子效果
        world.getEntities().forEach(entity -> {
            if (entity instanceof ItemEntity itemEntity) {
                if (itemEntity.getStack().getItem() instanceof MagicWandItem) {
                    spawnWandParticles(world, itemEntity.getPos().add(0, 0.25, 0));
                }
            }
        });

        // 渲染玩家手持的魔法棒的粒子效果
        ClientPlayerEntity player = client.player;
        if (player != null) {
            // 检查主手
            ItemStack mainStack = player.getMainHandStack();
            if (mainStack.getItem() instanceof MagicWandItem) {
                Vec3d wandPos = getWandPosition(player, true);
                spawnWandParticles(world, wandPos);
            }

            // 检查副手
            ItemStack offStack = player.getOffHandStack();
            if (offStack.getItem() instanceof MagicWandItem) {
                Vec3d wandPos = getWandPosition(player, false);
                spawnWandParticles(world, wandPos);
            }
        }

        // 渲染其他玩家手持的魔法棒
        world.getPlayers().forEach(otherPlayer -> {
            if (otherPlayer != player) {
                ItemStack mainStack = otherPlayer.getMainHandStack();
                if (mainStack.getItem() instanceof MagicWandItem) {
                    Vec3d wandPos = getWandPosition(otherPlayer, true);
                    spawnWandParticles(world, wandPos);
                }

                ItemStack offStack = otherPlayer.getOffHandStack();
                if (offStack.getItem() instanceof MagicWandItem) {
                    Vec3d wandPos = getWandPosition(otherPlayer, false);
                    spawnWandParticles(world, wandPos);
                }
            }
        });
    }

    private static Vec3d getWandPosition(net.minecraft.entity.player.PlayerEntity player, boolean mainHand) {
        Vec3d basePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVector();
        Vec3d rightVec = lookVec.crossProduct(new Vec3d(0, 1, 0)).normalize();
        
        float side = mainHand ? 0.4f : -0.4f;
        Vec3d sideOffset = rightVec.multiply(side);
        
        return basePos.add(
            sideOffset.x,                         // x方向偏移
            -0.4 - Math.abs(lookVec.y) * 0.2,    // y方向偏移
            sideOffset.z + 0.3                    // z方向偏移
        );
    }

    private static void spawnWandParticles(World world, Vec3d pos) {
        float time = world.getTime() * 0.3f;
        double radius = 0.4;
        
        // 只生成两个粒子
        for (int i = 0; i < 2; i++) {
            // 两个粒子相对位置相差 180 度
            double angle = time + (Math.PI * i);
            double x = pos.x + Math.cos(angle) * radius;
            double y = pos.y + Math.sin(time * 1.5) * radius * 0.3;
            double z = pos.z + Math.sin(angle) * radius;
            
            world.addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                x, y, z,
                0, 0, 0
            );
        }
    }
} 