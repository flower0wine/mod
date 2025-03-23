package com.example.fabricmod.networking;

import com.example.fabricmod.event.GamblerEvents;
import com.example.fabricmod.player.PlayerHoldManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import com.example.fabricmod.ExampleMod;

public class ModPackets {
    public static final Identifier MOUSE_HOLD_STATE = new Identifier(ExampleMod.MOD_ID, "mouse_hold_state");
    public static final Identifier MYSTERIOUS_BOX_EJECT = new Identifier(ExampleMod.MOD_ID, "mysterious_box_eject");

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
    }
} 