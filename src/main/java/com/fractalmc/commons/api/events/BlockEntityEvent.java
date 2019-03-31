package com.fractalmc.commons.api.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.util.Collection;

public class BlockEntityEvent extends WorldEvent
{
    public BlockEntityEvent(World worldIn)
    {
        super(worldIn);
    }

    @Cancelable
    public static class Pickup extends BlockEntityEvent
    {
        private final EntityLivingBase entity;
        private final Collection<BlockPos> positions;

        public Pickup(World worldIn, EntityLivingBase entity, Collection<BlockPos> positions)
        {
            super(worldIn);

            this.entity = entity;
            this.positions = positions;
        }

        public EntityLivingBase getEntity()
        {
            return entity;
        }

        public Collection<BlockPos> getPositions()
        {
            return positions;
        }
    }

    @Cancelable
    public static class Place extends BlockEntityEvent
    {
        private final Entity blockEntity;
        private final EntityLivingBase entity;
        private final IBlockState blockState;
        private final BlockPos position;

        public Place(World worldIn, Entity blockEntity, EntityLivingBase entity, IBlockState blockState, BlockPos position)
        {
            super(worldIn);

            this.blockEntity = blockEntity;
            this.entity = entity;
            this.blockState = blockState;
            this.position = position;
        }

        public Entity getBlockEntity()
        {
            return blockEntity;
        }

        public EntityLivingBase getEntity()
        {
            return entity;
        }

        public IBlockState getBlockState()
        {
            return blockState;
        }

        public BlockPos getPos()
        {
            return position;
        }
    }
}
