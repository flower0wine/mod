package com.example.fabricmod.gui;

import com.example.fabricmod.audio.AudioDevice;
import com.example.fabricmod.audio.VoiceJumpController;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.List;

public class AudioDeviceScreen extends Screen {
    private final ItemStack boots;
    private final List<AudioDevice> devices;
    private AudioDevice currentDevice;

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 4;
    private static final int SLIDER_WIDTH = 200;
    private float sensitivity;  // 敏感度（0-100）

    public AudioDeviceScreen(ItemStack boots, List<AudioDevice> devices) {
        super(Text.translatable("gui.fabricmod.audio_device.title"));
        this.boots = boots;
        this.devices = devices;
        
        // 从靴子NBT中读取敏感度
        NbtCompound nbt = boots.getNbt();
        if (nbt != null && nbt.contains("VoiceSensitivity")) {
            this.sensitivity = nbt.getFloat("VoiceSensitivity");
        } else {
            this.sensitivity = 50.0f;  // 默认敏感度
        }
        
        // 获取当前选中的设备
        if (nbt != null && nbt.contains("SelectedAudioDevice")) {
            int savedIndex = nbt.getInt("SelectedAudioDevice");
            String savedName = nbt.getString("AudioDeviceName");
            
            // 查找保存的设备
            currentDevice = devices.stream()
                .filter(device -> device.index() == savedIndex && device.name().equals(savedName))
                .findFirst()
                .orElseGet(() -> {
                    // 如果找不到已保存的设备，使用默认设备
                    return devices.stream()
                        .filter(AudioDevice::isStereoMix)
                        .findFirst()
                        .orElse(devices.get(0));
                });
        } else {
            // 如果没有保存设备信息，使用默认设备
            currentDevice = devices.stream()
                .filter(AudioDevice::isStereoMix)
                .findFirst()
                .orElse(devices.get(0));
        }
    }

    @Override
    protected void init() {
        super.init();
        
        // 计算内容的总高度
        int totalContentHeight = devices.size() * (BUTTON_HEIGHT + BUTTON_SPACING);
        int startY = (height - totalContentHeight) / 2;
        
        // 添加设备按钮
        for (int i = 0; i < devices.size(); i++) {
            final AudioDevice device = devices.get(i);
            boolean isSelected = device.equals(currentDevice);

            ButtonWidget button = new ButtonWidget.Builder(
                Text.literal(device.name() + (device.isStereoMix() ? " (立体声混音)" : "")),
                btn -> {
                    // 更新所有按钮的状态
                    for (Element child : children()) {
                        if (child instanceof ButtonWidget buttonWidget) {
                            buttonWidget.active = true;
                        }
                    }
                    btn.active = false;  // 设置当前按钮为按下状态
                    
                    // 保存设备信息
                    NbtCompound nbt = boots.getOrCreateNbt();
                    nbt.putInt("SelectedAudioDevice", device.index());
                    nbt.putString("AudioDeviceName", device.name());
                    currentDevice = device;
                })
                .dimensions(
                    (width - BUTTON_WIDTH) / 2,
                    startY + i * (BUTTON_HEIGHT + BUTTON_SPACING),
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT
                )
                .build();

            // 设置初始状态
            button.active = !isSelected;
            
            addDrawableChild(button);
        }
        
        // 添加敏感度滑块
        int sliderY = startY + devices.size() * (BUTTON_HEIGHT + BUTTON_SPACING) + BUTTON_SPACING;
        SliderWidget sensitivitySlider = new SliderWidget(
            (width - SLIDER_WIDTH) / 2,
            sliderY,
            SLIDER_WIDTH,
            20,
            Text.translatable("gui.fabricmod.sensitivity", String.format("%.0f", sensitivity)),
            sensitivity / 100.0f  // 转换为0-1范围
        ) {
            @Override
            protected void updateMessage() {
                setMessage(Text.translatable("gui.fabricmod.sensitivity", 
                    String.format("%.0f", sensitivity)));
            }
            
            @Override
            protected void applyValue() {
                sensitivity = (float) (this.value * 100.0f);
                // 保存到靴子NBT
                NbtCompound nbt = boots.getOrCreateNbt();
                nbt.putFloat("VoiceSensitivity", sensitivity);
                // 通知VoiceJumpController更新敏感度
                VoiceJumpController.updateSensitivity(sensitivity);
            }
        };
        
        addDrawableChild(sensitivitySlider);
        
        // 添加关闭按钮，位置需要下移
        ButtonWidget closeButton = ButtonWidget.builder(
            Text.translatable("gui.done"),
            button -> close())
            .dimensions(
                (width - 60) / 2,
                sliderY + 30,
                60,
                20
            )
            .build();
        
        addDrawableChild(closeButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 渲染半透明的黑色背景
        renderBackground(context);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatable("gui.fabricmod.audio_device.title"),
            width / 2,
            20,
            0xFFFFFF
        );
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}