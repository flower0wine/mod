package com.example.fabricmod.render;

import com.example.fabricmod.entity.WeaponDisplayEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class WeaponDisplayEntityRenderer extends EntityRenderer<WeaponDisplayEntity> {
    private final ItemRenderer itemRenderer;

    public WeaponDisplayEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(WeaponDisplayEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(240));  // 先让剑竖起来
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));  // 稍微旋转一下，让剑面向正面
        matrices.translate(0, -1.5, 0);
        matrices.scale(3.0f, 3.0f, 3.0f);
        
        // 渲染物品
        ItemStack stack = entity.getDisplayItem();
        if (!stack.isEmpty()) {
            this.itemRenderer.renderItem(
                stack,
                ModelTransformationMode.GROUND,
                light,
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                entity.getWorld(),
                0
            );
        }
        
        matrices.pop();
    }

    @Override
    public Identifier getTexture(WeaponDisplayEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}