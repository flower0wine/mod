package com.example.fabricmod.data;

import com.example.fabricmod.ExampleMod;
import com.example.fabricmod.effect.EntanglementEffects;
import com.example.fabricmod.entity.MeteorEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MythicalEntanglementData extends BaseEntanglementData {
    public static final String MYTHICAL_KEY = ExampleMod.MOD_ID + "_mythical_entanglement";

    public MythicalEntanglementData() {
        super();
    }

    public static MythicalEntanglementData get(World world) {
        if (world.isClient) return null;
        if (world.getServer() == null) return null;
        return world.getServer().getOverworld().getPersistentStateManager()
            .getOrCreate((nbt) -> BaseEntanglementData.createFromNbt(nbt, new MythicalEntanglementData()), 
                MythicalEntanglementData::new, MYTHICAL_KEY);
    }

    @Override
    public <T extends BaseEntanglementData> boolean distributeDamage(float amount, World world, T data, LivingEntity target) {
        if (!(world instanceof ServerWorld serverWorld)) return false;
        if (receivers.isEmpty()) return false;

        List<UUID> aliveReceivers = new ArrayList<>(receivers);

        // 遍历所有接收者
        for (UUID receiverId : aliveReceivers) {
            Entity entity = serverWorld.getEntity(receiverId);
            if (entity instanceof LivingEntity livingEntity) {
                // 造成等额伤害
                boolean damageSuccess = livingEntity.damage(serverWorld.getDamageSources().magic(), amount);
                if (damageSuccess) {
                    // 生成闪电
                    LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(serverWorld);
                    if (lightning != null) {
                        lightning.setPosition(livingEntity.getPos());
                        serverWorld.spawnEntity(lightning);
                    }
                    
                    EntanglementEffects.spawnDamageTransferEffect(serverWorld, target, livingEntity);

                    // 在接收者上方生成陨石
                    Vec3d pos = livingEntity.getPos();
                    MeteorEntity meteor = new MeteorEntity(
                        world,
                        target,
                        pos.getX(),
                        pos.getY() + 20, // 在20格高的位置生成
                        pos.getZ()
                    );
                    
                    // 设置伤害
                    meteor.setDamage(amount * 2);
                    
                    // 生成陨石
                    world.spawnEntity(meteor);
                    
                    // 生成预警效果
                    serverWorld.spawnParticles(
                        ParticleTypes.FLAME,
                        pos.getX(), pos.getY(), pos.getZ(),
                        50,
                        0.5, 0.5, 0.5,
                        0.1
                    );

                    // 更新生命值数据
                    updateEntityHealth(receiverId, livingEntity.getHealth());
                }
            }
        }
        return true;
    }
}