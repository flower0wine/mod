package com.example.fabricmod.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.example.fabricmod.access.ThiefAccess;
import com.example.fabricmod.ExampleMod;

@Mixin(VillagerResemblingModel.class)
public class VillagerEntityModelMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleMod.MOD_ID);
    
    @Shadow @Final private ModelPart head;

    @Inject(method = "setAngles", at = @At("RETURN"))
    public void setAngles(Entity entity, float limbAngle, float limbDistance, 
                         float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof VillagerEntity villager) {

            if (villager instanceof ThiefAccess thiefAccess) {
                boolean isThief = thiefAccess.isThief();

                if (isThief) {
                    float pitch = 0.5F;
                    if (limbDistance > 0.01F) {
                        pitch += MathHelper.sin(limbAngle * 0.8F) * 0.1F;
                    }
                    
                    this.head.setAngles(
                        pitch,
                        headYaw * ((float)Math.PI / 180F),
                        0.0F
                    );
                }
            }
        }
    }
} 