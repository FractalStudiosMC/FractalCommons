package com.fractalmc.commons.common.packet;

import com.fractalmc.commons.FractalCommons;
import com.fractalmc.commons.common.core.network.AbstractPacket;
import com.fractalmc.commons.common.entity.EntityBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class BlockEntityDataPackets
{
    public static class PacketBlockEntityData extends AbstractPacket
    {
        public int id;
        public NBTTagCompound tag;

        public PacketBlockEntityData() {}

        public PacketBlockEntityData(int id, NBTTagCompound tag)
        {
            this.id = id;
            this.tag = tag;
        }

        @Override
        public void writeTo(ByteBuf buffer)
        {
            buffer.writeInt(id);
            ByteBufUtils.writeTag(buffer, tag);
        }

        @Override
        public void readFrom(ByteBuf buffer)
        {
            id = buffer.readInt();
            tag = ByteBufUtils.readTag(buffer);
        }

        @Override
        public void execute(Side side, EntityPlayer player)
        {
            Entity ent = player.getEntityWorld().getEntityByID(id);

            if (ent instanceof EntityBlock)
            {
                EntityBlock block = (EntityBlock)ent;

                block.readFromNBT(tag);
                block.setup = true;
            }
        }

        @Override
        public Side receivingSide()
        {
            return Side.CLIENT;
        }
    }

    public static class PacketRequestBlockEntityData extends AbstractPacket
    {
        public int id;

        public PacketRequestBlockEntityData() {}

        public PacketRequestBlockEntityData(EntityBlock block)
        {
            id = block.getEntityId();
        }

        @Override
        public void writeTo(ByteBuf buffer)
        {
            buffer.writeInt(id);
        }

        @Override
        public void readFrom(ByteBuf buffer)
        {
            id = buffer.readInt();
        }

        @Override
        public void execute(Side side, EntityPlayer player)
        {
            Entity ent = player.getEntityWorld().getEntityByID(id);

            if(ent instanceof EntityBlock)
            {
                NBTTagCompound tag = new NBTTagCompound();

                ent.writeToNBT(tag);

                FractalCommons.channel.sendTo(new PacketBlockEntityData(id, tag), player);
            }
        }

        @Override
        public Side receivingSide()
        {
            return Side.SERVER;
        }
    }
}
