package com.example.fabricmod.effect;

import com.example.fabricmod.item.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import com.example.fabricmod.event.GamblerEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GamblersBlessingEffect extends StatusEffect {
    public GamblersBlessingEffect() {
        // 使用NEUTRAL类别，黄色粒子效果
        super(StatusEffectCategory.NEUTRAL, 0xFFD700);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) return;
        
        // 只在效果即将结束时触发事件（比如剩余5 ticks时）
        if (entity instanceof PlayerEntity player) {
            int duration = entity.getStatusEffect(this).getDuration();
            if (duration == 5) {
                triggerRandomEvent(player);
            }
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // 每tick都检查
        return true;
    }

    private void triggerRandomEvent(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld)) return;
        boolean eventTriggered = GamblerEvents.triggerRandomEvent(player);  // 使用随机事件分配方法
        if (!eventTriggered) {
            player.sendMessage(Text.literal("再来一次吧，赌徒...").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC), false);

            // 给予玩家新的赌徒卡牌
            ItemStack gamblerCard = new ItemStack(ModItems.GAMBLER_CARD);
            if (!player.giveItemStack(gamblerCard)) {
                // 如果物品栏满了，掉落在玩家位置
                player.dropItem(gamblerCard, true);
            }
        }
    }
}