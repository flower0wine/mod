package com.example.fabricmod.render;

import com.example.fabricmod.entity.MeteorEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class MeteorEntityRenderer extends EntityRenderer<MeteorEntity> {
    private final BlockRenderManager blockRenderManager;
    private static final double FIRE_RADIUS = 0.8; // 火焰半径
    public MeteorEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
    }

    @Override
    public void render(MeteorEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        
        // 旋转效果
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.age * 20));

        // 渲染地狱岩方块
        blockRenderManager.renderBlockAsEntity(
            Blocks.NETHERRACK.getDefaultState(),
            matrices,
            vertexConsumers,
            light,
            0
        );

        // 生成粒子效果
        spawnParticles(entity);
        spawnFireballParticles(entity);
        
        matrices.pop();
        
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void spawnFireballParticles(MeteorEntity entity) {
        World world = entity.getWorld();
        Random random = world.getRandom();
        Vec3d velocity = entity.getVelocity();

        // 生成球形火焰效果
        for (int i = 0; i < 20; i++) { // 增加粒子数量
            // 使用球坐标系生成均匀分布的点
            double theta = random.nextDouble() * 2 * Math.PI;
            double phi = random.nextDouble() * Math.PI;
            double radius = FIRE_RADIUS * (0.8 + random.nextDouble() * 0.2); // 略微随机的半径
            
            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);

            // 火焰粒子
            world.addParticle(
                ParticleTypes.FLAME,
                entity.getX() + x,
                entity.getY() + y,
                entity.getZ() + z,
                velocity.x + random.nextGaussian() * 0.02,
                velocity.y + random.nextGaussian() * 0.02,
                velocity.z + random.nextGaussian() * 0.02
            );
        }

        // 内部明亮的核心
        for (int i = 0; i < 5; i++) {
            double x = random.nextGaussian() * 0.2;
            double y = random.nextGaussian() * 0.2;
            double z = random.nextGaussian() * 0.2;

            world.addParticle(
                ParticleTypes.SOUL_FIRE_FLAME, // 使用灵魂火焰作为核心
                entity.getX() + x,
                entity.getY() + y,
                entity.getZ() + z,
                velocity.x,
                velocity.y,
                velocity.z
            );
        }

        // 外部的烟雾效果
        for (int i = 0; i < 8; i++) {
            double theta = random.nextDouble() * 2 * Math.PI;
            double phi = random.nextDouble() * Math.PI;
            double radius = FIRE_RADIUS * 1.2; // 稍大的半径
            
            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);

            world.addParticle(
                ParticleTypes.LARGE_SMOKE,
                entity.getX() + x,
                entity.getY() + y,
                entity.getZ() + z,
                velocity.x + random.nextGaussian() * 0.01,
                velocity.y + random.nextGaussian() * 0.01,
                velocity.z + random.nextGaussian() * 0.01
            );
        }

        // 偶尔的火星效果
        if (random.nextFloat() < 0.3) {
            double x = random.nextGaussian() * FIRE_RADIUS;
            double y = random.nextGaussian() * FIRE_RADIUS;
            double z = random.nextGaussian() * FIRE_RADIUS;

            world.addParticle(
                ParticleTypes.LAVA,
                entity.getX() + x,
                entity.getY() + y,
                entity.getZ() + z,
                velocity.x * 0.2,
                velocity.y * 0.2,
                velocity.z * 0.2
            );
        }
    }

    private void spawnParticles(MeteorEntity entity) {
        World world = entity.getWorld();
        Random random = world.random;
        
        // 获取陨石的速度
        Vec3d velocity = entity.getVelocity();
        
        // 火焰尾迹
        for (int i = 0; i < 8; i++) {
            // 在陨石周围生成粒子
            double offsetX = random.nextGaussian() * 0.2;
            double offsetY = random.nextGaussian() * 0.2;
            double offsetZ = random.nextGaussian() * 0.2;
            
            // 设置粒子的速度与陨石相同，并添加一些随机偏移
            world.addParticle(
                ParticleTypes.FLAME,
                entity.getX() + offsetX,
                entity.getY() + offsetY,
                entity.getZ() + offsetZ,
                velocity.x + random.nextGaussian() * 0.02,
                velocity.y + random.nextGaussian() * 0.02,
                velocity.z + random.nextGaussian() * 0.02
            );
        }
        
        // 烟雾尾迹
        for (int i = 0; i < 4; i++) {
            double offsetX = random.nextGaussian() * 0.2;
            double offsetY = random.nextGaussian() * 0.2;
            double offsetZ = random.nextGaussian() * 0.2;
            
            world.addParticle(
                ParticleTypes.LARGE_SMOKE,
                entity.getX() + offsetX,
                entity.getY() + offsetY,
                entity.getZ() + offsetZ,
                velocity.x + random.nextGaussian() * 0.01,
                velocity.y + random.nextGaussian() * 0.01,
                velocity.z + random.nextGaussian() * 0.01
            );
        }
        
        // 闪光效果
        if (random.nextFloat() < 0.2) {
            world.addParticle(
                ParticleTypes.END_ROD,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                velocity.x + random.nextGaussian() * 0.02,
                velocity.y + random.nextGaussian() * 0.02,
                velocity.z + random.nextGaussian() * 0.02
            );
        }
    }

    @Override
    public Identifier getTexture(MeteorEntity entity) {
        // 由于我们直接渲染方块，这个纹理实际上不会被使用
        return new Identifier("minecraft", "textures/block/netherrack.png");
    }
}