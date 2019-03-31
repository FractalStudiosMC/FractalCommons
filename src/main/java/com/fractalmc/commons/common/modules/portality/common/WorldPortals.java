package com.fractalmc.commons.common.modules.portality.common;

import com.fractalmc.commons.FractalCommons;
import com.fractalmc.commons.api.portality.PortalityApi;
import com.fractalmc.commons.client.render.RendererHelper;
import com.fractalmc.commons.common.core.network.PacketChannel;
import com.fractalmc.commons.common.modules.portality.client.core.EventHandlerWorldPortalClient;
import com.fractalmc.commons.common.modules.portality.common.core.ApiImpl;
import com.fractalmc.commons.common.modules.portality.common.core.EventHandlerWorldPortal;
import com.fractalmc.commons.common.modules.portality.common.packet.PacketEntityLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldPortals
{
    private static boolean init = false;

    public static EventHandlerWorldPortalClient eventHandlerClient;
    public static EventHandlerWorldPortal eventHandler;
    public static PacketChannel channel;

    public static void init()
    {
        if(init)
        {
            return;
        }
        init = true;

        FractalCommons.config.reveal("maxRecursion", "stencilValue", "renderDistanceChunks", "maxRendersPerTick");

        eventHandler = new EventHandlerWorldPortal();
        MinecraftForge.EVENT_BUS.register(eventHandler);

        PortalityApi.setApi(new ApiImpl());

        channel = new PacketChannel("Dewy_Portality", PacketEntityLocation.class);

        if(FMLCommonHandler.instance().getSide().isClient())
        {
            initClient();
        }
    }

    @SideOnly(Side.CLIENT)
    private static void initClient()
    {
        eventHandlerClient = new EventHandlerWorldPortalClient();
        MinecraftForge.EVENT_BUS.register(eventHandlerClient);

        if(!RendererHelper.canUseStencils())
        {
            FractalCommons.internalLogger.logFatalError("[WorldPortals] Stencils aren't enabled. We won't be able to render a world portal!");
        }
    }

    public static void onServerStopping()
    {
        if(init)
        {
            eventHandler.monitoredEntities.get(Side.SERVER).clear();
        }
    }
}
