package com.fractalmc.commons.common.modules.portality.client.core;

import com.fractalmc.commons.common.core.utils.EntityHelper;
import com.fractalmc.commons.common.modules.portality.client.render.WorldPortalRenderer;
import com.fractalmc.commons.common.modules.portality.client.render.world.RenderGlobalProxy;
import com.fractalmc.commons.common.modules.portality.common.WorldPortals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;

public class EventHandlerWorldPortalClient
{
    public float prevCameraRoll;
    public float cameraRoll;

    public WorldClient instance;
    public RenderGlobalProxy renderGlobalProxy;

    //temp
    public HashSet<AxisAlignedBB> aabbToRender = new HashSet<>();

    @SubscribeEvent
    public void onCameraSetupEvent(EntityViewRenderEvent.CameraSetup event)
    {
        if(cameraRoll != 0F && WorldPortalRenderer.renderLevel <= 0)
        {
            event.setRoll(EntityHelper.interpolateValues(prevCameraRoll, cameraRoll, (float)event.getRenderPartialTicks()));
        }
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event)
    {
        if(WorldPortals.eventHandler.isInPortal(event.getPlayer()))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPushPlayerSPOutOfBlock(PlayerSPPushOutOfBlocksEvent event)
    {
        if(WorldPortals.eventHandler.isInPortal(event.getEntityPlayer()))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.phase == TickEvent.Phase.START)
        {
            if(instance != mc.world)
            {
                instance = mc.world;

                if(renderGlobalProxy == null)
                {
                    renderGlobalProxy = new RenderGlobalProxy(mc);
                    renderGlobalProxy.updateDestroyBlockIcons();
                }
                renderGlobalProxy.setWorldAndLoadRenderers(instance);
            }
            WorldPortalRenderer.renderLevel = 0;
            WorldPortalRenderer.renderCount = 0;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            prevCameraRoll = cameraRoll;
            cameraRoll *= 0.85F;
            if(Math.abs(cameraRoll) < 0.05F)
            {
                cameraRoll = 0F;
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        Minecraft.getMinecraft().addScheduledTask(this::disconnectFromServer);
    }

    public void disconnectFromServer()
    {
        WorldPortals.eventHandler.monitoredEntities.get(Side.CLIENT).clear();
        WorldPortalRenderer.renderLevel = 0;
        WorldPortalRenderer.rollFactor.clear();
    }
}
