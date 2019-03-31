package com.fractalmc.commons.common.core.event;

import com.fractalmc.commons.FractalCommons;
import com.fractalmc.commons.common.core.config.ConfigBase;
import com.fractalmc.commons.common.core.config.ConfigHandler;
import com.fractalmc.commons.common.core.tracker.EntityTrackerRegistry;
import com.fractalmc.commons.common.core.utils.EventCalendar;
import com.fractalmc.commons.common.grab.GrabHandler;
import com.fractalmc.commons.common.packet.mod.PacketNewGrabbedEntityId;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;

public class EventHandlerServer
{
    public int ticks;

    public EntityTrackerRegistry entityTrackerRegistry = new EntityTrackerRegistry();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            entityTrackerRegistry.tick();

            GrabHandler.tick(Side.SERVER);

            ticks++;
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.side.isServer() && event.phase == TickEvent.Phase.END)
        {
            if(EventCalendar.isAFDay())
            {
                if(event.player.isPlayerSleeping() && event.player.getRNG().nextFloat() < 0.025F || event.player.getRNG().nextFloat() < 0.005F)
                {
                    event.player.getEntityWorld().playSound(null, event.player.posX, event.player.posY + event.player.getEyeHeight(), event.player.posZ, SoundEvents.ENTITY_PIG_AMBIENT, SoundCategory.PLAYERS, event.player.isPlayerSleeping() ? 0.2F : 1.0F, (event.player.getRNG().nextFloat() - event.player.getRNG().nextFloat()) * 0.2F + 1.0F);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        for(ConfigBase conf : ConfigHandler.configs)
        {
            if(!conf.sessionProp.isEmpty())
            {
                conf.sendPlayerSession(event.player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        ArrayList<GrabHandler> handlers = GrabHandler.getHandlers(event.player, Side.SERVER);
        for(int i = handlers.size() - 1; i >= 0; i--)
        {
            GrabHandler handler = handlers.get(i);
            if(handler.canSendAcrossDimensions())
            {
                GrabHandler.dimensionalEntities.add(handler.grabbed.getEntityId());
                handler.grabbed.getEntityData().setInteger("Grabbed-ID", handler.grabbed.getEntityId());
                handler.grabbed.changeDimension(event.player.dimension);
                handler.update();
            }
            else
            {
                handler.terminate();
                handlers.remove(i);
            }
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event)
    {
        if(!event.getEntity().world.isRemote && event.getEntity().getEntityData().hasKey("Grabbed-ID"))
        {
            Integer x = event.getEntity().getEntityData().getInteger("Grabbed-ID");
            if(event.getEntity().getEntityId() != x)
            {
                for(int i = GrabHandler.dimensionalEntities.size() - 1; i >= 0; i--)
                {
                    if(GrabHandler.dimensionalEntities.get(i).equals(x))
                    {
                        GrabHandler.dimensionalEntities.remove(i);
                        for(GrabHandler handler : GrabHandler.grabbedEntities.get(Side.SERVER))
                        {
                            if(handler.grabbed.getEntityId() == x)
                            {
                                handler.grabbed = event.getEntity();
                                FractalCommons.channel.sendToAll(new PacketNewGrabbedEntityId(true, x, event.getEntity().getEntityId()));
                            }
                        }
                    }
                }
            }
        }
    }

    public void shuttingDownServer()
    {
        entityTrackerRegistry.trackerEntries.clear();

        GrabHandler.grabbedEntities.get(Side.SERVER).clear();
        GrabHandler.dimensionalEntities.clear();
    }
}
