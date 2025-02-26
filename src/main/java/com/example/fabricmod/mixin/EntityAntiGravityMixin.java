package com.example.fabricmod.mixin;

import com.example.fabricmod.access.AntiGravityAccess;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.example.fabricmod.enchantment.ModEnchantments;

@Mixin(LivingEntity.class)
public abstract class EntityAntiGravityMixin implements AntiGravityAccess {
    @Unique
    private static final TrackedData<Boolean> ANTI_GRAVITY = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    
    @Unique
    private int antiGravityTicks = 0;
    @Unique
    private static final int ANTI_GRAVITY_DURATION = 1000; // 5秒

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initAntiGravityData(CallbackInfo ci) {
        ((LivingEntity) (Object) this).getDataTracker().startTracking(ANTI_GRAVITY, false);
    }

    @Override
    public boolean isAntiGravity() {
        return ((LivingEntity) (Object) this).getDataTracker().get(ANTI_GRAVITY);
    }

    @Override
    public void setAntiGravity(boolean antiGravity) {
        ((LivingEntity) (Object) this).getDataTracker().set(ANTI_GRAVITY, antiGravity);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        Entity attacker = source.getAttacker();
        
        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack weapon = livingAttacker.getMainHandStack();
            int antiGravityLevel = EnchantmentHelper.getLevel(ModEnchantments.ANTI_GRAVITY, weapon);
            
            if (antiGravityLevel > 0) {
                setAntiGravity(true);
                antiGravityTicks = ANTI_GRAVITY_DURATION * antiGravityLevel;
                
                // 添加粒子效果
                if (entity.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        entity.getX(),
                        entity.getY() + entity.getHeight() / 2,
                        entity.getZ(),
                        20,  // 粒子数量
                        0.5, // X扩散范围
                        0.5, // Y扩散范围
                        0.5, // Z扩散范围
                        0.1  // 速度
                    );
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (isAntiGravity() && antiGravityTicks > 0) {
            LivingEntity entity = (LivingEntity) (Object) this;
            World world = entity.getWorld();
            
            // 添加持续的粒子效果
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                    ParticleTypes.PORTAL,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    2,   // 每tick产生的粒子数量
                    0.2, // X扩散范围
                    0.2, // Y扩散范围
                    0.2, // Z扩散范围
                    0.0  // 速度
                );
            }

            // 检查头顶方块
            Box box = entity.getBoundingBox();
            double checkHeight = box.maxY + 0.1; // 稍微向上检查一点
            boolean hasCeiling = !world.isSpaceEmpty(entity, new Box(
                box.minX, box.maxY, box.minZ,
                box.maxX, checkHeight, box.maxZ
            ));

            entity.setNoGravity(true); // 禁用原版重力

            // 处理反重力移动
            Vec3d velocity = entity.getVelocity();
            if (!hasCeiling) {
                // 如果头顶没有方块，继续上升
                velocity = velocity.add(0, 0.15, 0); // 使用与原版重力相同的加速度
            } else {
                entity.setOnGround(true); // 设置为在地面上，允许跳跃
            }

            // 限制最大上升速度
            if (velocity.y > 2.0) {
                velocity = new Vec3d(velocity.x, 2.0, velocity.z);
            }

            // 应用速度
            entity.setVelocity(velocity);
            
            antiGravityTicks--;
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        if (isAntiGravity()) {
            LivingEntity entity = (LivingEntity) (Object) this;
            World world = entity.getWorld();
            
            // 取消原版跳跃
            ci.cancel();
            
            // 反向跳跃（向下）
            float jumpVelocity = 0.42f; // 原版跳跃速度
            Vec3d velocity = entity.getVelocity();
            entity.setVelocity(velocity.x, -jumpVelocity, velocity.z);
            
            // 如果有跳跃提升效果，增加跳跃高度
            StatusEffectInstance jumpBoost = entity.getStatusEffect(StatusEffects.JUMP_BOOST);
            if (jumpBoost != null) {
                int jumpBoostLevel = jumpBoost.getAmplifier() + 1;
                entity.setVelocity(entity.getVelocity().add(
                    0,
                    -0.1f * jumpBoostLevel,
                    0
                ));
            }

            // 播放跳跃音效
            world.playSound(
                null, // 播放者（null表示所有人都能听到）
                entity.getX(), entity.getY(), entity.getZ(), // 音效位置
                SoundEvents.ENTITY_GENERIC_SMALL_FALL, // 使用通用的跳跃音效
                SoundCategory.PLAYERS, // 音效类别
                1.0f, // 音量
                1.0f  // 音调
            );
        }
    }

    // 添加数据保存
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeAntiGravityToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("AntiGravity", isAntiGravity());
        nbt.putInt("AntiGravityTicks", antiGravityTicks);
    }

    // 添加数据加载
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readAntiGravityFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("AntiGravity")) {
            setAntiGravity(nbt.getBoolean("AntiGravity"));
        }
        if (nbt.contains("AntiGravityTicks")) {
            antiGravityTicks = nbt.getInt("AntiGravityTicks");
        }

        // 触发姿势变化来更新视角
        LivingEntity entity = (LivingEntity) (Object) this;
        EntityPose originalPose = entity.getPose();
        // 先切换到下蹲
        entity.setPose(EntityPose.CROUCHING);
        // 立即切回原来的姿势
        entity.setPose(originalPose);
    }

    // 修改实体的视角
    @Inject(method = "getEyeHeight", at = @At("RETURN"), cancellable = true)
    private void onGetEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
        if (isAntiGravity()) {
            LivingEntity entity = (LivingEntity) (Object) this;
            float originalEyeHeight = cir.getReturnValue();
            float entityHeight = entity.getHeight();
            
            // 根据不同姿势计算眼睛高度
            float normalHeight = switch (pose) {
                case STANDING -> dimensions.height * 0.85f;
                case CROUCHING -> dimensions.height * 0.68f;
                case SWIMMING, FALL_FLYING, SPIN_ATTACK -> dimensions.height * 0.4f;
                default -> dimensions.height * 0.85f;
            };
            
            // 反转高度
            cir.setReturnValue(dimensions.height - normalHeight);
        }
    }
}