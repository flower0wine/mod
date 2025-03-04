package com.example.fabricmod.manager;

import com.example.fabricmod.data.BaseEntanglementData;
import com.example.fabricmod.data.GoldenEntanglementData;
import com.example.fabricmod.data.MythicalEntanglementData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Function;

public class EntanglementManager {
    // 使用List存储所有纠缠数据类型的获取方法
    public static final List<Function<World, BaseEntanglementData>> ENTANGLEMENT_TYPES = List.of(
        BaseEntanglementData::get,
        GoldenEntanglementData::get,
        MythicalEntanglementData::get
    );

    public static <T extends BaseEntanglementData> boolean handleDamage(LivingEntity target, float amount, T data) {
        World world = target.getWorld();

        return data.distributeDamage(amount, world, data, target);
    }

    public static void handleEntityDeath(LivingEntity entity) {
        if (entity.getWorld().isClient) return;

        // 遍历所有纠缠类型并移除实体
        for (var dataGetter : ENTANGLEMENT_TYPES) {
            BaseEntanglementData data = dataGetter.apply(entity.getWorld());
            if (data != null) {
                data.removeEntity(entity);
            }
        }
    }
} 