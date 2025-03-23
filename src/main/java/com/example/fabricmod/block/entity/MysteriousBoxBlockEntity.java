package com.example.fabricmod.block.entity;

import com.example.fabricmod.networking.ModPackets;
import com.example.fabricmod.registry.ModBlockEntities;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;

public class MysteriousBoxBlockEntity extends BlockEntity {
    private int tickCounter = 0;
    private int ejectInterval = 600; // 默认30秒
    private boolean canBeDestroyed = true; // 默认可被破坏
    private static final Random RANDOM = Random.create();
    private static final int MIN_ITEMS = 10; // 最少喷射物品数量
    private static final int MAX_ITEMS = 30; // 最多喷射物品数量
    
    public MysteriousBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MYSTERIOUS_BOX_ENTITY, pos, state);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("ejectInterval", ejectInterval);
        nbt.putBoolean("canBeDestroyed", canBeDestroyed);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        ejectInterval = nbt.getInt("ejectInterval");
        canBeDestroyed = nbt.getBoolean("canBeDestroyed");
    }

    public static void tick(World world, BlockPos pos, BlockState state, MysteriousBoxBlockEntity blockEntity) {
        if (world.isClient) return;
        
        blockEntity.tickCounter++;
        if (blockEntity.tickCounter >= blockEntity.ejectInterval) {
            blockEntity.tickCounter = 0;
            blockEntity.ejectItems();
        }
    }

    private void ejectItems() {
        if (world == null || world.isClient) return;

        // 从注册表中获取随机物品
        List<Item> allItems = new ArrayList<>();
        Registries.ITEM.forEach(item -> {
            if (item.getDefaultStack().isStackable()) {
                allItems.add(item);
            }
        });

        // 随机决定这次要喷射多少个物品
        int itemCount = RANDOM.nextBetween(MIN_ITEMS, MAX_ITEMS);
        
        // 发送网络包到客户端（只发送一次，客户端会生成对应数量的粒子效果）
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeInt(itemCount); // 告诉客户端要生成多少组粒子效果
        PlayerLookup.tracking((ServerWorld)world, pos).forEach(player -> 
            ServerPlayNetworking.send(player, ModPackets.MYSTERIOUS_BOX_EJECT, buf)
        );

        // 生成多个物品
        for (int i = 0; i < itemCount; i++) {
            // 随机选择一个物品
            Item randomItem = allItems.get(RANDOM.nextInt(allItems.size()));
            ItemStack itemStack = new ItemStack(randomItem);

            // 创建物品实体，位置略微随机偏移
            double offsetX = RANDOM.nextDouble() * 0.4 - 0.2; // -0.2 到 0.2 的随机偏移
            double offsetZ = RANDOM.nextDouble() * 0.4 - 0.2;
            
            double x = pos.getX() + 0.5 + offsetX;
            double y = pos.getY() + 1.2;
            double z = pos.getZ() + 0.5 + offsetZ;

            ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack);
            
            // 设置随机速度
            double velocityX = RANDOM.nextDouble() * 0.2 - 0.1;
            double velocityY = 0.2 + RANDOM.nextDouble() * 0.2;
            double velocityZ = RANDOM.nextDouble() * 0.2 - 0.1;
            
            itemEntity.setVelocity(velocityX, velocityY, velocityZ);
            
            // 生成物品实体
            world.spawnEntity(itemEntity);
        }
    }

    // Getter和Setter方法
    public int getEjectInterval() {
        return ejectInterval;
    }

    public void setEjectInterval(int interval) {
        this.ejectInterval = interval;
        markDirty();
    }

    public boolean canBeDestroyed() {
        return canBeDestroyed;
    }

    public void setCanBeDestroyed(boolean canBeDestroyed) {
        this.canBeDestroyed = canBeDestroyed;
        markDirty();
    }
} 