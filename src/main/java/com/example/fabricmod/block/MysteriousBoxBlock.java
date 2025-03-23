package com.example.fabricmod.block;

import com.example.fabricmod.block.entity.MysteriousBoxBlockEntity;
import com.example.fabricmod.registry.ModBlockEntities;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class MysteriousBoxBlock extends BlockWithEntity {
    // 创建自定义的碰撞箱形状
    private static final VoxelShape SHAPE = VoxelShapes.union(
        // 底座
        Block.createCuboidShape(1, 0, 1, 15, 1, 15),
        
        // 主体四周墙壁 (从模型文件可以看出墙壁是从2开始的)
        Block.createCuboidShape(2, 1, 1, 14, 8, 2),    // 前墙
        Block.createCuboidShape(2, 1, 14, 14, 8, 15),  // 后墙
        Block.createCuboidShape(1, 1, 2, 2, 8, 14),    // 左墙
        Block.createCuboidShape(14, 1, 2, 15, 8, 14),  // 右墙
        
        // 中间层
        Block.createCuboidShape(2, 8, 2, 14, 9, 14),   // 中间的平台
        
        // 顶部装饰 (从模型可以看出是从10层开始的)
        Block.createCuboidShape(0, 10, 2, 1, 12, 14),   // 左边装饰
        Block.createCuboidShape(15, 10, 2, 16, 12, 14), // 右边装饰
        Block.createCuboidShape(2, 10, 0, 14, 12, 1),   // 前边装饰
        Block.createCuboidShape(2, 10, 15, 14, 12, 16)  // 后边装饰
    );

    public MysteriousBoxBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MysteriousBoxBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntities.MYSTERIOUS_BOX_ENTITY, MysteriousBoxBlockEntity::tick);
    }
} 