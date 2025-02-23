package com.example.fabricmod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.entity.MovementType;

import java.util.List;

public class WeaponDisplayEntity extends Entity {
    // 追踪物品数据
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(WeaponDisplayEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    // 追踪旋转角度
    private static final TrackedData<Float> ROTATION = DataTracker.registerData(WeaponDisplayEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Boolean> IS_FALLING = DataTracker.registerData(WeaponDisplayEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    
    private boolean hasLanded = false;

    public WeaponDisplayEntity(EntityType<? extends WeaponDisplayEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);  // 不受重力影响
    }

    // 设置要展示的物品
    public void setDisplayItem(ItemStack stack) {
        this.dataTracker.set(ITEM, stack);
    }

    // 获取展示的物品
    public ItemStack getDisplayItem() {
        return this.dataTracker.get(ITEM);
    }

    public void setRotation(float rotation) {
        this.dataTracker.set(ROTATION, rotation);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ITEM, ItemStack.EMPTY);
        this.dataTracker.startTracking(ROTATION, 0f);
        this.dataTracker.startTracking(IS_FALLING, true);
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.getWorld().isClient) {
            if (this.dataTracker.get(IS_FALLING)) {
                // 下落逻辑
                this.setVelocity(this.getVelocity().add(0, -0.03, 0)); // 重力加速度
                this.move(MovementType.SELF, this.getVelocity());
                
                // 检查是否接触地面
                if (this.verticalCollision && !hasLanded) {
                    hasLanded = true;
                    this.dataTracker.set(IS_FALLING, false);
                    
                    // 伤害附近的生物（排除玩家）
                    Box box = this.getBoundingBox().expand(1.0);
                    List<LivingEntity> entities = this.getWorld().getEntitiesByClass(
                        LivingEntity.class, box, 
                        entity -> entity != null && 
                                 entity.isAlive() && 
                                 !(entity instanceof PlayerEntity)  // 排除玩家
                    );
                    
                    for (LivingEntity entity : entities) {
                        entity.kill();  // 直接处决
                    }
                    
                    // 播放落地音效和粒子
                    this.getWorld().playSound(null, this.getBlockPos(), 
                        SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
            }
            
            // 5秒后消失
            if (this.age >= 100000) {
                this.discard();
            }
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Item", NbtElement.COMPOUND_TYPE)) {
            this.setDisplayItem(ItemStack.fromNbt(nbt.getCompound("Item")));
        }
        this.setRotation(nbt.getFloat("Rotation"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        ItemStack stack = this.getDisplayItem();
        if (!stack.isEmpty()) {
            nbt.put("Item", stack.writeNbt(new NbtCompound()));
        }
        nbt.putFloat("Rotation", this.dataTracker.get(ROTATION));
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

}