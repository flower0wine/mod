package com.example.fabricmod.item;

import com.example.fabricmod.access.LightningAccess;
import com.example.fabricmod.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.HitResult;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import java.util.Random;
import static com.example.fabricmod.ExampleMod.MOD_ID;

public class MagicWandItem extends Item {

    public static final Identifier MAGIC_WAND_STRIKE = new Identifier(MOD_ID, "magic_wand_strike");

    public MagicWandItem() {
        super(new FabricItemSettings()
            .maxCount(1)  // 最大堆叠数量为1
            .maxDamage(100)  // 耐久度为100
            .rarity(Rarity.UNCOMMON));  // 设置为不常见物品
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantability() {
        return 15; // 与铁制工具相同的附魔能力
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        if (!world.isClient) {
            // 获取附魔等级
            int sharpnessLevel = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack);
            int thunderMasteryLevel = EnchantmentHelper.getLevel(ModEnchantments.THUNDER_MASTERY, stack);
            
            // 从玩家眼睛位置发射射线
            double maxDistance = 10.0;
            Vec3d startPos = user.getEyePos();
            Vec3d lookVec = user.getRotationVector();
            Vec3d endPos = startPos.add(lookVec.multiply(maxDistance));
            
            // 检测方块和实体
            BlockHitResult blockHit = world.raycast(new RaycastContext(
                startPos,
                endPos,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                user
            ));
            
            // 检测实体
            EntityHitResult entityHit = ProjectileUtil.getEntityCollision(
                world,
                user,
                startPos,
                endPos,
                user.getBoundingBox().stretch(lookVec.multiply(maxDistance)).expand(1.0),
                entity -> !entity.isSpectator() && entity.canHit()
            );
            
            // 确定闪电的目标位置
            Vec3d strikePos;
            if (entityHit != null && (blockHit.getType() == HitResult.Type.MISS || 
                entityHit.getPos().squaredDistanceTo(startPos) < blockHit.getPos().squaredDistanceTo(startPos))) {
                // 如果击中实体，并且实体比方块更近
                strikePos = entityHit.getPos();
            } else if (blockHit.getType() != HitResult.Type.MISS) {
                // 如果击中方块
                strikePos = blockHit.getPos();
            } else {
                // 如果什么都没击中
                return TypedActionResult.pass(stack);
            }
            
            // 根据雷霆之力等级生成多个闪电
            int lightningCount = 1 + thunderMasteryLevel; // 每级增加一道闪电
            double spreadRadius = 2.0 * thunderMasteryLevel; // 闪电分散范围随等级增加
            
            Random random = new Random();
            
            for (int i = 0; i < lightningCount; i++) {
                Vec3d offset;
                if (i == 0) {
                    offset = Vec3d.ZERO; // 第一道闪电击中目标点
                } else {
                    // 其他闪电随机分散
                    double angle = random.nextDouble() * Math.PI * 2;
                    double distance = random.nextDouble() * spreadRadius;
                    double heightVariation = (random.nextDouble() - 0.5) * 2.0; // -1.0 到 1.0 的随机高度变化
                    offset = new Vec3d(
                        Math.cos(angle) * distance,
                        heightVariation,  // 上下1格范围内的随机高度
                        Math.sin(angle) * distance
                    );
                }
                
                Vec3d lightningPos = strikePos.add(offset);
                
                // 生成闪电
                LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                lightning.setPos(lightningPos.x, lightningPos.y, lightningPos.z);
                ((LightningAccess)lightning).setSharpnessLevel(sharpnessLevel);
                world.spawnEntity(lightning);
                
                // 发送数据包到客户端
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeDouble(lightningPos.x);
                buf.writeDouble(lightningPos.y);
                buf.writeDouble(lightningPos.z);
                buf.writeInt(sharpnessLevel);
                PlayerLookup.tracking((ServerWorld)world, new BlockPos((int)lightningPos.x, (int)lightningPos.y, (int)lightningPos.z))
                    .forEach(player -> ServerPlayNetworking.send(player, MAGIC_WAND_STRIKE, buf));
            }
            
            // 播放音效
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS,
                1.0f, 1.0f);
            
            // 设置冷却时间（20 tick = 1秒）
            user.getItemCooldownManager().set(this, 20);
            
            // 消耗耐久度
            if (!user.getAbilities().creativeMode) {
                stack.damage(1, user, (p) -> p.sendToolBreakStatus(hand));
            }
        }
        
        return TypedActionResult.success(stack);
    }
} 