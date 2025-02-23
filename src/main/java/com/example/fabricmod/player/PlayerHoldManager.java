package com.example.fabricmod.player;

import com.example.fabricmod.enchantment.ModEnchantments;
import net.minecraft.server.network.ServerPlayerEntity;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import java.util.Map;
import com.example.fabricmod.ExampleMod;

public class PlayerHoldManager {
    private static final HashMap<UUID, PlayerHoldState> playerStates = new HashMap<>();
    private static final int CROSS_AURA_TIME = 10; // 1.5秒
    private static final Map<UUID, Long> holdStartTimes = new HashMap<>();
    private static final Map<UUID, Boolean> chargingStates = new HashMap<>();
    private static final float CHARGE_TIME = 20.0f; // 蓄力需要1秒 (20 ticks)

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
        SwordAuraManager.SwordAuraData config = registry.get(new Identifier(ExampleMod.MOD_ID, 
            state.holdTime >= 20 ? "cross" : "normal"));
        
        int level = EnchantmentHelper.getEquipmentLevel(
                ModEnchantments.SWORD_AURA,
                player);
            
        if (level <= 0) {
            resetHoldState(playerId);
            return;
        }
        
        if (pressed && !state.isHolding) {
            state.isHolding = true;
            holdStartTimes.put(playerId, System.currentTimeMillis());
            chargingStates.put(playerId, true);
        } else if (!pressed && state.isHolding) {
            if (state.holdTime >= config.effect().chargeTime()) {
                SwordAuraEffect.createCrossAura(player.getWorld(), player, level);
            } else if (state.holdTime > 1) {
                SwordAuraEffect.createAura(player.getWorld(), player, level);
            }
            resetHoldState(playerId);
            holdStartTimes.remove(playerId);
            chargingStates.put(playerId, false);
        }
    }
    
    public static void updateHoldTime(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        PlayerHoldState state = playerStates.get(playerId);
        if (state != null && state.isHolding) {
            state.holdTime++;
        }
    }
    
    private static void resetHoldState(UUID playerId) {
        PlayerHoldState state = playerStates.get(playerId);
        if (state != null && state.holdTime > 0) {
        }
        playerStates.remove(playerId);
    }
    
    public static void removePlayer(UUID playerId) {
        playerStates.remove(playerId);
    }

    public static boolean isCharging(PlayerEntity player) {
        return chargingStates.getOrDefault(player.getUuid(), false);
    }

    public static float getChargeProgress(PlayerEntity player) {
        UUID playerId = player.getUuid();
        Long startTime = holdStartTimes.get(playerId);
        if (startTime == null) return 0.0f;
        
        long currentTime = System.currentTimeMillis();
        float elapsedTicks = (currentTime - startTime) / 50.0f; // 转换为游戏刻
        return Math.min(elapsedTicks / CHARGE_TIME, 1.0f);
    }

    public static void clearPlayerData(UUID playerId) {
        holdStartTimes.remove(playerId);
        chargingStates.remove(playerId);
    }

    // 当玩家断开连接时清理数据
    public static void onPlayerDisconnect(ServerPlayerEntity player) {
        clearPlayerData(player.getUuid());
    }
} 