package com.example.fabricmod.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.example.fabricmod.ExampleMod.MOD_ID;
import static com.example.fabricmod.effect.ModEffects.GAMBLERS_BLESSING;

public class GamblerCardItem extends Item {

    public static final Identifier GAMBLER_CARD_ANIMATION = new Identifier(MOD_ID, "gambler_card_animation");

    public GamblerCardItem() {
        super(new FabricItemSettings().maxCount(64));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        // 设置使用时的冷却时间（20 ticks = 1秒）
        user.getItemCooldownManager().set(this, 20);
        
        if (!world.isClient) {  // 在服务端执行
            // 生成一个1-6的随机数
            int randomNumber = world.random.nextInt(6) + 1;
            
            // 向玩家发送消息
            user.sendMessage(Text.literal("你抽到了数字: " + randomNumber), true);
            
            // 给予状态效果 (5秒 = 100 ticks)
            user.addStatusEffect(new StatusEffectInstance(
                GAMBLERS_BLESSING,
                100,    // 持续时间改为固定5秒
                0,      // 等级
                false,  // 是否显示粒子
                true,   // 是否显示图标
                true    // 是否显示在物品栏
            ));
            
            // 发送动画数据包给所有能看到这个玩家的客户端
            PacketByteBuf animationBuf = PacketByteBufs.create();
            PlayerLookup.tracking(user).forEach(player ->
                ServerPlayNetworking.send(player, GAMBLER_CARD_ANIMATION, animationBuf));
            // 别忘了也给使用者自己发送
            if (user instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, GAMBLER_CARD_ANIMATION, animationBuf);
            }
            
            // 播放音效
            world.playSound(null, user.getX(), user.getY(), user.getZ(), 
                          SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 
                          SoundCategory.PLAYERS, 0.5f, 1.0f);
            
            // 如果是生存模式，消耗物品
            if (!user.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
        }
        
        return TypedActionResult.success(itemStack);
    }
} 