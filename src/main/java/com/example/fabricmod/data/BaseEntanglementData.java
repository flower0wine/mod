package com.example.fabricmod.data;

import com.example.fabricmod.ExampleMod;
import com.example.fabricmod.effect.EntanglementEffects;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.*;

public class BaseEntanglementData extends PersistentState {
    public static final String KEY = ExampleMod.MOD_ID + "_entanglement";
    
    // 存储所有传递者的集合
    protected final Set<UUID> transmitters = new HashSet<>();
    // 存储所有接收者的集合
    protected final Set<UUID> receivers = new HashSet<>();
    // 存储生命值信息
    protected final Map<UUID, Float> maxHealthMap = new HashMap<>();
    protected final Map<UUID, Float> currentHealthMap = new HashMap<>();

    public BaseEntanglementData() {
        super();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        // 保存传递者集合
        NbtList transmitterList = new NbtList();
        transmitters.forEach(uuid -> transmitterList.add(NbtHelper.fromUuid(uuid)));
        nbt.put("transmitters", transmitterList);

        // 保存接收者集合
        NbtList receiverList = new NbtList();
        receivers.forEach(uuid -> receiverList.add(NbtHelper.fromUuid(uuid)));
        nbt.put("receivers", receiverList);

        // 保存生命值信息
        NbtCompound healthData = new NbtCompound();
        maxHealthMap.forEach((uuid, maxHealth) -> {
            NbtCompound entry = new NbtCompound();
            entry.putFloat("max", maxHealth);
            entry.putFloat("current", currentHealthMap.getOrDefault(uuid, maxHealth));
            healthData.put(uuid.toString(), entry);
        });
        nbt.put("health", healthData);

        return nbt;
    }

    public static <T extends BaseEntanglementData> T createFromNbt(NbtCompound nbt, T data) {
        
        // 加载传递者集合
        NbtList transmitterList = nbt.getList("transmitters", NbtElement.INT_ARRAY_TYPE);
        transmitterList.forEach(element -> data.transmitters.add(NbtHelper.toUuid(element)));

        // 加载接收者集合
        NbtList receiverList = nbt.getList("receivers", NbtElement.INT_ARRAY_TYPE);
        receiverList.forEach(element -> data.receivers.add(NbtHelper.toUuid(element)));

        // 加载生命值信息
        NbtCompound healthData = nbt.getCompound("health");
        healthData.getKeys().forEach(key -> {
            UUID uuid = UUID.fromString(key);
            NbtCompound entry = healthData.getCompound(key);
            data.maxHealthMap.put(uuid, entry.getFloat("max"));
            data.currentHealthMap.put(uuid, entry.getFloat("current"));
        });

        return data;
    }

    public boolean isTransmitter(UUID entityId) {
        return transmitters.contains(entityId);
    }

    public boolean isReceiver(UUID entityId) {
        return receivers.contains(entityId);
    }

    public Set<UUID> getReceivers(World world) {
        if (!(world instanceof ServerWorld serverWorld)) return new HashSet<>();
        
        // 创建一个新的集合来存储需要移除的接收者
        Set<UUID> toRemove = new HashSet<>();
        Set<UUID> validReceivers = new HashSet<>();
        
        // 检查每个接收者
        for (UUID receiverId : receivers) {
            Entity entity = serverWorld.getEntity(receiverId);
            if (entity instanceof LivingEntity livingEntity && entity.isAlive()) {
                validReceivers.add(receiverId);
            } else {
                toRemove.add(receiverId);
            }
        }
        
        // 移除无效的接收者
        if (!toRemove.isEmpty()) {
            receivers.removeAll(toRemove);
            // 清理相关的生命值数据
            toRemove.forEach(uuid -> {
                maxHealthMap.remove(uuid);
                currentHealthMap.remove(uuid);
            });
            this.markDirty();
        }
        
        return validReceivers;
    }

    public float getHealthPercentage(UUID entityId) {
        Float maxHealth = maxHealthMap.get(entityId);
        Float currentHealth = currentHealthMap.get(entityId);
        if (maxHealth != null && currentHealth != null && maxHealth > 0) {
            return currentHealth / maxHealth;
        }
        return 0.0f;
    }

    public void updateEntityHealth(UUID entityId, float health) {
        if (maxHealthMap.containsKey(entityId)) {
            currentHealthMap.put(entityId, health);
            this.markDirty();
        }
    }

    public List<UUID> getTopHealthReceivers(int limit, ServerWorld serverWorld) {
        return receivers.stream()
            .filter(uuid -> {
                Entity entity = serverWorld.getEntity(uuid);
                return entity instanceof LivingEntity && entity.isAlive();
            })
            .sorted((id1, id2) -> Float.compare(
                getHealthPercentage(id2),
                getHealthPercentage(id1)))
            .limit(limit)
            .toList();
    }

    public <T extends BaseEntanglementData> boolean distributeDamage(float amount, World world, T data, LivingEntity target) {
        if (!(world instanceof ServerWorld serverWorld)) return false;
        if (receivers.isEmpty()) return false;

        // 获取存活的接收者
        List<UUID> aliveReceivers = getTopHealthReceivers(5, serverWorld);

        if (aliveReceivers.isEmpty()) {
            return false;
        }

        // 随机选择一个接收者
        Random random = new Random();
        UUID selectedReceiver = aliveReceivers.get(random.nextInt(aliveReceivers.size()));
        Entity entity = serverWorld.getEntity(selectedReceiver);
        
        if (entity instanceof LivingEntity livingEntity && entity.isAlive()) {
            // 尝试对接收者造成伤害
            boolean damageSuccess = livingEntity.damage(serverWorld.getDamageSources().magic(), amount);
            if (damageSuccess) {
                // 添加效果
                EntanglementEffects.spawnDamageTransferEffect(serverWorld, target, entity);
                // 更新血量数据
                data.updateEntityHealth(selectedReceiver, livingEntity.getHealth());
            }
        }
        return true;
    }

    /**
     * 添加传递者
     */
    public void addTransmitter(LivingEntity transmitter) {
        UUID transmitterId = transmitter.getUuid();
        receivers.remove(transmitterId);
        transmitters.add(transmitterId);
        // 记录生命值信息
        maxHealthMap.put(transmitterId, transmitter.getMaxHealth());
        currentHealthMap.put(transmitterId, transmitter.getHealth());
        this.markDirty();
    }

    /**
     * 添加接收者
     */
    public void addReceiver(LivingEntity receiver) {
        UUID receiverId = receiver.getUuid();
        transmitters.remove(receiverId);
        receivers.add(receiverId);
        // 记录生命值信息
        maxHealthMap.put(receiverId, receiver.getMaxHealth());
        currentHealthMap.put(receiverId, receiver.getHealth());
        this.markDirty();
    }

    /**
     * 移除传递者
     */
    public void removeTransmitter(LivingEntity transmitter) {
        UUID transmitterId = transmitter.getUuid();
        transmitters.remove(transmitterId);
        // 如果该实体不是接收者，则清除其生命值信息
        if (!receivers.contains(transmitterId)) {
            maxHealthMap.remove(transmitterId);
            currentHealthMap.remove(transmitterId);
        }
        this.markDirty();
    }

    /**
     * 移除接收者
     */
    public void removeReceiver(LivingEntity receiver) {
        UUID receiverId = receiver.getUuid();
        receivers.remove(receiverId);
        // 如果该实体不是传递者，则清除其生命值信息
        if (!transmitters.contains(receiverId)) {
            maxHealthMap.remove(receiverId);
            currentHealthMap.remove(receiverId);
        }
        this.markDirty();
    }

    public void removeEntity(LivingEntity entity) {
        removeTransmitter(entity);
        removeReceiver(entity);
    }

    /**
     * 检查实体是否可以成为传递者
     */
    public boolean canBeTransmitter(UUID entityId) {
        return !receivers.contains(entityId); // 接收者不能成为传递者
    }

    /**
     * 检查实体是否可以成为接收者
     */
    public boolean canBeReceiver(UUID entityId) {
        return !transmitters.contains(entityId); // 传递者不能成为接收者
    }

    public static BaseEntanglementData get(World world) {
        if (world.isClient) return null;
        if (world.getServer() == null) return null;
        return world.getServer().getOverworld().getPersistentStateManager()
            .getOrCreate((nbt) -> BaseEntanglementData.createFromNbt(nbt, new BaseEntanglementData()), BaseEntanglementData::new, KEY);
    }
}