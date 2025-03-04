package com.example.fabricmod.data;

import com.example.fabricmod.ExampleMod;
import com.example.fabricmod.effect.EntanglementEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GoldenEntanglementData extends BaseEntanglementData {
    public static final String GOLDEN_KEY = ExampleMod.MOD_ID + "_golden_entanglement";

    public GoldenEntanglementData() {
        super();
    }

    public static GoldenEntanglementData get(World world) {
        if (world.isClient) return null;
        if (world.getServer() == null) return null;
        return world.getServer().getOverworld().getPersistentStateManager()
            .getOrCreate((nbt) -> BaseEntanglementData.createFromNbt(nbt, new GoldenEntanglementData()), GoldenEntanglementData::new, GOLDEN_KEY);
    }

    /**
     * 将伤害等额分配给所有接收者，并生成闪电效果
     * @param amount 伤害量
     * @param world 世界实例
     * @return 是否成功分配伤害
     */
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
                    
                    // 更新生命值数据
                    updateEntityHealth(receiverId, livingEntity.getHealth());
                }
            }
        }
        return true;
    }
} 