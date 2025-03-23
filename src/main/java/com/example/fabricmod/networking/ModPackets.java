package com.example.fabricmod.networking;

import com.example.fabricmod.block.MysteriousBoxBlock;
import com.example.fabricmod.player.PlayerHoldManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import com.example.fabricmod.ExampleMod;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ModPackets {
    public static final Identifier MOUSE_HOLD_STATE = new Identifier(ExampleMod.MOD_ID, "mouse_hold_state");
    public static final Identifier MYSTERIOUS_BOX_EJECT = new Identifier(ExampleMod.MOD_ID, "mysterious_box_eject");
    public static final Identifier MYSTERIOUS_BOX_CONFIG = new Identifier(ExampleMod.MOD_ID, "mysterious_box_config");

    public static void registerC2SPackets() {
        // 现有的鼠标状态处理
        ServerPlayNetworking.registerGlobalReceiver(
                ModPackets.MOUSE_HOLD_STATE,
                (server, player, handler, buf, responseSender) -> {
                    boolean isPressed = buf.readBoolean();
                    server.execute(() -> {
                        PlayerHoldManager.handleMouseState(player, isPressed);
                    });
                }
        );

        // 添加神秘盒子配置处理器
        ServerPlayNetworking.registerGlobalReceiver(MYSTERIOUS_BOX_CONFIG,
            (server, player, handler, buf, responseSender) -> {
                int interval = buf.readInt();
                boolean canBeDestroyed = buf.readBoolean();
                
                // 在服务端线程中执行
                server.execute(() -> {
                    // 获取玩家手中的物品
                    ItemStack heldItem = player.getMainHandStack();
                    if (heldItem.getItem() instanceof BlockItem blockItem &&
                        blockItem.getBlock() instanceof MysteriousBoxBlock) {
                        // 更新物品NBT
                        NbtCompound nbt = heldItem.getOrCreateNbt();
                        nbt.putInt("ejectInterval", interval);
                        nbt.putBoolean("canBeDestroyed", canBeDestroyed);
                    }
                });
            }
        );
    }
} 