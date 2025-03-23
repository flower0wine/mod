package com.example.fabricmod.gui;

import com.example.fabricmod.ExampleMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class MysteriousBoxScreen extends Screen {
    private final ItemStack boxItem;
    private TextFieldWidget intervalField;
    private ButtonWidget destroyableButton;
    private final int currentInterval;
    private final boolean currentDestroyable;

    public MysteriousBoxScreen(ItemStack item, int interval, boolean destroyable) {
        super(Text.translatable("screen.fabricmod.mysterious_box"));
        this.boxItem = item;
        this.currentInterval = interval;
        this.currentDestroyable = destroyable;
    }

    @Override
    protected void init() {
        // 添加间隔时间输入框
        intervalField = new TextFieldWidget(
            textRenderer, 
            width / 2 - 100, 50, 
            200, 20, 
            Text.translatable("screen.fabricmod.interval")
        );
        intervalField.setText(String.valueOf(currentInterval / 20)); // 转换为秒
        addDrawableChild(intervalField);

        // 添加是否可破坏的按钮
        destroyableButton = ButtonWidget.builder(
            Text.translatable("screen.fabricmod.destroyable." + currentDestroyable),
            button -> {
                boolean newValue = !currentDestroyable;
                button.setMessage(Text.translatable("screen.fabricmod.destroyable." + newValue));
                // 发送更新包到服务器
                sendUpdatePacket(intervalField.getText(), newValue);
            }
        ).dimensions(width / 2 - 100, 80, 200, 20).build();
        addDrawableChild(destroyableButton);

        // 添加确认按钮
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.done"),
            button -> {
                sendUpdatePacket(intervalField.getText(), 
                    destroyableButton.getMessage().getString().contains("true"));
                close();
            }
        ).dimensions(width / 2 - 100, 110, 200, 20).build());
    }

    private void sendUpdatePacket(String intervalText, boolean destroyable) {
        try {
            int seconds = Integer.parseInt(intervalText);
            int ticks = seconds * 20;
            
            // 直接更新物品的NBT数据
            NbtCompound nbt = boxItem.getOrCreateNbt();
            nbt.putInt("ejectInterval", ticks);
            nbt.putBoolean("canBeDestroyed", destroyable);
            
            // 发送网络包到服务器同步物品数据
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(ticks);
            buf.writeBoolean(destroyable);
            ClientPlayNetworking.send(new Identifier(ExampleMod.MOD_ID, "mysterious_box_config"), buf);
            
            // 关闭界面
            close();
        } catch (NumberFormatException e) {
            // 处理无效输入
        }
    }
} 