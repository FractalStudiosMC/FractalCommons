package com.fractalmc.commons.common.packet;

import com.fractalmc.commons.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public abstract class PacketDataFragment extends AbstractPacket
{
    public String fileName;
    public short packetTotal;
    public short packetNumber;
    public int fragmentSize;
    public byte[] data;

    public PacketDataFragment() {}

    public PacketDataFragment(String fileName, int packetTotal, int packetNumber, int fragmentSize, byte[] data)
    {
        this.fileName = fileName;
        this.packetTotal = (short)packetTotal;
        this.packetNumber = (short)packetNumber;
        this.fragmentSize = fragmentSize;
        this.data = data;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, fileName);

        buffer.writeShort(packetTotal);
        buffer.writeShort(packetNumber);
        buffer.writeInt(fragmentSize);
        buffer.writeBytes(data);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        fileName = ByteBufUtils.readUTF8String(buffer);
        packetTotal = buffer.readShort();
        packetNumber = buffer.readShort();
        fragmentSize = buffer.readInt();

        data = new byte[fragmentSize];

        buffer.readBytes(data);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        execution(side, player);
    }

    public abstract void execution(Side side, EntityPlayer player);
}
