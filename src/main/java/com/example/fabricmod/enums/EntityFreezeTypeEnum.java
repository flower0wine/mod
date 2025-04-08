package com.example.fabricmod.enums;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;

public enum EntityFreezeTypeEnum {
    HOSTILE("hostile", "敌对生物") {
        @Override
        public boolean matches(Entity entity) {
            return entity instanceof HostileEntity;
        }
    },
    PASSIVE("passive", "友好生物") {
        @Override
        public boolean matches(Entity entity) {
            return entity instanceof PassiveEntity;
        }
    },
    NEUTRAL("neutral", "中立生物") {
        @Override
        public boolean matches(Entity entity) {
            return entity instanceof Angerable || 
                   entity instanceof EndermanEntity || 
                   entity instanceof SpiderEntity || 
                   entity instanceof PiglinEntity || 
                   entity instanceof ZombifiedPiglinEntity;
        }
    },
    TAMED("tamed", "已驯服生物") {
        @Override
        public boolean matches(Entity entity) {
            return entity instanceof TameableEntity && ((TameableEntity) entity).isTamed();
        }
    },
    ALL("all", "所有生物") {
        @Override
        public boolean matches(Entity entity) {
            return true;
        }
    };

    private final String id;
    private final String displayName;

    EntityFreezeTypeEnum(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract boolean matches(Entity entity);

    public static EntityFreezeTypeEnum fromId(String id) {
        for (EntityFreezeTypeEnum type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return ALL;
    }
} 