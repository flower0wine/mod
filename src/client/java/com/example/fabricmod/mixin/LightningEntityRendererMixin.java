package com.example.fabricmod.mixin;

import com.example.fabricmod.access.LightningAccess;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LightningEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.joml.Matrix4f;

@Mixin(LightningEntityRenderer.class)
public class LightningEntityRendererMixin {
    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private void onRender(LightningEntity lightningEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        int sharpnessLevel = ((LightningAccess)lightningEntity).getSharpnessLevel();
        if (sharpnessLevel >= 0) {
            // 如果是魔法棒的闪电，使用自定义渲染
            float[] fs = new float[8];
            float[] gs = new float[8];
            float h = 0.0F;
            float j = 0.0F;
            Random random = Random.create(lightningEntity.seed);

            for(int k = 7; k >= 0; --k) {
                fs[k] = h;
                gs[k] = j;
                h += (float)(random.nextInt(11) - 5);
                j += (float)(random.nextInt(11) - 5);
            }

            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLightning());
            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

            for(int l = 0; l < 4; ++l) {
                Random random2 = Random.create(lightningEntity.seed);

                for(int m = 0; m < 3; ++m) {
                    int n = 7;
                    int o = 0;
                    if (m > 0) {
                        n = 7 - m;
                    }

                    if (m > 0) {
                        o = n - 2;
                    }

                    float p = fs[n] - h;
                    float q = gs[n] - j;

                    for(int r = n; r >= o; --r) {
                        float s = p;
                        float t = q;
                        if (m == 0) {
                            p += (float)(random2.nextInt(11) - 5);
                            q += (float)(random2.nextInt(11) - 5);
                        } else {
                            p += (float)(random2.nextInt(31) - 15);
                            q += (float)(random2.nextInt(31) - 15);
                        }

                        float y = 0.1F + (float)l * 0.2F;
                        if (m == 0) {
                            y *= (float)r * 0.1F + 1.0F;
                        }

                        float z = 0.1F + (float)l * 0.2F;
                        if (m == 0) {
                            z *= ((float)r - 1.0F) * 0.1F + 1.0F;
                        }

                        // 根据锋利等级选择颜色
                        float red = 0.45F, green = 0.45F, blue = 0.5F;
                        switch(sharpnessLevel) {
                            case 1: // 白色 - 普通
                                red = 1.0f; green = 1.0f; blue = 1.0f;
                                break;
                            case 2: // 蓝色 - 稀有
                                red = 0.3f; green = 0.5f; blue = 1.0f;
                                break;
                            case 3: // 紫色 - 史诗
                                red = 0.7f; green = 0.3f; blue = 1.0f;
                                break;
                            case 4: // 金色/橙色 - 传说
                                red = 1.0f; green = 0.7f; blue = 0.0f;
                                break;
                            default: // 红色 - 神话
                                red = 1.0f; green = 0.2f; blue = 0.2f;
                        }

                        drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, false, false, true, false);
                        drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, true, false, true, true);
                        drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, true, true, false, true);
                        drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, false, true, false, false);
                    }
                }
            }
            
            ci.cancel(); // 取消原版渲染
        }
    }

    @Unique
    private static void drawBranch(Matrix4f matrix, VertexConsumer buffer, float x1, float z1, int y, float x2, float z2, float red, float green, float blue, float offset2, float offset1, boolean shiftEast1, boolean shiftSouth1, boolean shiftEast2, boolean shiftSouth2) {
        buffer.vertex(matrix, x1 + (shiftEast1 ? offset1 : -offset1), (float)(y * 16), z1 + (shiftSouth1 ? offset1 : -offset1)).color(red, green, blue, 0.3F).next();
        buffer.vertex(matrix, x2 + (shiftEast1 ? offset2 : -offset2), (float)((y + 1) * 16), z2 + (shiftSouth1 ? offset2 : -offset2)).color(red, green, blue, 0.3F).next();
        buffer.vertex(matrix, x2 + (shiftEast2 ? offset2 : -offset2), (float)((y + 1) * 16), z2 + (shiftSouth2 ? offset2 : -offset2)).color(red, green, blue, 0.3F).next();
        buffer.vertex(matrix, x1 + (shiftEast2 ? offset1 : -offset1), (float)(y * 16), z1 + (shiftSouth2 ? offset1 : -offset1)).color(red, green, blue, 0.3F).next();
    }
}