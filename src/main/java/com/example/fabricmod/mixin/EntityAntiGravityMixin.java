package com.example.fabricmod.mixin;

import com.example.fabricmod.access.AntiGravityAccess;
import com.example.fabricmod.enchantment.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.MovementType;

@Mixin(LivingEntity.class)
public abstract class EntityAntiGravityMixin implements AntiGravityAccess {
    @Unique
    private static final TrackedData<Boolean> ANTI_GRAVITY = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    
    @Unique
    private int antiGravityTicks = 0;
    @Unique
    private static final int ANTI_GRAVITY_DURATION = 100; // 5秒

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
            
            // 添加持续的粒子效果
            if (entity.getWorld() instanceof ServerWorld serverWorld) {
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
            
            antiGravityTicks--;
            if (antiGravityTicks <= 0) {
                setAntiGravity(false);
            }
        }
    }

    // 处理重力
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravel(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (isAntiGravity()) {
            // 获取当前速度
            Vec3d velocity = entity.getVelocity();
            
            // 应用反向重力（与原版相同的加速度值）
            if (!entity.isOnGround()) {
                double gravity = 0.08;
                
                // 应用反向重力
                velocity = velocity.add(0, gravity, 0);
                
                // 应用空气阻力
                velocity = velocity.multiply(0.98);
                
                // 限制最大上升速度
                if (velocity.y > 3.92) {
                    velocity = new Vec3d(velocity.x, 3.92, velocity.z);
                }
            }
            
            // 处理水中和熔岩中的浮力
            if (entity.isTouchingWater()) {
                velocity = velocity.add(0, -0.02, 0);
            } else if (entity.isInLava()) {
                velocity = velocity.add(0, -0.02, 0);
            }
            
            // 保持原有的水平移动，但减小其影响
            velocity = new Vec3d(
                velocity.x * 0.91, 
                velocity.y, 
                velocity.z * 0.91
            );
            
            // 设置新的速度
            entity.setVelocity(velocity);
            
            // 移动实体
            entity.move(MovementType.SELF, entity.getVelocity());
            
            ci.cancel(); // 取消原版的移动处理
        }
    }

    @Inject(method = "getMovementSpeed*", at = @At("HEAD"), cancellable = true)
    private void onGetMovementSpeed(CallbackInfoReturnable<Float> cir) {
        if (isAntiGravity()) {
            // 在反重力状态下减小移动速度，这会影响动画速度
            cir.setReturnValue(0.02f);
        }
    }
}