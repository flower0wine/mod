package com.example.fabricmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.List;
import com.example.fabricmod.enums.VisionModeEnum;
import com.example.fabricmod.manager.FreezeLookManager;

import static com.example.fabricmod.manager.FreezeLookManager.FREEZE_LOOK;

public class FreezeLookClient implements ClientModInitializer {
    private Entity lastLookedEntity = null;
    private static final double VISION_RANGE = 32.0; // 视野范围（方块）
    private static final float VISION_ANGLE = 60.0f; // 视野角度（度）

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (FreezeLookManager.INSTANCE.getVisionMode() == VisionModeEnum.CROSSHAIR) {
                handleCrosshairMode(client);
            } else {
                handleVisionMode(client);
            }
        });
    }

    private void handleCrosshairMode(MinecraftClient client) {
        HitResult hit = client.crosshairTarget;
        Entity currentEntity = null;

        if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
            currentEntity = ((EntityHitResult) hit).getEntity();
        }

        if (currentEntity != lastLookedEntity) {
            PacketByteBuf buf = PacketByteBufs.create();
            try {
                buf.writeBoolean(true); // 表示这是准心模式的包
                buf.writeBoolean(currentEntity != null);
                if (currentEntity != null) {
                    buf.writeInt(currentEntity.getId());
                }
                ClientPlayNetworking.send(FREEZE_LOOK, buf);
            } finally {
                lastLookedEntity = currentEntity;
            }
        }
    }

    private void handleVisionMode(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        Vec3d playerPos = client.player.getEyePos();
        Vec3d lookVec = client.player.getRotationVec(1.0f);
        
        // 创建视野范围的边界盒
        Box visionBox = new Box(
            playerPos.x - VISION_RANGE, playerPos.y - VISION_RANGE, playerPos.z - VISION_RANGE,
            playerPos.x + VISION_RANGE, playerPos.y + VISION_RANGE, playerPos.z + VISION_RANGE
        );

        // 获取视野范围内的所有实体
        List<Entity> entitiesInRange = client.world.getEntitiesByClass(
            Entity.class, 
            visionBox,
            entity -> {
                if (entity == client.player) return false;
                
                // 检查实体是否在玩家的视野角度内
                Vec3d toEntity = entity.getPos().subtract(playerPos).normalize();
                double angle = Math.toDegrees(Math.acos(toEntity.dotProduct(lookVec)));
                return angle <= VISION_ANGLE / 2;
            }
        );

        // 发送视野范围内的实体列表
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(false); // 表示这是视野模式的包
        buf.writeInt(entitiesInRange.size());
        for (Entity entity : entitiesInRange) {
            buf.writeInt(entity.getId());
        }
        ClientPlayNetworking.send(FREEZE_LOOK, buf);
    }
}