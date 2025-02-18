package com.example.fabricmod.mixin;

import com.example.fabricmod.enchantment.ModEnchantments;
import com.example.fabricmod.enchantments.SwordAuraEnchantment;
import com.example.fabricmod.player.PlayerHoldManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeldItemRendererMixin.class);

    @ModifyVariable(
        method = "renderFirstPersonItem",
        at = @At(value = "HEAD"),
        ordinal = 0,
        argsOnly = true
    )
    private float modifySwingProgress(float swingProgress) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            ItemStack heldItem = client.player.getMainHandStack();
            if (heldItem.getItem() instanceof SwordItem && client.player.handSwinging) {
                return swingProgress * 0.5f; // 减缓挥砍动画速度
            }
        }
        return swingProgress;
    }

    @Inject(
        method = "applyEquipOffset",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onApplyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            ItemStack heldItem = client.player.getMainHandStack();
            
            if (heldItem.getItem() instanceof SwordItem) {
                // 获取剑气附魔等级
                int auraLevel = EnchantmentHelper.getLevel(
                    ModEnchantments.SWORD_AURA,
                    heldItem
                );
                
                // 只有具有剑气附魔的剑才处理蓄力
                if (auraLevel > 0) {
                    boolean isCharging = PlayerHoldManager.isCharging(client.player);
                    float chargeProgress = PlayerHoldManager.getChargeProgress(client.player);
                    
                    if (isCharging) {
                        // 蓄力动画：剑尖移动到左上角
                        float progress = Math.min(chargeProgress, 1.0f);
                        
                        matrices.push(); // 保存原始状态
                        
                        // 首先将剑整体抬高
                        matrices.translate(0.0F, 0.5F, 0.0F);
                        
                        // 剑逐渐移动到左上角
                        float upRotation = progress * 135.0f; // 向上抬起135度
                        float leftRotation = progress * -45.0f; // 向左偏转45度
                        
                        // 添加轻微的颤动，颤动强度随附魔等级增加
                        float wobble = MathHelper.sin(client.world.getTime() * 0.8f) * (2.0f + auraLevel * 0.5f) * progress;
                        
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-upRotation));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(leftRotation + wobble));
                        
                        // 调整位移，减少向下的偏移
                        float offset = progress * 0.2f;
                        matrices.translate(-offset, -offset * 0.2f, -offset);
                        
                        // 蓄力完成时的发光效果随附魔等级增强
                        if (progress >= 1.0f) {
                            float glow = MathHelper.sin(client.world.getTime() * 0.4f) * (0.1f + auraLevel * 0.05f) + 1.0f;
                            matrices.scale(glow, glow, glow);
                        }
                        
                        matrices.pop(); // 恢复原始状态
                        ci.cancel();
                    } else if (client.player.handSwinging) {
                        LOGGER.info("普通攻击动画");
                        // 普通攻击动画：横向挥砍
                        float f = equipProgress;
                        float g = MathHelper.sin(f * f * (float) Math.PI);
                        float h = MathHelper.sin(MathHelper.sqrt(f) * (float) Math.PI);
                        
                        matrices.push(); // 保存原始状态
                        
                        // 1. 首先将剑放置到水平位置
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F)); // 使剑水平
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));  // 使剑尖朝左
                        
                        // 2. 调整剑的基础位置
                        matrices.translate(0.0F, -0.5F, 0.0F); // 调整剑的中心点
                        
                        // 3. 执行挥砍动作
                        if (f < 0.5F) {
                            // 前半段动作：准备阶段，将剑向右方抬起
                            float prepareProgress = f * 2.0F; // 0.0 -> 1.0
                            float prepareRotation = prepareProgress * 90.0F; // 最大抬起90度，使剑完全水平
                            
                            // 将剑向右抬起到水平位置
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(prepareRotation));
                            
                            // 稍微向后拉，准备挥砍
                            float pullBack = prepareProgress * 0.3F;
                            matrices.translate(pullBack, 0.0F, -pullBack);
                        } else {
                            // 后半段动作：快速的横向挥砍
                            float slashProgress = (f - 0.5F) * 2.0F; // 0.0 -> 1.0
                            
                            // 从右向左的大幅度横向挥砍
                            float horizontalSlash = slashProgress * 180.0F; // 挥砍180度，从右到左
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F - horizontalSlash));
                            
                            // 添加一些向前的推进感
                            float thrustForward = (1.0F - slashProgress) * 0.3F;
                            matrices.translate(-thrustForward, 0.0F, -thrustForward * 0.5F);
                            
                            // 在挥砍过程中稍微倾斜剑身，使动作更自然
                            float tiltAngle = MathHelper.sin(slashProgress * (float)Math.PI) * 20.0F;
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(tiltAngle));
                        }
                        
                        // 4. 如果有剑气附魔，添加轨迹特效
                        if (auraLevel > 0) {
                            float glowIntensity = 1.0F + MathHelper.sin(f * (float)Math.PI) * 0.2F;
                            matrices.scale(glowIntensity, glowIntensity, glowIntensity);
                        }
                        
                        matrices.pop(); // 恢复原始状态
                        ci.cancel();
                    }
                }
            }
        }
    }
} 