package com.example.fabricmod.mixin;

import com.example.fabricmod.data.BaseEntanglementData;
import com.example.fabricmod.data.GoldenEntanglementData;
import com.example.fabricmod.manager.EntanglementManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.UUID;

@Mixin(LivingEntity.class)
public class EntityDamageMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (entity.getWorld().isClient) return;

        for (var dataGetter : EntanglementManager.ENTANGLEMENT_TYPES) {
            BaseEntanglementData data = dataGetter.apply(entity.getWorld());
            if (data != null && data.isTransmitter(entity.getUuid())) {
                Set<UUID> receivers = data.getReceivers(entity.getWorld());

                if (!receivers.isEmpty() && EntanglementManager.handleDamage(entity, amount, data)) {
                    // 取消伤害
                    cir.setReturnValue(false);
                }
            }
        }
    }
} 