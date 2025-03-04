package com.example.fabricmod.entity;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.MovementType;
import net.minecraft.block.Blocks;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import java.util.List;
import java.util.Random;
import net.minecraft.util.math.Box;

public class MeteorEntity extends ProjectileEntity {
    private float damage = 10.0f;
    private static final double FALL_SPEED = -0.1; // 降低下落速度
    private static final int DAMAGE_RADIUS = 8; // 伤害范围（方块）
    private static final int CRATER_RADIUS = 8; // 增加陨石坑半径
    private static final int EXPLOSION_RADIUS = 6; // 爆炸范围（方块）

    public MeteorEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public MeteorEntity(World world, LivingEntity owner, double x, double y, double z) {
        super(ModEntities.METEOR, world);
        this.setPosition(x, y + 40, z); // 在更高的位置生成
        this.setVelocity(0, FALL_SPEED, 0);
    }

    @Override
    protected void initDataTracker() {}

    @Override
    public void tick() {
        super.tick();
        
        if (!this.getWorld().isClient) {
            // 设置下落速度
            this.setVelocity(this.getVelocity().add(0, FALL_SPEED, 0));
            
            // 使用move方法进行移动
            this.move(MovementType.SELF, this.getVelocity());
            
            // 检查是否碰到地面
            if (this.verticalCollision) {
                createExplosion();
                this.discard();
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        
        if (!getWorld().isClient) {
            // 造成伤害
            entity.damage(getDamageSources().magic(), damage);
            // 创建爆炸
            createExplosion();
            // 移除陨石
            discard();
        }
    }

    private boolean canBreakBlock(World world, BlockPos pos) {
        float hardness = world.getBlockState(pos).getHardness(world, pos);
        return hardness >= 0 && hardness < 50.0f; // 黑曜石硬度为50
    }

    private void createCrater(BlockPos center) {
        World world = getWorld();
        Random random = new Random();

        // 先创建空的陨石坑
        for (int x = -CRATER_RADIUS; x <= CRATER_RADIUS; x++) {
            for (int z = -CRATER_RADIUS; z <= CRATER_RADIUS; z++) {
                double distance2D = Math.sqrt(x * x + z * z);
                if (distance2D <= CRATER_RADIUS) {
                    // 计算深度，使用二次函数创造弧形
                    int maxDepth = (int)(4 * (1 - (distance2D / CRATER_RADIUS)));
                    
                    // 从上往下破坏方块
                    for (int y = 0; y >= -maxDepth; y--) {
                        BlockPos currentPos = center.add(x, y, z);
                        // 检查方块硬度
                        if (canBreakBlock(world, currentPos)) {
                            world.breakBlock(currentPos, false);
                        }
                    }
                }
            }
        }

        // 然后在陨石坑表面添加融化的方块
        for (int x = -CRATER_RADIUS; x <= CRATER_RADIUS; x++) {
            for (int z = -CRATER_RADIUS; z <= CRATER_RADIUS; z++) {
                double distance2D = Math.sqrt(x * x + z * z);
                if (distance2D <= CRATER_RADIUS) {
                    // 找到每个x,z坐标最上面的空气方块
                    BlockPos surfacePos = findSurface(world, center.add(x, 0, z));
                    if (surfacePos != null) {
                        BlockPos belowSurface = surfacePos.down();
                        
                        // 检查是否可以在这个位置放置方块
                        if (canBreakBlock(world, belowSurface)) {
                            // 在边缘更容易生成地狱岩，中心更容易生成岩浆方块
                            double centerDistance = distance2D / CRATER_RADIUS;
                            if (random.nextFloat() < (0.3 + centerDistance * 0.4)) {
                                world.setBlockState(belowSurface, Blocks.NETHERRACK.getDefaultState());
                            } else {
                                world.setBlockState(belowSurface, Blocks.MAGMA_BLOCK.getDefaultState());
                            }
                        }
                    }
                }
            }
        }
    }

    // 找到从上往下第一个非空气方块的上表面
    private BlockPos findSurface(World world, BlockPos startPos) {
        BlockPos.Mutable pos = startPos.mutableCopy();
        
        // 从上往下搜索
        for (int y = 0; y >= -6; y--) { // 增加搜索深度
            pos.setY(startPos.getY() + y);
            
            // 如果当前方块是空气，下面是实心方块
            if (world.isAir(pos) && !world.isAir(pos.down())) {
                return pos;
            }
        }
        return null;
    }

    private void damageNearbyEntities(BlockPos pos) {
        Box damageBox = new Box(
            pos.getX() - DAMAGE_RADIUS, pos.getY() - 2, pos.getZ() - DAMAGE_RADIUS,
            pos.getX() + DAMAGE_RADIUS, pos.getY() + 4, pos.getZ() + DAMAGE_RADIUS
        );

        List<LivingEntity> entities = getWorld().getEntitiesByClass(
            LivingEntity.class, 
            damageBox,
            entity -> entity != null && entity.isAlive()
        );

        for (LivingEntity entity : entities) {
            double distance = Math.sqrt(entity.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()));
            float damageAmount = (float) (damage * (1.0 - (distance / DAMAGE_RADIUS)));
            entity.damage(getDamageSources().explosion(this, null), damageAmount);
            
            // 击飞效果
            double dx = entity.getX() - pos.getX();
            double dz = entity.getZ() - pos.getZ();
            double distance2D = Math.sqrt(dx * dx + dz * dz);
            if (distance2D != 0) {
                double knockback = 2.0 * (1.0 - (distance / DAMAGE_RADIUS));
                entity.addVelocity(
                    (dx / distance2D) * knockback,
                    knockback * 0.5,
                    (dz / distance2D) * knockback
                );
            }
        }
    }

    private void createExplosion() {
        World world = getWorld();
        BlockPos pos = getBlockPos();
        
        // 创建爆炸，但不破坏黑曜石及以上硬度的方块
        world.createExplosion(
            this,
            pos.getX(), pos.getY(), pos.getZ(),
            EXPLOSION_RADIUS,
            true, // 破坏方块
            World.ExplosionSourceType.BLOCK
        );

        // 创建陨石坑
        createCrater(pos);
        
        // 伤害实体
        damageNearbyEntities(pos);

        // 发送能量波效果包到客户端
        if (world instanceof ServerWorld serverWorld) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeDouble(pos.getX());
            buf.writeDouble(pos.getY());
            buf.writeDouble(pos.getZ());
            
            for (ServerPlayerEntity player : PlayerLookup.tracking(serverWorld, pos)) {
                ServerPlayNetworking.send(player, new Identifier("fabricmod", "meteor_impact"), buf);
            }
        }
        
        // 播放音效
        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, 
            SoundCategory.BLOCKS, 4.0f, (1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.2f) * 0.7f);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }
}