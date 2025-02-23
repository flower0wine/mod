package com.example.fabricmod.event;

import com.example.fabricmod.ExampleMod;
import com.example.fabricmod.enchantment.ModEnchantments;
import com.example.fabricmod.entity.ModEntities;
import com.example.fabricmod.entity.WeaponDisplayEntity;
import com.example.fabricmod.item.ModItems;
import com.example.fabricmod.util.WeightedRandom;
import net.minecraft.advancement.Advancement;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class GamblerEvents {

    public static void main(String[] args) {
        WeightedRandom<GamblerEventType> weightedRandom = new WeightedRandom<>(
                GamblerEventType.values(),
                GamblerEventType::getWeight
        );

        List<GamblerEventType> sequence = new ArrayList<>();
        int testCount = 100000;

        // 生成序列
        for (int i = 0; i < testCount; i++) {
            sequence.add(weightedRandom.next());
        }

        // 统计每个事件出现的次数
        Map<GamblerEventType, Long> counts = sequence.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        // 计算总权重
        int totalWeight = Arrays.stream(GamblerEventType.values())
                .mapToInt(GamblerEventType::getWeight)
                .sum();

        // 打印统计结果
        System.out.println("总测试次数: " + testCount);
        System.out.println("总权重: " + totalWeight);
        System.out.println("\n事件统计:");
        System.out.println("事件名称\t权重\t期望概率\t实际次数\t实际概率");
        System.out.println("----------------------------------------");

        counts.forEach((event, count) -> {
            double expectedProb = (double) event.getWeight() / totalWeight;
            double actualProb = (double) count / testCount;
            System.out.printf("%s\t%d\t%.3f\t%d\t%.3f%n",
                    event.name(),
                    event.getWeight(),
                    expectedProb,
                    count,
                    actualProb
            );
        });

        // 检查是否有未出现的事件
        Arrays.stream(GamblerEventType.values())
                .filter(event -> !counts.containsKey(event))
                .forEach(event -> {
                    double expectedProb = (double) event.getWeight() / totalWeight;
                    System.out.printf("%s\t%d\t%.3f\t0\t0.000%n",
                            event.name(),
                            event.getWeight(),
                            expectedProb
                    );
                });
    }

    /**
     * 赌徒事件枚举，包含事件方法引用和权重
     */
    private enum GamblerEventType {
        NO_EVENT(GamblerEvents::noEvent, 7),                    // 空事件，什么都不会发生
        SUMMON_LIGHTNING(GamblerEvents::summonLightning, 10),    // 在玩家位置生成闪电
        CREATE_LAVA_CROSS(GamblerEvents::createLavaCross, 10),   // 在玩家头顶生成十字形岩浆
        SPEEDY_VILLAGERS(GamblerEvents::spawnSpeedyVillagers, 5),  // 生成10个超级快速的村民
        ENCHANTED_SWORD(GamblerEvents::giveEnchantedSword, 5),    // 给予玩家附魔钻石剑（锋利X，剑气III）
        ENCHANTED_WAND(GamblerEvents::giveEnchantedWand, 4),      // 给予玩家附魔魔法棒（万雷V，锋利V）
        HOSTILE_MOBS(GamblerEvents::spawnHostileMobs, 8),         // 在玩家周围生成20个随机敌对生物
        SPAWN_WITHERS(GamblerEvents::spawnWithers, 1),            // 生成3个凋零
        IRON_GOLEMS(GamblerEvents::spawnIronGolems, 8),          // 生成8个友好的铁傀儡
        GODLY_POWER(GamblerEvents::giveGodlyPower, 3),           // 给予玩家超强力量、再生和伤害吸收效果
        GAMBLE_LIFE(GamblerEvents::gambleWithLife, 7),           // 与周围生物赌命，50%概率杀死它们或被杀死
        GAMBLE_FALL(GamblerEvents::gambleWithFall, 7),           // 将玩家传送至高空，给予水桶
        NETHERITE_GEAR(GamblerEvents::giveNetheriteGearAndPortal, 2),  // 给予全套下界合金装备并开启末地传送门
        DROP_WEAPONS(GamblerEvents::dropNetheriteWeapons, 4),     // 从天空掉落大量附魔钻石剑
        HUNGER_BREAD(GamblerEvents::giveHungerAndBread, 6),      // 给予极度饥饿效果和一组面包
        SPEED_EFFECTS(GamblerEvents::giveSpeedEffects, 6),       // 给予超高等级的速度、急迫和跳跃提升效果
        TELEPORT_VILLAGE(GamblerEvents::teleportToVillage, 4),   // 将玩家传送到最近的村庄
        LEVITATION(GamblerEvents::giveLevitation, 5),            // 给予玩家强力悬浮效果
        BED_VILLAGER(GamblerEvents::spawnBedAndVillager, 7),     // 生成床和名为"杰哥"的村民，并脱掉玩家盔甲
        ENDER_PEARLS(GamblerEvents::throwEnderPearls, 8),        // 向四周发射10颗末影珍珠
        TELEPORT_NETHER(GamblerEvents::teleportToNether, 2);     // 将玩家随机传送到地狱的某个位置

        private final GamblerEvent event;
        private final int weight;

        GamblerEventType(GamblerEvent event, int weight) {
            this.event = event;
            this.weight = weight;
        }

        public GamblerEvent getEvent() {
            return event;
        }

        public int getWeight() {
            return weight;
        }
    }

    // 静态权重随机实例
    private static WeightedRandom<GamblerEventType> weightedRandom;
    
    // 为每个玩家维护事件序列
    private static final Map<UUID, List<GamblerEventType>> playerEventSequences = new HashMap<>();
    private static final Map<UUID, Integer> playerCurrentIndices = new HashMap<>();

    /**
     * 初始化权重随机器
     */
    private static void initWeightedRandom() {
        if (weightedRandom == null) {
            weightedRandom = new WeightedRandom<>(
                GamblerEventType.values(),
                GamblerEventType::getWeight
            );
        }
    }

    /**
     * 生成加权随机事件序列
     * 权重越大的事件出现的概率越大
     */
    private static List<GamblerEventType> generateWeightedSequence() {
        initWeightedRandom();
        
        // 生成一个包含8个事件的序列
        // 你可以根据需要调整这个数值
        int sequenceLength = 8;
        List<GamblerEventType> sequence = new ArrayList<>();
        
        for (int i = 0; i < sequenceLength; i++) {
            sequence.add(weightedRandom.next());
        }
        
        return sequence;
    }

    /**
     * 随机触发一个事件
     */
    public static boolean triggerRandomEvent(PlayerEntity player) {
        UUID playerId = player.getUuid();
        List<GamblerEventType> sequence = playerEventSequences.get(playerId);
        Integer index = playerCurrentIndices.get(playerId);
        
        // 如果序列为空或已用完，重新生成
        if (sequence == null || index == null || index >= sequence.size()) {
            sequence = generateWeightedSequence();
            playerEventSequences.put(playerId, sequence);
            playerCurrentIndices.put(playerId, 0);
            index = 0;
        }
        
        // 获取并触发当前事件
        GamblerEventType currentEvent = sequence.get(index);
        playerCurrentIndices.put(playerId, index + 1);
        
        return currentEvent.getEvent().trigger(player);
    }

    // 定义事件接口
    @FunctionalInterface
    private interface GamblerEvent {
        boolean trigger(PlayerEntity player);
    }

    /**
     * 空事件 - 表示未触发任何事件
     */
    public static boolean noEvent(PlayerEntity player) {
        return false;  // 返回false表示这是一个空事件
    }
    
    /**
     * 在玩家位置生成闪电
     */
    public static boolean summonLightning(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        BlockPos pos = player.getBlockPos();
        EntityType.LIGHTNING_BOLT.spawn(
            serverWorld,
            pos,
            SpawnReason.EVENT
        );

        player.sendMessage(Text.literal("上天降下神罚！").formatted(Formatting.YELLOW, Formatting.BOLD), false);
        return true;
    }
    
    /**
     * 在玩家头顶生成十字形岩浆
     */
    public static boolean createLavaCross(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        // 获取玩家头顶3格位置作为中心点
        BlockPos centerPos = player.getBlockPos().up(2);
        
        // 生成十字形岩浆
        BlockPos[] lavaPositions = {
            centerPos,                    // 中心
            centerPos.north(),           // 北
            centerPos.south(),           // 南
            centerPos.east(),            // 东
            centerPos.west()             // 西
        };
        
        // 放置岩浆
        for (BlockPos pos : lavaPositions) {
            // 检查位置是否为空气或可替换方块
            if (serverWorld.getBlockState(pos).isReplaceable()) {
                serverWorld.setBlockState(pos, Blocks.LAVA.getDefaultState());
            }
        }

        player.sendMessage(Text.literal("赌神以炼狱之火考验着你的信仰...").formatted(Formatting.GOLD, Formatting.ITALIC), false);

        return true;
    }
    
    /**
     * 在玩家附近生成快速村民
     */
    public static boolean spawnSpeedyVillagers(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        BlockPos playerPos = player.getBlockPos();
        
        // 生成10个村民
        for (int i = 0; i < 10; i++) {
            // 在玩家周围3格范围内随机位置生成
            int offsetX = serverWorld.getRandom().nextInt(7) - 3;
            int offsetZ = serverWorld.getRandom().nextInt(7) - 3;
            
            BlockPos spawnPos = playerPos.add(offsetX, 0, offsetZ);
            
            // 生成村民
            VillagerEntity villager = EntityType.VILLAGER.create(serverWorld);
            if (villager != null) {
                villager.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    0.0F,
                    0.0F
                );
                
                // 给予速度效果 (等级4对应速度5，持续时间100秒)
                villager.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED,
                    2000,  // 100秒
                    4,     // 速度等级5
                    false, // 是否显示粒子
                    false  // 是否显示图标
                ));
                
                serverWorld.spawnEntity(villager);
            }
        }

        player.sendMessage(Text.literal("赌神派来了他最快的信徒们...").formatted(Formatting.GREEN, Formatting.ITALIC), false);
        
        return true;
    }
    
    /**
     * 给予玩家附魔钻石剑
     */
    public static boolean giveEnchantedSword(PlayerEntity player) {
        // 创建钻石剑
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        
        // 添加锋利X
        sword.addEnchantment(Enchantments.SHARPNESS, 10);
        
        // 添加剑气III
        sword.addEnchantment(ModEnchantments.SWORD_AURA, 3);
        
        // 给予玩家物品
        if (!player.giveItemStack(sword)) {
            // 如果物品栏满了，掉落在玩家位置
            player.dropItem(sword, true);
        }

        player.sendMessage(Text.literal("神铸造的利刃，赌徒的奖赏！").formatted(Formatting.AQUA, Formatting.BOLD), false);

        return true;
    }
    
    /**
     * 给予玩家附魔魔法棒
     */
    public static boolean giveEnchantedWand(PlayerEntity player) {
        // 创建魔法棒
        ItemStack wand = new ItemStack(ModItems.MAGIC_WAND);
        
        // 添加万雷V
        wand.addEnchantment(ModEnchantments.THUNDER_MASTERY, 5);
        
        // 添加锋利V
        wand.addEnchantment(Enchantments.SHARPNESS, 5);
        
        // 给予玩家物品
        if (!player.giveItemStack(wand)) {
            // 如果物品栏满了，掉落在玩家位置
            player.dropItem(wand, true);
        }

        player.sendMessage(Text.literal("众神将雷霆之力注入这根魔杖！").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), false);

        return true;
    }
    
    /**
     * 在玩家周围生成大量怪物
     */
    public static boolean spawnHostileMobs(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        BlockPos playerPos = player.getBlockPos();
        EntityType<?>[] mobTypes = {
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.CREEPER,
            EntityType.SPIDER,
            EntityType.WITCH,           // 女巫，可以投掷药水
            EntityType.BLAZE,          // 烈焰人，可以发射火球
            EntityType.WITHER_SKELETON, // 凋零骷髅，带有凋零效果
            EntityType.RAVAGER,        // 劫掠兽，非常强大
            EntityType.VINDICATOR,     // 卫道士，持斧头攻击
            EntityType.EVOKER,         // 唤魔者，可以召唤尖牙
            EntityType.PIGLIN_BRUTE,   // 猪灵蛮兵，非常强大
            EntityType.GUARDIAN,       // 守卫者，可以发射激光
            EntityType.ELDER_GUARDIAN, // 远古守卫者，更强的守卫者
            EntityType.SHULKER,       // 潜影贝，可以让玩家漂浮
            EntityType.PILLAGER,      // 掠夺者，带有弩
            EntityType.GHAST,         // 恶魂，可以发射火球
            EntityType.MAGMA_CUBE,    // 岩浆怪，免疫火焰
            EntityType.HOGLIN,        // 疣猪兽，非常强大的近战单位
            EntityType.ZOGLIN,        // 僵尸疣猪兽，更具攻击性
            EntityType.VEX            // 恼鬼，可以穿墙的飞行生物
        };
        
        // 在玩家周围3格范围内生成20个随机怪物
        for (int i = 0; i < 20; i++) {
            // 随机选择一个怪物类型
            EntityType<?> mobType = mobTypes[serverWorld.getRandom().nextInt(mobTypes.length)];
            
            // 随机生成位置
            int offsetX = serverWorld.getRandom().nextInt(7) - 3; // -3 到 3
            int offsetZ = serverWorld.getRandom().nextInt(7) - 3; // -3 到 3
            
            BlockPos spawnPos = playerPos.add(offsetX, 0, offsetZ);
            
            // 生成怪物
            mobType.spawn(
                serverWorld,
                spawnPos,
                SpawnReason.EVENT
            );
        }

        player.sendMessage(Text.literal("主的怒火唤醒了地狱的军团！").formatted(Formatting.DARK_RED, Formatting.BOLD), false);

        return true;
    }
    
    /**
     * 在玩家周围生成三个凋零
     */
    public static boolean spawnWithers(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        BlockPos playerPos = player.getBlockPos();
        
        // 生成3个凋零
        for (int i = 0; i < 3; i++) {
            // 随机生成位置
            int offsetX = serverWorld.getRandom().nextInt(7) - 3; // -3 到 3
            int offsetZ = serverWorld.getRandom().nextInt(7) - 3; // -3 到 3
            
            BlockPos spawnPos = playerPos.add(offsetX, 0, offsetZ);
            
            // 生成凋零
            EntityType.WITHER.spawn(
                serverWorld,
                spawnPos,
                SpawnReason.EVENT
            );
        }

        player.sendMessage(Text.literal("主的震怒化作了三位死神！").formatted(Formatting.DARK_GRAY, Formatting.BOLD), false);

        return true;
    }
    
    /**
     * 在玩家周围生成铁傀儡
     */
    public static boolean spawnIronGolems(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        BlockPos playerPos = player.getBlockPos();
        
        // 生成8个铁傀儡
        for (int i = 0; i < 8; i++) {
            // 随机生成位置（2格范围内）
            int offsetX = serverWorld.getRandom().nextInt(5) - 2; // -2 到 2
            int offsetZ = serverWorld.getRandom().nextInt(5) - 2; // -2 到 2
            
            BlockPos spawnPos = playerPos.add(offsetX, 0, offsetZ);
            
            // 生成铁傀儡
            IronGolemEntity golem = EntityType.IRON_GOLEM.create(serverWorld);
            if (golem != null) {
                golem.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    0.0F,
                    0.0F
                );
                
                // 设置为玩家召唤的铁傀儡（不会攻击玩家）
                golem.setPlayerCreated(true);
                
                serverWorld.spawnEntity(golem);
            }
        }

        player.sendMessage(Text.literal("上苍的护卫降临在你身边！").formatted(Formatting.GRAY, Formatting.BOLD), false);
        return true;
    }

    /**
     * 给予玩家神之力（超强增益效果）
     */
    public static boolean giveGodlyPower(PlayerEntity player) {
        // 创建效果实例（每个效果持续60秒 = 1200 ticks）
        StatusEffectInstance strength = new StatusEffectInstance(
            StatusEffects.STRENGTH,
            1200,           // 持续时间（ticks）
            254,           // 等级 (255 = 等级 255)
            false,         // 是否显示粒子
            true,          // 是否显示图标
            true           // 是否显示在物品栏
        );
        
        StatusEffectInstance regeneration = new StatusEffectInstance(
            StatusEffects.REGENERATION,
            1200,
            254,
            false,
            true,
            true
        );
        
        StatusEffectInstance absorption = new StatusEffectInstance(
            StatusEffects.ABSORPTION,
            1200,
            254,
            false,
            true,
            true
        );
        
        // 给予玩家效果
        player.addStatusEffect(strength);
        player.addStatusEffect(regeneration);
        player.addStatusEffect(absorption);
        
        // 发送消息提醒
        player.sendMessage(Text.translatable("主的神力注入你的血脉！").formatted(Formatting.GOLD, Formatting.BOLD), true);
        return true;
    }
    
    /**
     * 与周围生物赌命
     */
    public static boolean gambleWithLife(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        // 获取玩家周围10格范围内的所有生物
        List<LivingEntity> nearbyEntities = serverWorld.getEntitiesByClass(
            LivingEntity.class,
            player.getBoundingBox().expand(10.0),
            entity -> entity != player && !(entity instanceof PlayerEntity)
        );
        
        // 如果周围没有生物，返回false
        if (nearbyEntities.isEmpty()) {
            player.sendMessage(Text.translatable("没人愿和一个赌徒赌命！").formatted(Formatting.RED), true);
            return false;
        }
        
        // 50%的胜率
        boolean playerWins = serverWorld.getRandom().nextBoolean();
        
        if (playerWins) {
            // 玩家胜利，处决所有周围生物
            for (LivingEntity entity : nearbyEntities) {
                // 生成闪电特效
                EntityType.LIGHTNING_BOLT.spawn(
                    serverWorld,
                    entity.getBlockPos(),
                    SpawnReason.EVENT
                );
                // 立即处决生物
                entity.kill();
            }
            // 发送胜利消息
            player.sendMessage(Text.translatable("命运之轮转向了你！")
                .formatted(Formatting.GOLD, Formatting.BOLD), true);
        } else {
            // 玩家失败，处决玩家
            // 生成闪电特效
            EntityType.LIGHTNING_BOLT.spawn(
                serverWorld,
                player.getBlockPos(),
                SpawnReason.EVENT
            );
            // 立即处决玩家
            player.kill();
            // 发送失败消息
            player.sendMessage(Text.translatable("这就是赌徒的宿命！")
                .formatted(Formatting.RED, Formatting.BOLD), true);
        }
        
        return true;
    }
    
    /**
     * 赌徒跳伞
     */
    public static boolean gambleWithFall(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        // 保存玩家当前手持物品
        ItemStack oldItem = player.getMainHandStack();
        
        // 替换为水桶
        ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
        player.setStackInHand(player.getActiveHand(), waterBucket);
        
        // 如果之前手上有物品，掉落它
        if (!oldItem.isEmpty()) {
            player.dropItem(oldItem, true);
        }
        
        // 传送玩家到高空
        BlockPos currentPos = player.getBlockPos();
        player.teleport(
            currentPos.getX(),
            255,  // Y坐标设为255
            currentPos.getZ()
        );
        
        // 发送沉浸式消息
        player.sendMessage(Text.translatable("命运将你抛向高空！你就是那枚硬币！")
            .formatted(Formatting.GOLD, Formatting.BOLD), true);
        
        return true;
    }
    
    /**
     * 给予玩家顶级装备并开启末地传送门
     */
    public static boolean giveNetheriteGearAndPortal(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        // 创建全套下界合金装备
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        
        // 添加通用附魔
        helmet.addEnchantment(Enchantments.PROTECTION, 4);
        helmet.addEnchantment(Enchantments.UNBREAKING, 3);
        helmet.addEnchantment(Enchantments.MENDING, 1);
        helmet.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
        helmet.addEnchantment(Enchantments.RESPIRATION, 3);
        
        chestplate.addEnchantment(Enchantments.PROTECTION, 4);
        chestplate.addEnchantment(Enchantments.UNBREAKING, 3);
        chestplate.addEnchantment(Enchantments.MENDING, 1);
        
        leggings.addEnchantment(Enchantments.PROTECTION, 4);
        leggings.addEnchantment(Enchantments.UNBREAKING, 3);
        leggings.addEnchantment(Enchantments.MENDING, 1);
        
        boots.addEnchantment(Enchantments.PROTECTION, 4);
        boots.addEnchantment(Enchantments.UNBREAKING, 3);
        boots.addEnchantment(Enchantments.MENDING, 1);
        boots.addEnchantment(Enchantments.FEATHER_FALLING, 4);
        boots.addEnchantment(Enchantments.DEPTH_STRIDER, 3);
        
        sword.addEnchantment(Enchantments.SHARPNESS, 5);
        sword.addEnchantment(Enchantments.UNBREAKING, 3);
        sword.addEnchantment(Enchantments.MENDING, 1);
        sword.addEnchantment(Enchantments.LOOTING, 3);
        sword.addEnchantment(Enchantments.SWEEPING, 3);
        sword.addEnchantment(Enchantments.FIRE_ASPECT, 2);
        
        // 给予装备
        player.getInventory().armor.set(3, helmet);      // 头盔
        player.getInventory().armor.set(2, chestplate);  // 胸甲
        player.getInventory().armor.set(1, leggings);    // 护腿
        player.getInventory().armor.set(0, boots);       // 靴子
        
        // 给予剑
        if (!player.giveItemStack(sword)) {
            player.dropItem(sword, true);
        }
        
        // 获取玩家面前的位置（2格距离）
        BlockPos portalPos = player.getBlockPos().offset(
            player.getHorizontalFacing(),  // 使用玩家朝向
            4                              // 距离
        );
        
        // 清理区域（5x5x3）以确保有足够空间
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos clearPos = portalPos.add(x, y, z);
                    serverWorld.setBlockState(clearPos, Blocks.AIR.getDefaultState());
                }
            }
        }
        
        // 创建末地传送门框架（5x5，不包括四个角）
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // 跳过四个角落
                if (Math.abs(x) == 2 && Math.abs(z) == 2) {
                    continue;
                }
                // 只在外圈放置框架方块
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    BlockPos framePos = portalPos.add(x, 0, z);
                    serverWorld.setBlockState(framePos, Blocks.END_PORTAL_FRAME.getDefaultState()
                        .with(EndPortalFrameBlock.EYE, true)  // 放置末影之眼
                        .with(EndPortalFrameBlock.FACING, player.getHorizontalFacing()));
                }
            }
        }
        
        // 在3x3的中心区域创建末地传送门（与框架同高度）
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos portalBlockPos = portalPos.add(x, 0, z);
                serverWorld.setBlockState(portalBlockPos, Blocks.END_PORTAL.getDefaultState());
            }
        }
        
        // 授予成就
        Advancement advancement = serverWorld.getServer().getAdvancementLoader()
            .get(new Identifier(ExampleMod.MOD_ID, "go_brave_warrior"));
        if (advancement != null) {
            ((ServerPlayerEntity)player).getAdvancementTracker().grantCriterion(advancement, "got_gear");
        }
        
        // 发送消息
        player.sendMessage(Text.translatable("命运为你开启了通往终末之地的大门！")
            .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), true);
        
        return true;
    }

    /**
     * 天降神兵
     */
    public static boolean dropNetheriteWeapons(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        // 获取玩家头顶10格的位置作为圆心
        Vec3d center = player.getPos().add(0, 10, 0);
        int numSwords = 240;  // 生成剑的数量
        float maxRadius = 10.0f; // 圆的最大半径
        Random random = new Random();
        
        for (int i = 0; i < numSwords; i++) {
            // 使用平方根来确保分布均匀（否则中心会过于密集）
            double sqrtR = Math.sqrt(random.nextDouble());
            double radius = maxRadius * sqrtR;
            
            // 随机角度
            double angle = random.nextDouble() * 2 * Math.PI;
            
            // 计算剑的位置
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            
            // 创建展示实体
            WeaponDisplayEntity display = new WeaponDisplayEntity(ModEntities.WEAPON_DISPLAY, serverWorld);
            display.setPosition(x, center.y, z);
            
            // 创建一把钻石剑并添加附魔
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.addEnchantment(Enchantments.SHARPNESS, 5);
            
            // 设置展示的物品
            display.setDisplayItem(sword);
            
            // 生成实体
            serverWorld.spawnEntity(display);
        }

        // 发送消息
        player.sendMessage(Text.literal("神兵从天而降！").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), false);
        
        return true;
    }

    /**
     * 给予玩家极度饥饿效果和面包
     */
    public static boolean giveHungerAndBread(PlayerEntity player) {
        // 创建饥饿效果实例（1秒 = 20 ticks）
        StatusEffectInstance hunger = new StatusEffectInstance(
            StatusEffects.HUNGER,
            1200,           // 持续时间（ticks）
            254,            // 等级 (255)
            false,        // 是否显示粒子
            true,         // 是否显示图标
            true          // 是否显示在物品栏
        );
        
        // 给予玩家饥饿效果
        player.addStatusEffect(hunger);

        // 直接减少玩家的饱食度
        player.getHungerManager().setFoodLevel(1);  // 设置为最低饱食度
        player.getHungerManager().setSaturationLevel(0.0f);  // 设置为最低饱和度
        
        // 保存玩家当前手持物品
        ItemStack oldItem = player.getMainHandStack();
        
        // 创建一组面包并替换主手物品
        ItemStack bread = new ItemStack(Items.BREAD, 64);
        player.setStackInHand(player.getActiveHand(), bread);
        
        // 如果之前手上有物品，掉落它
        if (!oldItem.isEmpty()) {
            player.dropItem(oldItem, true);
        }
        
        // 发送消息
        player.sendMessage(Text.literal("事已至此，先吃饭吧！").formatted(Formatting.RED, Formatting.ITALIC), false);
        
        return true;
    }

    /**
     * 给予玩家极速效果（急迫、速度、跳跃提升）
     */
    public static boolean giveSpeedEffects(PlayerEntity player) {
        // 创建效果实例（持续30秒 = 600 ticks）
        StatusEffectInstance haste = new StatusEffectInstance(
            StatusEffects.HASTE,
            600,           // 持续时间（ticks）
            9,            // 等级 (9 = 等级 10)
            false,        // 是否显示粒子
            true,         // 是否显示图标
            true          // 是否显示在物品栏
        );
        
        StatusEffectInstance speed = new StatusEffectInstance(
            StatusEffects.SPEED,
            600,
            9,
            false,
            true,
            true
        );
        
        StatusEffectInstance jump = new StatusEffectInstance(
            StatusEffects.JUMP_BOOST,
            600,
            9,
            false,
            true,
            true
        );
        
        // 给予玩家效果
        player.addStatusEffect(haste);
        player.addStatusEffect(speed);
        player.addStatusEffect(jump);
        
        // 播放音效
        player.getWorld().playSound(
            null,
            player.getBlockPos(),
            SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
            SoundCategory.PLAYERS,
            1.0f,
            1.0f
        );
        
        // 生成粒子效果
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                ParticleTypes.END_ROD,
                player.getX(),
                player.getY() + 1,
                player.getZ(),
                50,  // 粒子数量
                0.5, // X扩散范围
                0.5, // Y扩散范围
                0.5, // Z扩散范围
                0.1  // 速度
            );
        }
        
        // 发送消息
        player.sendMessage(Text.literal("赌神封你为急急国王！").formatted(Formatting.AQUA, Formatting.BOLD), false);
        
        return true;
    }

    /**
     * 传送玩家到附近的村庄
     */
    public static boolean teleportToVillage(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        // 寻找最近的村庄
        BlockPos villagePos = serverWorld.locateStructure(
            StructureTags.VILLAGE,
            player.getBlockPos(),
            32,  // 搜索半径（区块）
            false  // 不跳过已引用的结构
        );

        if (villagePos == null) {
            player.sendMessage(Text.literal("赌神想将你卖到村庄，但没有人收你！").formatted(Formatting.RED), false);
            return true;
        }

        // 找到地面位置
        BlockPos groundPos = findSafeGroundPos(serverWorld, villagePos);
        if (groundPos == null) {
            groundPos = villagePos;
        }

        // 传送前生成粒子效果
        serverWorld.spawnParticles(
            ParticleTypes.PORTAL,
            player.getX(),
            player.getY(),
            player.getZ(),
            50,  // 粒子数量
            0.5, // X扩散范围
            1.0, // Y扩散范围
            0.5, // Z扩散范围
            0.1  // 速度
        );

        // 播放传送音效
        serverWorld.playSound(
            null,
            player.getBlockPos(),
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.PLAYERS,
            1.0f,
            1.0f
        );

        // 传送玩家
        player.teleport(
            groundPos.getX() + 0.5,
            groundPos.getY() + 1,
            groundPos.getZ() + 0.5
        );

        // 传送后生成粒子效果
        serverWorld.spawnParticles(
            ParticleTypes.PORTAL,
            groundPos.getX() + 0.5,
            groundPos.getY() + 1,
            groundPos.getZ() + 0.5,
            50,  // 粒子数量
            0.5, // X扩散范围
            1.0, // Y扩散范围
            0.5, // Z扩散范围
            0.1  // 速度
        );

        // 发送消息
        player.sendMessage(Text.literal("赌神将你卖到了村庄！").formatted(Formatting.GREEN, Formatting.ITALIC), false);

        return true;
    }

    /**
     * 寻找安全的地面位置
     */
    private static BlockPos findSafeGroundPos(ServerWorld world, BlockPos pos) {
        // 从较高位置开始向下搜索
        BlockPos.Mutable mutable = pos.mutableCopy().set(pos.getX(), 320, pos.getZ());

        while (mutable.getY() > world.getBottomY()) {
            BlockState state = world.getBlockState(mutable);
            BlockState stateBelow = world.getBlockState(mutable.down());
            BlockState stateAbove = world.getBlockState(mutable.up());

            // 检查是否是安全的传送位置
            if (!state.isAir() && stateBelow.isSolid() && stateAbove.isAir()) {
                return mutable.up().toImmutable();
            }

            mutable.move(Direction.DOWN);
        }

        return null;
    }

    /**
     * 给予玩家超级悬浮效果
     */
    public static boolean giveLevitation(PlayerEntity player) {
        // 创建悬浮效果实例（持续30秒 = 600 ticks，等级255）
        StatusEffectInstance levitation = new StatusEffectInstance(
            StatusEffects.LEVITATION,
            600,           // 持续时间（ticks）
            10,          // 等级 (254 = 等级 255)
            false,        // 是否显示粒子
            true,         // 是否显示图标
            true          // 是否显示在物品栏
        );
        
        // 给予玩家效果
        player.addStatusEffect(levitation);
        
        // 播放音效
        player.getWorld().playSound(
            null,
            player.getBlockPos(),
            SoundEvents.ENTITY_SHULKER_SHOOT,
            SoundCategory.PLAYERS,
            1.0f,
            1.0f
        );
        
        // 生成粒子效果
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                ParticleTypes.CLOUD,
                player.getX(),
                player.getY(),
                player.getZ(),
                50,  // 粒子数量
                0.5, // X扩散范围
                0.5, // Y扩散范围
                0.5, // Z扩散范围
                0.1  // 速度
            );
        }
        
        // 发送消息
        player.sendMessage(Text.literal("神明赐予你悬浮之力！之后的结果就看你自己了！").formatted(Formatting.AQUA, Formatting.BOLD), false);
        
        return true;
    }

    /**
     * 生成床和村民，脱掉玩家盔甲
     */
    public static boolean spawnBedAndVillager(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        // 获取玩家面前4格的位置
        BlockPos bedPos = player.getBlockPos().offset(
            player.getHorizontalFacing(),  // 使用玩家朝向
            2                              // 距离
        );
        
        // 确保位置是空的
        serverWorld.setBlockState(bedPos, Blocks.AIR.getDefaultState());
        serverWorld.setBlockState(bedPos.up(), Blocks.AIR.getDefaultState());

        // 放置床（根据玩家朝向）
        Direction bedFacing = player.getHorizontalFacing();
        
        // 放置床尾
        BlockState footState = Blocks.RED_BED.getDefaultState()
            .with(BedBlock.FACING, bedFacing)
            .with(BedBlock.PART, BedPart.FOOT);
        serverWorld.setBlockState(bedPos, footState);
        
        // 放置床头（在床尾的前方）
        BlockPos headPos = bedPos.offset(bedFacing);
        BlockState headState = Blocks.RED_BED.getDefaultState()
            .with(BedBlock.FACING, bedFacing)
            .with(BedBlock.PART, BedPart.HEAD);
        serverWorld.setBlockState(headPos, headState);
        
        // 生成村民
        VillagerEntity villager = EntityType.VILLAGER.create(serverWorld);
        if (villager != null) {
            // 设置村民位置（床的旁边）
            villager.refreshPositionAndAngles(
                bedPos.getX() + 0.5,
                bedPos.getY(),
                bedPos.getZ() + 1.5,
                0.0F,
                0.0F
            );

            // 设置村民名字
            villager.setCustomName(Text.literal("杰哥").formatted(Formatting.GOLD));
            // 让名字永远显示
            villager.setCustomNameVisible(true);
            // 随机职业和等级
            villager.setVillagerData(villager.getVillagerData()
                .withProfession(VillagerProfession.NITWIT)  // 设为傻子村民
                .withLevel(1));
            
            serverWorld.spawnEntity(villager);
        }
        
        // 脱掉玩家盔甲并掉落
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack armorItem = player.getInventory().armor.get(i);
            if (!armorItem.isEmpty()) {
                player.dropItem(armorItem, true);
                player.getInventory().armor.set(i, ItemStack.EMPTY);
            }
        }
        
        // 播放音效
        serverWorld.playSound(
            null,
            player.getBlockPos(),
            SoundEvents.ENTITY_VILLAGER_CELEBRATE,
            SoundCategory.NEUTRAL,
            1.0f,
            1.0f
        );
        
        // 生成爱心粒子
        serverWorld.spawnParticles(
            ParticleTypes.HEART,
            bedPos.getX() + 0.5,
            bedPos.getY() + 1,
            bedPos.getZ() + 0.5,
            10,  // 粒子数量
            0.5, // X扩散范围
            0.5, // Y扩散范围
            0.5, // Z扩散范围
            0.1  // 速度
        );
        
        // 发送消息
        player.sendMessage(Text.literal("赌神把你许配给了杰哥！").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC), false);
        
        return true;
    }

    /**
     * 向多个方向发射末影珍珠
     */
    public static boolean throwEnderPearls(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        // 发射10颗末影珍珠
        int pearlCount = 10;
        float baseSpeed = 1.5f; // 基础速度
        
        for (int i = 0; i < pearlCount; i++) {
            // 计算发射角度（均匀分布在360度内）
            double angle = (2 * Math.PI * i) / pearlCount;
            
            // 创建末影珍珠实体
            EnderPearlEntity pearl = new EnderPearlEntity(serverWorld, player);
            
            // 设置珍珠位置（从玩家眼睛位置发射）
            pearl.setPosition(
                player.getEyePos().x,
                player.getEyePos().y,
                player.getEyePos().z
            );
            
            // 计算发射方向
            double vx = Math.cos(angle) * baseSpeed;
            double vz = Math.sin(angle) * baseSpeed;
            pearl.setVelocity(vx, 0.2, vz); // 添加一点向上的速度
            
            // 生成实体
            serverWorld.spawnEntity(pearl);
            
            // 生成粒子效果
            serverWorld.spawnParticles(
                ParticleTypes.PORTAL,
                player.getX(),
                player.getY() + 1,
                player.getZ(),
                10,  // 每个方向的粒子数量
                0.2, // X扩散范围
                0.2, // Y扩散范围
                0.2, // Z扩散范围
                0.1  // 速度
            );
        }
        
        // 播放音效
        serverWorld.playSound(
            null,
            player.getBlockPos(),
            SoundEvents.ENTITY_ENDER_PEARL_THROW,
            SoundCategory.PLAYERS,
            1.0f,
            1.0f
        );
        
        // 发送消息
        player.sendMessage(Text.literal("表演个飞雷神！").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), false);
        
        return true;
    }

    /**
     * 将玩家传送到地狱
     */
    public static boolean teleportToNether(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        
        // 获取地狱维度
        ServerWorld netherWorld = serverWorld.getServer().getWorld(World.NETHER);
        if (netherWorld == null) return false;
        
        // 在 ±2000 范围内随机一个位置
        Random random = new Random();
        double randomX = random.nextDouble() * 4000 - 2000;  // -2000 到 2000
        double randomZ = random.nextDouble() * 4000 - 2000;  // -2000 到 2000
        
        // 从Y=100开始向下寻找安全的位置
        BlockPos safePos = null;
        for (int y = 100; y > 0; y--) {
            BlockPos testPos = new BlockPos((int)randomX, y, (int)randomZ);
            BlockState state = netherWorld.getBlockState(testPos);
            BlockState stateAbove = netherWorld.getBlockState(testPos.up());
            BlockState stateBelow = netherWorld.getBlockState(testPos.down());
            
            // 检查是否是安全的传送位置（当前位置和上方是空的，下方是实心方块）
            if (state.isAir() && stateAbove.isAir() && stateBelow.isSolid()) {
                safePos = testPos;
                break;
            }
        }
        
        // 如果找不到安全位置，就在Y=70创建一个平台
        if (safePos == null) {
            safePos = new BlockPos((int)randomX, 70, (int)randomZ);
            // 创建一个3x3的黑曜石平台
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    netherWorld.setBlockState(safePos.add(x, -1, z), Blocks.OBSIDIAN.getDefaultState());
                }
            }
        }
        
        // 传送前生成粒子效果
        serverWorld.spawnParticles(
            ParticleTypes.FLAME,
            player.getX(),
            player.getY(),
            player.getZ(),
            50,  // 粒子数量
            0.5, // X扩散范围
            1.0, // Y扩散范围
            0.5, // Z扩散范围
            0.1  // 速度
        );
        
        // 播放传送音效
        serverWorld.playSound(
            null,
            player.getBlockPos(),
            SoundEvents.BLOCK_PORTAL_TRAVEL,
            SoundCategory.PLAYERS,
            1.0f,
            1.0f
        );
        
        // 传送玩家
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
        serverPlayer.teleport(
            netherWorld,
            safePos.getX() + 0.5,
            safePos.getY() + 0.1,
            safePos.getZ() + 0.5,
            player.getYaw(),
            player.getPitch()
        );
        
        // 传送后生成粒子效果
        netherWorld.spawnParticles(
            ParticleTypes.FLAME,
            safePos.getX() + 0.5,
            safePos.getY() + 0.5,
            safePos.getZ() + 0.5,
            50,  // 粒子数量
            0.5, // X扩散范围
            1.0, // Y扩散范围
            0.5, // Z扩散范围
            0.1  // 速度
        );
            
        // 发送消息
        player.sendMessage(Text.literal("不是！哥们！这哪啊！！").formatted(Formatting.RED, Formatting.BOLD), false);

        return true;
    }
} 