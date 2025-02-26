package com.example.fabricmod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import io.netty.buffer.Unpooled;
import com.example.fabricmod.networking.ModPackets;
import org.lwjgl.glfw.GLFW;
import java.util.concurrent.atomic.AtomicBoolean;

public class MouseStateHandler {
    private static boolean lastMouseState = false;

    public static void init() {
        AtomicBoolean hasScreen = new AtomicBoolean(false);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                // 检查是否在菜单中
                if (client.currentScreen != null) {
                    hasScreen.set(true);
                    // 如果在菜单中且上一次状态是按下的，发送松开状态
                    if (lastMouseState) {
                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                        buf.writeBoolean(false);
                        ClientPlayNetworking.send(ModPackets.MOUSE_HOLD_STATE, buf);
                        lastMouseState = false;
                    }
                    return;
                }

                // 检查鼠标左键状态
                boolean currentState = GLFW.glfwGetMouseButton(
                    MinecraftClient.getInstance().getWindow().getHandle(), 
                    GLFW.GLFW_MOUSE_BUTTON_LEFT
                ) == GLFW.GLFW_PRESS;

                // 如果当前在菜单中，则不发送数据包
                if (hasScreen.get()) {
                    // 如果鼠标左键还按着，则不发送数据包
                    if (!currentState) {
                        hasScreen.set(false);
                    }
                    return;
                }

                // 只在状态改变时发送数据包
                if (currentState != lastMouseState) {
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeBoolean(currentState);
                    ClientPlayNetworking.send(ModPackets.MOUSE_HOLD_STATE, buf);
                    lastMouseState = currentState;
                }
            }
        });
    }
} 