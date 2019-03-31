package com.fractalmc.commons.client.core;

import com.fractalmc.commons.FractalCommons;
import com.fractalmc.commons.client.core.event.RendererSafeCompatibilityEvent;
import com.fractalmc.commons.client.core.event.ServerPacketableEvent;
import com.fractalmc.commons.client.eula.GuiEulaNotifier;
import com.fractalmc.commons.client.gui.config.GuiConfigs;
import com.fractalmc.commons.client.keybind.KeyBind;
import com.fractalmc.commons.client.render.EntityLatchedRenderer;
import com.fractalmc.commons.client.render.RenderLatchedRenderer;
import com.fractalmc.commons.client.render.RendererHelper;
import com.fractalmc.commons.client.render.item.ItemRenderingHelper;
import com.fractalmc.commons.client.render.world.RenderGlobalProxy;
import com.fractalmc.commons.common.core.config.ConfigBase;
import com.fractalmc.commons.common.core.config.ConfigHandler;
import com.fractalmc.commons.common.core.tracker.EntityTrackerRegistry;
import com.fractalmc.commons.common.core.utils.EntityHelper;
import com.fractalmc.commons.common.core.utils.ObfuscationHelper;
import com.fractalmc.commons.common.grab.GrabHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventHandlerClient
{
    public boolean hasShownFirstGui;
    public boolean connectingToServer;

    public int ticks;
    public float renderTick;
    public boolean hasScreen;

    public int screenWidth;
    public int screenHeight;

    public boolean mouseLeftDown;

    public ArrayList<KeyBind> keyBindList = new ArrayList<>();
    public HashMap<KeyBinding, KeyBind> mcKeyBindList = new HashMap<>();

    protected WorldClient renderGlobalWorldInstance;

    public EventHandlerClient()
    {
        Minecraft mc = Minecraft.getMinecraft();
        screenWidth = mc.displayWidth;
        screenHeight = mc.displayHeight;

        EntityTrackerHandler.init();
    }

    @SubscribeEvent
    public void onRendererSafeCompatibility(RendererSafeCompatibilityEvent event)
    {
        GuiEulaNotifier.createIfRequired();
    }

    public WorldClient getRenderGlobalWorldInstance()
    {
        return renderGlobalWorldInstance;
    }

    @SubscribeEvent
    public void onRenderSpecificHand(RenderSpecificHandEvent event)
    {
        ItemRenderingHelper.onRenderSpecificHand(event);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        renderTick = event.renderTickTime;
        if(event.phase == TickEvent.Phase.START)
        {
            if(screenWidth != mc.displayWidth || screenHeight != mc.displayHeight)
            {
                screenWidth = mc.displayWidth;
                screenHeight = mc.displayHeight;

                for(Framebuffer buffer : RendererHelper.frameBuffers)
                {
                    buffer.createBindFramebuffer(screenWidth, screenHeight);
                }
            }

            if(renderGlobalWorldInstance != mc.renderGlobal.world) //Assume world has changed, eg changing dimension or loading an MC world.
            {
                renderGlobalWorldInstance = mc.renderGlobal.world;

                for(RenderGlobalProxy proxy : RendererHelper.renderGlobalProxies)
                {
                    if(!proxy.released)
                    {
                        proxy.setWorldAndLoadRenderers(renderGlobalWorldInstance);
                    }
                }
            }

            ItemRenderingHelper.handlePreRender(mc);

            EntityTrackerHandler.onRenderTickStart(event);
        }
        else
        {
            ScaledResolution reso = new ScaledResolution(mc);
            GuiEulaNotifier.update();

            if (mc.currentScreen instanceof GuiOptions)
            {
                String s = I18n.translateToLocal("fractalcommons.config.controls.conf");
                int width = Math.round(mc.fontRenderer.getStringWidth(s) / 2F);
                GlStateManager.pushMatrix();
                GlStateManager.translate(reso.getScaledWidth() - width - 2, (reso.getScaledHeight() - (mc.fontRenderer.FONT_HEIGHT / 2D) - 2), 0);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                mc.fontRenderer.drawString(s, 0, 0, 0xffffff, true);
                GlStateManager.popMatrix();

                int i = Mouse.getX() * reso.getScaledWidth() / mc.displayWidth;
                int j = reso.getScaledHeight() - Mouse.getY() * reso.getScaledHeight() / mc.displayHeight - 1;

                if(!mouseLeftDown && Mouse.isButtonDown(0) && i >= (reso.getScaledWidth() - width - 2) && i <= reso.getScaledWidth() && j >= (reso.getScaledHeight() - mc.fontRenderer.FONT_HEIGHT - 2) && j <= reso.getScaledHeight())
                {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    FMLClientHandler.instance().showGuiScreen(new GuiConfigs(mc.currentScreen));
                }

                mouseLeftDown = Mouse.isButtonDown(0);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.phase.equals(TickEvent.Phase.END))
        {
            if(mc.world != null)
            {
                if(connectingToServer)
                {
                    connectingToServer = false;
                    MinecraftForge.EVENT_BUS.post(new ServerPacketableEvent());
                }

                for(KeyBind bind : keyBindList)
                {
                    bind.tick();
                }
                for(Map.Entry<KeyBinding, KeyBind> e : mcKeyBindList.entrySet())
                {
                    if(e.getValue().keyIndex != e.getKey().getKeyCode())
                    {
                        e.setValue(new KeyBind(e.getKey().getKeyCode()));
                    }
                    e.getValue().tick();
                }
                hasScreen = mc.currentScreen != null;

                if(!mc.isGamePaused())
                {
                    EntityTrackerHandler.tick();

                    GrabHandler.tick(Side.CLIENT);

                    if(!ObfuscationHelper.obfuscated() && Minecraft.getMinecraft().getSession().getProfile().getName().equals("iBuyMountainDew") && mc.player.isElytraFlying() && mc.gameSettings.keyBindJump.isKeyDown())
                    {
                        mc.player.motionY += 0.05F;
                    }
                }
            }
            ticks++;
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.side.isClient() && event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getMinecraft();

            ItemRenderingHelper.handlePlayerTick(mc, event.player);
        }
    }

    @SubscribeEvent
    public void onClientConnection(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        connectingToServer = true;

        for(ConfigBase conf : ConfigHandler.configs)
        {
            conf.storeSession();
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        IThreadListener thread = Minecraft.getMinecraft();

        if(thread.isCallingFromMinecraftThread())
        {
            onClientDisconnect();
        }
        else
        {
            thread.addScheduledTask(this::onClientDisconnect);
        }
    }

    public void onClientDisconnect()
    {
        EntityTrackerHandler.onClientDisconnect();

        GrabHandler.grabbedEntities.get(Side.CLIENT).clear();

        for(ConfigBase conf : ConfigHandler.configs)
        {
            conf.resetSession();
        }

        EntityHelper.profileCache = null;
        EntityHelper.sessionService = null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInitGuiPost(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if(!hasShownFirstGui)
        {
            hasShownFirstGui = true;
            MinecraftForge.EVENT_BUS.post(new RendererSafeCompatibilityEvent());
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        if(event.getWorld().isRemote)
        {
            for(GrabHandler handler : GrabHandler.grabbedEntities.get(Side.CLIENT))
            {
                handler.grabber = null;
                handler.grabbed = null;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(EntityJoinWorldEvent event)
    {
        EntityTrackerHandler.onEntitySpawn(event);
    }

    @SubscribeEvent
    public void onLatchedRendererRender(RenderLatchedRenderer.RenderLatchedRendererEvent event)
    {
        EntityTrackerHandler.onLatchedRendererRender(event); //patron render is there
    }

    @Deprecated //Use EntityTrackerHandler instead;
    public EntityTrackerRegistry getEntityTrackerRegistry()
    {
        return EntityTrackerHandler.getEntityTrackerRegistry();
    }

    @SubscribeEvent
    public void onGuiActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event)
    {
        if(!ObfuscationHelper.obfuscated() && Minecraft.getMinecraft().getSession().getProfile().getName().equals("iBuyMountainDew") && (event.getGui().getClass() == GuiIngameMenu.class && event.getButton().id == 12 || event.getGui().getClass() == GuiMainMenu.class && event.getButton().id == 6) && !GuiScreen.isShiftKeyDown())
        {
            event.setCanceled(true);

            Minecraft.getMinecraft().displayGuiScreen(new GuiConfigs(Minecraft.getMinecraft().currentScreen));
        }
    }
}
