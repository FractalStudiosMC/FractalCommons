package com.fractalmc.commons.common.packet.mod;

import com.fractalmc.commons.common.core.network.AbstractPacket;
import com.fractalmc.commons.common.grab.GrabHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public class PacketNewGrabbedEntityId extends AbstractPacket
{
    public boolean grabbed;
    public int oldId;
    public int newId;

    public PacketNewGrabbedEntityId() {}

    public PacketNewGrabbedEntityId(boolean grabbedd, int oldd, int neww)
    {
        grabbed = grabbedd;
        oldId = oldd;
        newId = neww;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        buffer.writeBoolean(grabbed);
        buffer.writeInt(oldId);
        buffer.writeInt(newId);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        grabbed = buffer.readBoolean();
        oldId = buffer.readInt();
        newId = buffer.readInt();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        for(GrabHandler handler : GrabHandler.grabbedEntities.get(Side.CLIENT))
        {
            if(grabbed)
            {
                if(handler.grabbedId == oldId)
                {
                    handler.grabbedId = newId;
                }
            }
            else
            {
                if(handler.grabberId == oldId)
                {
                    handler.grabberId = newId;
                }
            }
        }
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }
}
