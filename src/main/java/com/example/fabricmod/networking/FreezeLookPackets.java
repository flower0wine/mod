package com.example.fabricmod.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.example.fabricmod.manager.FreezeLookManager;
import net.minecraft.entity.Entity;
import java.util.ArrayList;
import java.util.List;

import static com.example.fabricmod.manager.FreezeLookManager.FREEZE_LOOK;

public class FreezeLookPackets {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(FREEZE_LOOK, (server, player, handler, buf, responseSender) -> {
            if (!FreezeLookManager.INSTANCE.isEnabled()) {
                return;
            }

            boolean isCrosshairMode = buf.readBoolean();

            if (isCrosshairMode) {
                boolean isLooking = buf.readBoolean();
                if (isLooking) {
                    int entityId = buf.readInt();
                    server.execute(() -> {
                        Entity entity = player.getWorld().getEntityById(entityId);
                        if (entity != null) {
                            FreezeLookManager.INSTANCE.handleEntityLooking(entity);
                        }
                    });
                }
            } else {
                int count = buf.readInt();
                List<Integer> entityIds = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    entityIds.add(buf.readInt());
                }

                server.execute(() -> {
                    List<Entity> entities = new ArrayList<>();
                    for (Integer id : entityIds) {
                        Entity entity = player.getWorld().getEntityById(id);
                        if (entity != null) {
                            entities.add(entity);
                        }
                    }
                    FreezeLookManager.INSTANCE.handleEntitiesInVision(entities);
                });
            }
        });
    }
} 