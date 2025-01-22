package com.example.fabricmod.player;

import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.item.SwordItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.Registries;
import com.example.fabricmod.enchantments.SwordAuraEnchantment;
import com.example.fabricmod.effects.SwordAuraEffect;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.example.fabricmod.data.SwordAuraManager;

public class PlayerHoldManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("SwordAuraMod");
    private static final HashMap<UUID, PlayerHoldState> playerStates = new HashMap<>();
    private static final int CROSS_AURA_TIME = 10; // 1.5秒

    private static class PlayerHoldState {
        boolean isHolding = false;
        int holdTime = 0;
    }
    
    public static void handleMouseState(ServerPlayerEntity player, boolean pressed) {
        UUID playerId = player.getUuid();
        PlayerHoldState state = playerStates.computeIfAbsent(playerId, k -> new PlayerHoldState());
        
        if (!(player.getMainHandStack().getItem() instanceof SwordItem)) {
            resetHoldState(playerId);
            return;
        }
        
        Registry<SwordAuraManager.SwordAuraData> registry = player.getWorld().getRegistryManager().get(SwordAuraManager.SWORD_AURA_KEY);
        SwordAuraManager.SwordAuraData config = registry.get(new Identifier("fabricmod", 
            state.holdTime >= 20 ? "cross" : "normal"));
        
        int level = EnchantmentHelper.getEquipmentLevel(
            Registries.ENCHANTMENT.get(SwordAuraEnchantment.SWORD_AURA.getValue()),
            player);
            
        if (level <= 0) {
            resetHoldState(playerId);
            return;
        }
        
        if (pressed && !state.isHolding) {
            state.isHolding = true;
            LOGGER.info("开始蓄力");
        } else if (!pressed && state.isHolding) {
            if (state.holdTime >= config.effect().chargeTime()) {
                LOGGER.info("释放交叉剑气！等级: {}", level);
                SwordAuraEffect.createCrossAura(player.getWorld(), player, level);
            } else if (state.holdTime > 1) {
                LOGGER.info("释放普通剑气！等级: {}", level);
                SwordAuraEffect.createAura(player.getWorld(), player, level);
            }
            resetHoldState(playerId);
        }
    }
    
    public static void updateHoldTime(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        PlayerHoldState state = playerStates.get(playerId);
        if (state != null && state.isHolding) {
            state.holdTime++;
            LOGGER.info("蓄力中... 时间: {} ticks", state.holdTime);
        }
    }
    
    private static void resetHoldState(UUID playerId) {
        PlayerHoldState state = playerStates.get(playerId);
        if (state != null && state.holdTime > 0) {
            LOGGER.info("重置长按状态，最终时间: {} ticks", state.holdTime);
        }
        playerStates.remove(playerId);
    }
    
    public static void removePlayer(UUID playerId) {
        playerStates.remove(playerId);
    }
} 