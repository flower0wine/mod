package com.example.fabricmod.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.Box;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import com.example.fabricmod.data.SwordAuraManager;
import net.minecraft.registry.Registry;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import com.example.fabricmod.ExampleMod;

public class SwordAuraEffect {
    private static final RegistryKey<DamageType> SWORD_AURA_DAMAGE = RegistryKey.of(
        RegistryKeys.DAMAGE_TYPE, 
        new Identifier(ExampleMod.MOD_ID, "sword_aura")
    );

    private static final Map<UUID, SwordAuraTracker> activeAuras = new HashMap<>();
    
    private static class SwordAuraTracker {
        final World world;
        final LivingEntity attacker;
        Vec3d currentPos;
        final Vec3d direction;
        final Vec3d right;
        final double radius;
        final int level;
        double distanceTraveled;
        final double maxDistance;
        final double moveSpeed;
        private final String configType;
        private final int sharpnessLevel;
        
        SwordAuraTracker(World world, LivingEntity attacker, Vec3d startPos, 
                        Vec3d direction, double radius, int level,
                        String configType, int sharpnessLevel) {
            this(world, attacker, startPos, direction, 
                 direction.crossProduct(new Vec3d(0, 1, 0)).normalize(),
                 radius, level, configType, sharpnessLevel);
        }
        
        SwordAuraTracker(World world, LivingEntity attacker, Vec3d startPos, 
                        Vec3d direction, Vec3d right, double radius, int level,
                        String configType, int sharpnessLevel) {
            Registry<SwordAuraManager.SwordAuraData> registry = world.getRegistryManager().get(SwordAuraManager.SWORD_AURA_KEY);
            SwordAuraManager.SwordAuraData config = registry.get(new Identifier(ExampleMod.MOD_ID, configType));
                
            double scaleMultiplier = 1.0 + (sharpnessLevel * 0.1);
            
            this.world = world;
            this.attacker = attacker;
            this.currentPos = startPos;
            this.direction = direction;
            this.right = right;
            this.radius = radius * scaleMultiplier;
            this.level = level;
            this.configType = configType;
            this.distanceTraveled = 0;
            this.maxDistance = config.movement().maxDistance() + (level * config.movement().distancePerLevel());
            this.moveSpeed = config.movement().moveSpeed() * (1 + level * config.movement().speedMultiplier());
            this.sharpnessLevel = sharpnessLevel;
        }
        
        boolean tick() {
            Registry<SwordAuraManager.SwordAuraData> registry = world.getRegistryManager().get(SwordAuraManager.SWORD_AURA_KEY);
            SwordAuraManager.SwordAuraData config = registry.get(new Identifier(ExampleMod.MOD_ID, configType));
            
            double scaleMultiplier = 1.0 + (sharpnessLevel * 0.1);
                
            if (distanceTraveled >= maxDistance) {
                return false;
            }
            
            // 移动剑气
            currentPos = currentPos.add(direction.multiply(moveSpeed));
            distanceTraveled += moveSpeed;
            
            // 计算剑气外弧的左右端点，应用锋利等级缩放
            double arcRadius = (config.shape().arcRadius() + level * config.shape().radiusPerLevel()) * scaleMultiplier;
            
            // 增加多层检测，使破坏线更宽
            int layers = 3;
            double layerSpacing = config.shape().innerRadiusRatio();
            
            for (int layer = 0; layer < layers; layer++) {
                double offset = (layer - (layers - 1) / 2.0) * layerSpacing;
                Vec3d layerPos = currentPos.add(direction.multiply(offset));
                
                Vec3d leftPoint = layerPos.add(right.multiply(-arcRadius));
                Vec3d rightPoint = layerPos.add(right.multiply(arcRadius));
                
                // 在左右端点之间检测方块
                int steps = 20; // 检测点的数量
                for (int i = 0; i <= steps; i++) {
                    double t = (double) i / steps;
                    Vec3d checkPoint = leftPoint.multiply(1 - t).add(rightPoint.multiply(t));
                    
                    BlockPos blockPos = new BlockPos(
                        (int)checkPoint.x,
                        (int)checkPoint.y,
                        (int)checkPoint.z
                    );
                    
                    BlockState blockState = world.getBlockState(blockPos);
                    if (!blockState.isAir()) {
                        float hardness = blockState.getHardness(world, blockPos);
                        if (hardness >= 0 && hardness <= 50.0f) {
                            world.breakBlock(blockPos, true);
                            world.playSound(null, blockPos,
                                blockState.getSoundGroup().getBreakSound(),
                                SoundCategory.BLOCKS, 1.0f, 1.0f);
                        }
                    }
                }
            }
            
            // 实体伤害检测
            Box damageBox = new Box(
                currentPos.x - arcRadius, 
                currentPos.y - arcRadius, 
                currentPos.z - arcRadius,
                currentPos.x + arcRadius, 
                currentPos.y + arcRadius, 
                currentPos.z + arcRadius
            );
            
            List<LivingEntity> entities = world.getNonSpectatingEntities(LivingEntity.class, damageBox);
            float baseDamage = config.damage().baseDamage() + 
                (level - 1) * config.damage().damageMultiplier() +
                sharpnessLevel * config.damage().sharpnessMultiplier();

            for (LivingEntity target : entities) {
                if (target == attacker) continue;
                
                double distance = target.getPos().distanceTo(currentPos);
                float damageMultiplier = (float)(1.0 - (distance / arcRadius)) * config.damage().damageDistanceMultiplier();
                
                if (damageMultiplier > 0) {
                    float finalDamage = baseDamage * damageMultiplier;
                    DamageSource damageSource = world.getDamageSources().create(
                        DamageTypes.MAGIC,
                        attacker
                    );
                    target.damage(damageSource, finalDamage);
                }
            }
            
            return true;
        }
    }

    public static void createAura(World world, LivingEntity attacker, int level) {
        if (world.isClient) return;

        // 获取玩家手持物品
        ItemStack mainHand = attacker instanceof PlayerEntity ? 
            ((PlayerEntity)attacker).getMainHandStack() : 
            ItemStack.EMPTY;
            
        // 获取锋利等级
        int sharpnessLevel = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, mainHand);

        Registry<SwordAuraManager.SwordAuraData> registry = world.getRegistryManager().get(SwordAuraManager.SWORD_AURA_KEY);
        SwordAuraManager.SwordAuraData config = registry.get(new Identifier(ExampleMod.MOD_ID, "normal"));

        // 根据锋利等级调整范围和大小
        double radiusMultiplier = 1.0 + (sharpnessLevel * 0.1); // 每级锋利增加10%范围
        double scaleMultiplier = 1.0 + (sharpnessLevel * 0.1); // 每级锋利增加10%大小

        // 播放音效
        world.playSound(null, attacker.getBlockPos(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS, 
                config.effect().soundVolume(), 
                config.effect().soundPitch());

        // 发送网络包到客户端
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(attacker.getX());
        buf.writeDouble(attacker.getY());
        buf.writeDouble(attacker.getZ());
        buf.writeDouble(attacker.getRotationVector().x);
        buf.writeDouble(attacker.getRotationVector().y);
        buf.writeDouble(attacker.getRotationVector().z);
        buf.writeInt(level);
        buf.writeDouble(scaleMultiplier);  // 添加缩放参数
        
        ServerPlayNetworking.send((ServerPlayerEntity) attacker, 
            new Identifier(ExampleMod.MOD_ID, "sword_aura"), buf);

        // 添加剑气追踪器
        if (!world.isClient) {
            Vec3d startPos = attacker.getPos().add(0, 1.0, 0);
            Vec3d direction = attacker.getRotationVector();
            double radius = config.shape().arcRadius() + level * config.shape().radiusPerLevel();
            
            SwordAuraTracker tracker = new SwordAuraTracker(
                world, attacker, startPos, direction, 
                radius * radiusMultiplier, level, "normal", sharpnessLevel);
            activeAuras.put(UUID.randomUUID(), tracker);
        }
    }

    public static void createCrossAura(World world, LivingEntity attacker, int level) {
        if (world.isClient) return;

        // 获取玩家手持物品
        ItemStack mainHand = attacker instanceof PlayerEntity ? 
            ((PlayerEntity)attacker).getMainHandStack() : 
            ItemStack.EMPTY;
            
        // 获取锋利等级
        int sharpnessLevel = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, mainHand);
        
        Registry<SwordAuraManager.SwordAuraData> registry = world.getRegistryManager().get(SwordAuraManager.SWORD_AURA_KEY);
        SwordAuraManager.SwordAuraData config = registry.get(new Identifier(ExampleMod.MOD_ID, "cross"));

        // 根据锋利等级调整范围和大小
        double radiusMultiplier = 1.0 + (sharpnessLevel * 0.1); // 每级锋利增加10%范围
        double scaleMultiplier = 1.0 + (sharpnessLevel * 0.1); // 每级锋利增加10%大小
            
        // 使用配置的旋转角度
        double rotationAngle = config.effect().rotationAngle();
        
        // 播放音效
        world.playSound(null, attacker.getBlockPos(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS, 
                config.effect().soundVolume(), 
                config.effect().soundPitch());

        // 发送网络包到客户端
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(attacker.getX());
        buf.writeDouble(attacker.getY());
        buf.writeDouble(attacker.getZ());
        buf.writeDouble(attacker.getRotationVector().x);
        buf.writeDouble(attacker.getRotationVector().y);
        buf.writeDouble(attacker.getRotationVector().z);
        buf.writeInt(level);
        buf.writeDouble(rotationAngle);
        buf.writeDouble(scaleMultiplier);  // 添加缩放参数
        
        ServerPlayNetworking.send((ServerPlayerEntity) attacker, 
            new Identifier(ExampleMod.MOD_ID, "sword_cross_aura"), buf);

        // 计算两个剑气的方向和对应的right向量
        Vec3d baseDirection = attacker.getRotationVector();
        Vec3d baseRight = baseDirection.crossProduct(new Vec3d(0, 1, 0)).normalize();
        Vec3d up = baseRight.crossProduct(baseDirection).normalize();

        // 计算两个旋转后的右向量（一个向上旋转，一个向下旋转）
        Vec3d right1 = baseRight.multiply(Math.cos(rotationAngle))
            .add(up.multiply(Math.sin(rotationAngle)))
            .normalize();
            
        Vec3d right2 = baseRight.multiply(Math.cos(-rotationAngle))
            .add(up.multiply(Math.sin(-rotationAngle)))
            .normalize();

        // 创建两个剑气追踪器
        Vec3d startPos = attacker.getPos().add(0, 1.0, 0);
        double radius = config.shape().arcRadius() + level * config.shape().radiusPerLevel();
        
        SwordAuraTracker tracker1 = new SwordAuraTracker(
            world, attacker, startPos, baseDirection, right1, radius, level, "cross", sharpnessLevel);
        SwordAuraTracker tracker2 = new SwordAuraTracker(
            world, attacker, startPos, baseDirection, right2, radius, level, "cross", sharpnessLevel);
        
        activeAuras.put(UUID.randomUUID(), tracker1);
        activeAuras.put(UUID.randomUUID(), tracker2);
    }

    // 在每个游戏刻更新所有活跃的剑气
    public static void tickAuras() {
        Iterator<Map.Entry<UUID, SwordAuraTracker>> iterator = activeAuras.entrySet().iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().getValue().tick()) {
                iterator.remove();
            }
        }
    }
}
