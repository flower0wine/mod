package com.example.fabricmod.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.ChestType;
import net.minecraft.state.property.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import com.example.fabricmod.access.ThiefAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerData;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.entity.passive.BatEntity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import com.example.fabricmod.ExampleMod;

@Mixin(VillagerEntity.class)
public abstract class VillagerBehaviorMixin extends MobEntity implements ThiefAccess {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleMod.MOD_ID);
    
    // 村民是否为小偷的状态标记，需要在客户端同步显示
    @Unique
    private static final TrackedData<Boolean> IS_THIEF = 
        DataTracker.registerData(VillagerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    // 持久化数据组 - 这些数据需要保存到 NBT 中
    // 村民是否有负罪感（被发现偷东西）
    @Unique
    private boolean hasGuilt = false;
    // 负罪感持续时间计时器
    @Unique
    private int guiltTimer = 0;
    // 偷窃行为的冷却时间
    @Unique
    private int theftCooldown = 0;
    // 当前偷到的物品
    @Unique
    private List<ItemStack> stolenItems = new ArrayList<>();
    // 村民的偷窃等级
    @Unique
    private int theftLevel = 0;

    // 临时状态组 - 这些数据不需要持久化
    // 是否正在被玩家注视
    @Unique
    private boolean isBeingWatched = false;
    // 搜索箱子的冷却时间
    @Unique
    private int searchCooldown = 0;
    // 当前目标箱子的位置
    @Unique
    private BlockPos targetChestPos = null;
    // 打开箱子的持续时间计时器
    @Unique
    private int chestOpenTime = 0;
    // 玩家在附近停留的时间计时器
    @Unique
    private int playerNearbyTime = 0;
    // 丢弃物品后的冷却时间
    @Unique
    private int dropItemCooldown = 0;
    // 恐慌声音的播放冷却时间
    @Unique
    private int panicSoundTimer = 0;

    // 常量配置组 - 用于调整村民行为的各项参数
    // 搜索箱子的范围（方块）
    @Unique
    private static final int SEARCH_RADIUS = 10;
    // 玩家能看到村民的最大距离
    @Unique
    private static final double MAX_VIEW_DISTANCE = 16.0;
    // 打开箱子的动画持续时间（刻）
    @Unique
    private static final int CHEST_OPEN_DURATION = 20;
    // 能够偷窃箱子的最大距离
    @Unique
    private static final double STEAL_DISTANCE = 2.0;
    // 两次偷窃之间的冷却时间（刻）
    @Unique
    private static final int THEFT_COOLDOWN = 2400;
    // 丢弃物品后的冷却时间（刻）
    @Unique
    private static final int DROP_ITEM_COOLDOWN = 100;
    // 玩家在附近停留多久会触发丢弃物品（刻）
    @Unique
    private static final int PLAYER_NEARBY_THRESHOLD = 200;
    // 检测玩家的范围
    @Unique
    private static final double PLAYER_DETECTION_RANGE = 3.0;
    // 逃跑时的移动速度倍率
    @Unique
    private static final double ESCAPE_SPEED = 1.2;
    // 恐慌声音的播放间隔（刻）
    @Unique
    private static final int PANIC_SOUND_COOLDOWN = 60;
    // 负罪感的持续时间（刻）
    @Unique
    private static final int GUILT_DURATION = 1800;
    // 最大偷窃等级
    @Unique
    private static final int MAX_THEFT_LEVEL = 10;
    // 最大偷窃物品数量
    @Unique
    private static final int MAX_THEFT_AMOUNT = 64;
    // 基础偷窃数量（1级时）
    @Unique
    private static final int BASE_THEFT_AMOUNT = 1;

    // 共享状态
    @Unique
    private static final Map<BlockPos, Integer> OPENED_CHESTS = new HashMap<>();
    @Unique
    private static final String MOD_DATA_KEY = "flowerwine_villager_data";
    @Unique
    private static final String THIEF_KEY = "isThief";
    @Unique
    private static final String HAS_GUILT_KEY = "hasGuilt";
    @Unique
    private static final String GUILT_TIMER_KEY = "guiltTimer";
    @Unique
    private static final String THEFT_COOLDOWN_KEY = "theftCooldown";
    @Unique
    private static final String STOLEN_ITEMS_KEY = "stolenItems";
    @Unique
    private static final String THEFT_LEVEL_KEY = "theftLevel";

    private VillagerBehaviorMixin() {
        super(null, null);
        throw new UnsupportedOperationException();
    }

    // 数据追踪初始化
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTracker(CallbackInfo ci) {
        this.getDataTracker().startTracking(IS_THIEF, false);
    }

    // NBT 数据持久化
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound modData = new NbtCompound();
        modData.putBoolean(THIEF_KEY, isThief());
        modData.putBoolean(HAS_GUILT_KEY, hasGuilt);
        modData.putInt(GUILT_TIMER_KEY, guiltTimer);
        modData.putInt(THEFT_COOLDOWN_KEY, theftCooldown);
        modData.putInt(THEFT_LEVEL_KEY, theftLevel);

        if (stolenItems != null) {
            // 保存偷取的物品列表
            NbtList itemsList = new NbtList();
            for (ItemStack item : stolenItems) {
                NbtCompound itemData = new NbtCompound();
                item.writeNbt(itemData);
                itemsList.add(itemData);
            }
            modData.put(STOLEN_ITEMS_KEY, itemsList);
        }

        nbt.put(MOD_DATA_KEY, modData);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(MOD_DATA_KEY)) {
            NbtCompound modData = nbt.getCompound(MOD_DATA_KEY);
            
            if (modData.contains(THIEF_KEY)) {
                setThief(modData.getBoolean(THIEF_KEY));
            }
            if (modData.contains(HAS_GUILT_KEY)) {
                hasGuilt = modData.getBoolean(HAS_GUILT_KEY);
            }
            if (modData.contains(GUILT_TIMER_KEY)) {
                guiltTimer = modData.getInt(GUILT_TIMER_KEY);
            }
            if (modData.contains(THEFT_COOLDOWN_KEY)) {
                theftCooldown = modData.getInt(THEFT_COOLDOWN_KEY);
            }
            if (modData.contains(THEFT_LEVEL_KEY)) {
                theftLevel = modData.getInt(THEFT_LEVEL_KEY);
            }
            
            // 读取偷取的物品列表
            stolenItems.clear();
            if (modData.contains(STOLEN_ITEMS_KEY)) {
                NbtList itemsList = modData.getList(STOLEN_ITEMS_KEY, NbtElement.COMPOUND_TYPE);
                for (int i = 0; i < itemsList.size(); i++) {
                    NbtCompound itemData = itemsList.getCompound(i);
                    stolenItems.add(ItemStack.fromNbt(itemData));
                }
            }
        }
    }

    // 接口实现
    @Override
    public boolean isThief() {
        return this.getDataTracker().get(IS_THIEF);
    }

    @Override
    public void setThief(boolean value) {
        this.getDataTracker().set(IS_THIEF, value);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        
        if (villager.getWorld().isClient) return;
        
        if (!canBecomeThief(villager)) {
            return;
        }

        // 更新冷却时间
        if (theftCooldown > 0) {
            theftCooldown--;
            // 在冷却时间剩余20秒时播放提示音
            if (theftCooldown == 20) {
                villager.getWorld().playSound(
                    null,
                    villager.getBlockPos(),
                    SoundEvents.ENTITY_VILLAGER_NO,
                    SoundCategory.NEUTRAL,
                    0.8F,
                    1.2F
                );
            }
        }
        if (dropItemCooldown > 0) {
            dropItemCooldown--;
            if (dropItemCooldown == 0) {
                resetThiefStatus();
            }
        }

        // 如果是小偷状态，检查附近的玩家
        if (this.isThief()) {
            handleThiefBehavior();
        }

        // 处理箱子开关状态
        if (targetChestPos != null) {
            // 检查是否足够近可以偷东西
            if (isCloseEnoughToChest(villager, targetChestPos)) {
                if (chestOpenTime == 0) {
                    // 增加箱子的打开计数
                    OPENED_CHESTS.merge(targetChestPos, 1, Integer::sum);
                }

                if (chestOpenTime == CHEST_OPEN_DURATION / 3) {
                    // 打开箱子
                    openChest(villager, targetChestPos, true);
                }

                if (chestOpenTime == CHEST_OPEN_DURATION / 3 * 2) {
                    stealFromChest(villager, targetChestPos);
                }
                
                chestOpenTime++;

                if (chestOpenTime >= CHEST_OPEN_DURATION) {
                    closeChest();

                    targetChestPos = null;
                    chestOpenTime = 0;

                    // 关闭箱子时才认为是小偷
                    setThief(true);
                    hasGuilt = true;
                    guiltTimer = 0;
                }
            } else {
                // 如果此时还在打开箱子
                if (chestOpenTime > 0) {
                    closeChest();
                    chestOpenTime = 0;
                }
                // 继续移动到箱子
                moveToChest(villager, targetChestPos);
            }
        }

        if (searchCooldown > 0) {
            searchCooldown--;
            return;
        }
        searchCooldown = 20;

        List<PlayerEntity> nearbyPlayers = villager.getWorld().getEntitiesByClass(
            PlayerEntity.class,
            new Box(villager.getBlockPos()).expand(SEARCH_RADIUS),
            player -> true
        );

        boolean currentlyBeingWatched = false;
        for (PlayerEntity player : nearbyPlayers) {
            if (canPlayerSeeVillager(player, villager)) {
                currentlyBeingWatched = true;
                break;
            }
        }

        if (!currentlyBeingWatched && isBeingWatched) {
            searchForChests(villager);
        }
        
        isBeingWatched = currentlyBeingWatched;
    }

    @Unique
    public boolean isCloseEnoughToChest(VillagerEntity villager, BlockPos chestPos) {
        return villager.getBlockPos().isWithinDistance(chestPos, STEAL_DISTANCE);
    }

    @Unique
    public void moveToChest(VillagerEntity villager, BlockPos chestPos) {
        // 如果还没有路径或者需要重新计算路径
        if (villager.getNavigation().isIdle()) {
            Path path = villager.getNavigation().findPathTo(chestPos, 0);
            if (path != null) {
                villager.getNavigation().startMovingAlong(path, 0.5);
            } else {
                targetChestPos = null;
            }
        }
    }

    @Unique
    public boolean canBecomeThief(VillagerEntity villager) {
        return villager.getVillagerData().getProfession() == VillagerProfession.NONE;
    }

    @Unique
    public void searchForChests(VillagerEntity villager) {
        BlockPos villagerPos = villager.getBlockPos();
        
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = villagerPos.add(x, y, z);
                    
                    if (villager.getWorld().getBlockEntity(pos) instanceof ChestBlockEntity) {
                        targetChestPos = pos;
                        moveToChest(villager, pos);
                        return;
                    }
                }
            }
        }
    }

    @Unique
    public void stealFromChest(VillagerEntity villager, BlockPos pos) {
        if (theftCooldown > 0) {
            return;
        }

        if (villager.getWorld().getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            // 获取当前等级总共能偷多少标准物品（相对于64堆叠的物品）
            int remainingStealQuota = getTheftAmountForCurrentLevel();
            
            for (int slot = 0; slot < chest.size(); slot++) {  
                // 如果配额用完了就退出
                if (remainingStealQuota <= 0) {
                    break;
                }

                ItemStack stack = chest.getStack(slot);
                if (!stack.isEmpty()) {
                    int maxStackSize = stack.getMaxCount();
                    int currentStackSize = stack.getCount();
                    
                    // 计算这种物品的价值比例（相对于64堆叠的物品）
                    // 例如：最大堆叠为1的物品，价值比例为64
                    //      最大堆叠为16的物品，价值比例为4
                    //      最大堆叠为64的物品，价值比例为1
                    int valueRatio = 64 / maxStackSize;
                    
                    // 计算要偷取的数量
                    ItemStack stolenStack;
                    if (remainingStealQuota <= valueRatio) {
                        // 如果剩余配额小于等于价值比例，只偷1个
                        // 例如：配额32，遇到最大堆叠1的物品（比例64），只偷1个
                        stolenStack = stack.split(1);
                        remainingStealQuota = 0; // 偷完就用完配额
                    } else {
                        // 计算这种物品最多可以偷几个
                        int maxCanSteal = remainingStealQuota / valueRatio;
                        // 实际偷取数量不能超过箱子中的数量
                        int actualSteal = Math.min(maxCanSteal, currentStackSize);
                        stolenStack = stack.split(actualSteal);
                        // 减去已使用的配额
                        remainingStealQuota -= (actualSteal * valueRatio);
                    }
                    
                    // 将偷到的物品添加到列表中
                    stolenItems.add(stolenStack.copy());
                    chest.markDirty();
                    
                    // 每次成功偷窃后增加经验
                    if (theftLevel < MAX_THEFT_LEVEL) {
                        if (villager.getWorld().random.nextFloat() < (1.0f - theftLevel / (float)MAX_THEFT_LEVEL)) {
                            theftLevel++;
                            villager.getWorld().playSound(
                                null,
                                villager.getBlockPos(),
                                SoundEvents.ENTITY_PLAYER_LEVELUP,
                                SoundCategory.NEUTRAL,
                                0.5F,
                                1.0F
                            );
                        }
                    }
                    
                    villager.getWorld().addParticle(
                        ParticleTypes.SMOKE,
                        villager.getX(),
                        villager.getY() + 1,
                        villager.getZ(),
                        0.0, 0.1, 0.0
                    );
                }
            }
        }
    }

    @Unique
    private int getTheftAmountForCurrentLevel() {
        if (theftLevel <= 0) return BASE_THEFT_AMOUNT;
        if (theftLevel >= MAX_THEFT_LEVEL) return MAX_THEFT_AMOUNT;
        
        // 使用指数函数计算基础偷窃比例
        int amount = (int)Math.pow(1.5, theftLevel);
        
        return Math.min(amount, MAX_THEFT_AMOUNT);
    }

    @Unique
    public void openChest(VillagerEntity villager, BlockPos pos, boolean open) {
        World world = villager.getWorld();
        if (!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
            return;
        }

        // 播放箱子开关音效
        world.playSound(
            null,
            pos,
            open ? SoundEvents.BLOCK_CHEST_OPEN : SoundEvents.BLOCK_CHEST_CLOSE,
            SoundCategory.BLOCKS,
            0.5F,
            world.random.nextFloat() * 0.1F + 0.9F
        );

        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof ChestBlock)) {
            return;
        }

        ChestBlockEntity chest = world.getBlockEntity(pos, BlockEntityType.CHEST).orElse(null);
        if (chest == null) {
            return;
        }

        try {
            // 更新箱子的开关状态
            if (state.contains(Properties.CHEST_TYPE) && 
                state.get(Properties.CHEST_TYPE) != ChestType.SINGLE) {
                // 处理大箱子
                BlockPos otherPos = pos.offset(ChestBlock.getFacing(state));

                BlockState otherState = world.getBlockState(otherPos);
                ChestBlockEntity otherChest = world.getBlockEntity(otherPos, BlockEntityType.CHEST).orElse(null);
                
                if (otherState.isOf(state.getBlock()) && otherChest != null) {
                    // 更新两个箱子的状态
                    updateChestState(world, pos, chest, open);
                    updateChestState(world, otherPos, otherChest, open);
                }
            } else {
                // 处理单个箱子
                updateChestState(world, pos, chest, open);
            }
        } catch (Exception e) {
            LOGGER.error("更新箱子状态时发生错误: {}", e.getMessage());
        }
    }

    @Unique
    public void closeChest() {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        // 减少箱子的打开计数
        int count = OPENED_CHESTS.compute(targetChestPos, (pos, oldCount) ->
                oldCount != null ? oldCount - 1 : 0);

        // 只有当没有其他村民在使用这个箱子时才关闭它
        if (count <= 0) {
            openChest(villager, targetChestPos, false);
            OPENED_CHESTS.remove(targetChestPos);
        }
    }

    @Unique
    public void updateChestState(World world, BlockPos pos, ChestBlockEntity chest, boolean open) {

        // 使用方块实体的同步事件来控制箱子开关
        world.addSyncedBlockEvent(pos, chest.getCachedState().getBlock(), 1, open ? 1 : 0);
        
        // 确保客户端收到更新
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).getChunkManager().markForUpdate(pos);
        }
    }

    @Unique
    public boolean canPlayerSeeVillager(PlayerEntity player, VillagerEntity villager) {
        // 检查距离
        if (player.squaredDistanceTo(villager) > MAX_VIEW_DISTANCE * MAX_VIEW_DISTANCE) {
            return false;
        }

        // 获取玩家视线方向的向量
        Vec3d playerLook = player.getRotationVector();
        Vec3d toVillager = villager.getPos().subtract(player.getEyePos()).normalize();
        double dot = playerLook.dotProduct(toVillager);
        
        // 如果在视角范围内，检查是否有方块阻挡
        if (dot > 0.85) {
            Vec3d start = player.getEyePos();
            Vec3d end = villager.getPos().add(0, villager.getHeight() / 2, 0);
            
            RaycastContext context = new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
            );
            
            return player.getWorld().raycast(context).getType() == net.minecraft.util.hit.HitResult.Type.MISS;
        }
        
        return false;
    }

    @Unique
    public void handleThiefBehavior() {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        // 更新声音计时器
        if (panicSoundTimer > 0) {
            panicSoundTimer--;
        }
        
        // 检查附近的玩家
        List<PlayerEntity> nearbyPlayers = villager.getWorld().getEntitiesByClass(
            PlayerEntity.class,
            new Box(villager.getBlockPos()).expand(PLAYER_DETECTION_RANGE),
            player -> true
        );

        if (!nearbyPlayers.isEmpty()) {
            playerNearbyTime++;

            // 如果玩家持续在附近超过阈值时间
            if (playerNearbyTime >= PLAYER_NEARBY_THRESHOLD) {
                dropStolenItems();
                return;
            }

            // 只有当有负罪感时才会逃跑
            if (hasGuilt) {
                PlayerEntity nearestPlayer = nearbyPlayers.get(0);
                double distanceSquared = villager.squaredDistanceTo(nearestPlayer);
                if (distanceSquared < PLAYER_DETECTION_RANGE * PLAYER_DETECTION_RANGE) {
                    panicFromPlayer(nearestPlayer);
                    guiltTimer = 0; // 重置负罪感计时器

                    // 此时如果有要开启的箱子，则重置
                    targetChestPos = null;
                }
            }
        } else {
            playerNearbyTime = 0;
            // 当没有玩家在附近时，增加负罪感计时器
            if (hasGuilt && guiltTimer < GUILT_DURATION) {
                guiltTimer++;
                if (guiltTimer >= GUILT_DURATION) {
                    hasGuilt = false;
                    // 播放一个轻微的音效表示状态改变
                    villager.getWorld().playSound(
                        null,
                        villager.getBlockPos(),
                        SoundEvents.ENTITY_VILLAGER_AMBIENT,
                        SoundCategory.NEUTRAL,
                        0.5F,
                        1.2F
                    );
                }
            }
        }
    }

    @Unique
    public void panicFromPlayer(PlayerEntity player) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        // 计算逃跑方向（远离玩家的方向）
        Vec3d escapeDirection = villager.getPos().subtract(player.getPos()).normalize();
        double escapeX = villager.getX() + escapeDirection.x * 8.0;
        double escapeZ = villager.getZ() + escapeDirection.z * 8.0;
        
        // 寻找一个安全的逃跑位置
        BlockPos escapePos = new BlockPos((int)escapeX, (int)villager.getY(), (int)escapeZ);
        Path escapePath = villager.getNavigation().findPathTo(escapePos, 0);
        
        if (escapePath != null) {
            // 使用较快的速度逃跑
            villager.getNavigation().startMovingAlong(escapePath, ESCAPE_SPEED);
        }
        
        // 控制声音播放频率
        if (panicSoundTimer <= 0) {
            villager.getWorld().playSound(
                null,
                villager.getBlockPos(),
                SoundEvents.ENTITY_VILLAGER_NO,
                SoundCategory.NEUTRAL,
                1.0F,
                1.0F
            );
            panicSoundTimer = PANIC_SOUND_COOLDOWN;
        }
    }

    @Unique
    public void dropStolenItems() {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        
        // 丢弃所有偷到的物品
        for (ItemStack stack : stolenItems) {
            ItemEntity itemEntity = new ItemEntity(
                villager.getWorld(),
                villager.getX(),
                villager.getY(),
                villager.getZ(),
                stack.copy()
            );
            
            villager.getWorld().spawnEntity(itemEntity);
        }
        
        // 清空偷取的物品列表
        stolenItems.clear();
        
        // 设置丢弃物品后的冷却时间
        dropItemCooldown = DROP_ITEM_COOLDOWN;
        
        // 播放丢弃物品的音效
        villager.getWorld().playSound(
            null,
            villager.getBlockPos(),
            SoundEvents.ENTITY_VILLAGER_YES,
            SoundCategory.NEUTRAL,
            0.2F,
            0.8F
        );
    }

    @Unique
    public void resetThiefStatus() {
        setThief(false);
        hasGuilt = false;
        guiltTimer = 0;
        theftCooldown = THEFT_COOLDOWN;
        playerNearbyTime = 0;
        stolenItems.clear();
        theftLevel = 0;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        // 检查是否是玩家造成的伤害
        if (source.getAttacker() instanceof PlayerEntity && isThief()) {
            dropStolenItems();
            resetThiefStatus();

            // 播放额外的音效表示被发现
            villager.getWorld().playSound(
                    null,
                    villager.getBlockPos(),
                    SoundEvents.ENTITY_VILLAGER_NO,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1.0F
            );
        }

        return super.damage(source, amount);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(EntityType<? extends VillagerEntity> entityType, World world, CallbackInfo ci) {
        stolenItems = new ArrayList<>();
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        
        // 如果是小偷，先掉落物品，然后生成蝙蝠
        if (isThief()) {
            if (!stolenItems.isEmpty()) {
                dropStolenItems();
            }

            // 生成蝙蝠
            BatEntity bat = EntityType.BAT.create(villager.getWorld());
            if (bat != null) {
                // 设置蝙蝠的位置为村民死亡的位置
                bat.setPosition(villager.getX(), villager.getY() + 0.5, villager.getZ());
                
                // 给蝙蝠添加一些粒子效果
                villager.getWorld().addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    villager.getX(),
                    villager.getY() + 0.5,
                    villager.getZ(),
                    0.0, 0.1, 0.0
                );
                
                // 播放一个转化的音效
                villager.getWorld().playSound(
                    null,
                    villager.getBlockPos(),
                    SoundEvents.ENTITY_BAT_TAKEOFF,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1.0F
                );
                
                // 生成蝙蝠
                villager.getWorld().spawnEntity(bat);
            }
        }
    }

    @Inject(method = "setVillagerData", at = @At("HEAD"))
    private void onProfessionChange(VillagerData data, CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        boolean isThief = isThief();
        boolean hasProfessionNow = data.getProfession() != VillagerProfession.NONE;
        boolean hasProfessionBefore = villager.getVillagerData().getProfession() != VillagerProfession.NONE;
        
        // 如果从无职业变成有职业
        if (!hasProfessionBefore && hasProfessionNow && isThief) {
            // 丢掉偷的物品
            dropStolenItems();
            // 重置小偷状态
            resetThiefStatus();
            
            // 播放一个音效表示改过自新
            villager.getWorld().playSound(
                null,
                villager.getBlockPos(),
                SoundEvents.ENTITY_VILLAGER_YES,
                SoundCategory.NEUTRAL,
                1.0F,
                1.0F
            );
        } else if (!hasProfessionNow && hasProfessionBefore) {
            // 如果从有职业变成无职业

            // 一段时间内不会偷东西
            theftCooldown = THEFT_COOLDOWN / 2;
        }
    }
}