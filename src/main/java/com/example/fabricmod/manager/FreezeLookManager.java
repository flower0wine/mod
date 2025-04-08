package com.example.fabricmod.manager;

import com.example.fabricmod.ExampleMod;
import com.example.fabricmod.enums.EntityFreezeTypeEnum;
import com.example.fabricmod.enums.VisionModeEnum;
import net.minecraft.entity.Entity;
import java.util.*;
import net.minecraft.util.Identifier;

public class FreezeLookManager {
    public static final FreezeLookManager INSTANCE = new FreezeLookManager();
    public static final Identifier FREEZE_LOOK = new Identifier(ExampleMod.MOD_ID, "freeze_look");

    private boolean enabled = false;
    private EntityFreezeTypeEnum entityType = EntityFreezeTypeEnum.ALL;
    private VisionModeEnum visionModeEnum = VisionModeEnum.CROSSHAIR;
    // 记录所有被冻结的实体
    private final Set<Entity> frozenEntities = new HashSet<>();
    
    private FreezeLookManager() {}

    public void setEnabled(boolean enabled, EntityFreezeTypeEnum type, VisionModeEnum mode) {
        this.enabled = enabled;
        this.entityType = type != null ? type : EntityFreezeTypeEnum.ALL;
        this.visionModeEnum = mode != null ? mode : VisionModeEnum.CROSSHAIR;
        
        // 清除之前的状态
        frozenEntities.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public VisionModeEnum getVisionMode() {
        return visionModeEnum;
    }

    public void handleEntityLooking(Entity target) {
        if (!enabled || target == null) return;
        
        if (shouldFreeze(target)) {
            frozenEntities.add(target);
        }
    }

    public void handleEntitiesInVision(List<Entity> entities) {
        if (!enabled) return;
        
        // 创建一个新的集合来存储需要移除的实体
        Set<Entity> entitiesToRemove = new HashSet<>();
        
        // 找出所有需要解冻的实体
        for (Entity entity : frozenEntities) {
            if (!entities.contains(entity)) {
                entitiesToRemove.add(entity);
            }
        }
        
        // 解冻实体
        for (Entity entity : entitiesToRemove) {
            frozenEntities.remove(entity);
            entity.setNoGravity(false);
        }
        
        // 冻结新的实体
        for (Entity entity : entities) {
            if (shouldFreeze(entity)) {
                frozenEntities.add(entity);
            }
        }
    }

    public boolean isEntityFrozen(Entity entity) {
        return enabled && frozenEntities.contains(entity);
    }

    public boolean shouldFreeze(Entity entity) {
        if (!enabled) return false;
        return entityType.matches(entity);
    }
}