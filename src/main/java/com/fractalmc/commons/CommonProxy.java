package com.fractalmc.commons;

import com.fractalmc.commons.client.keybind.KeyBind;
import com.fractalmc.commons.common.core.config.ConfigBase;
import com.fractalmc.commons.common.core.config.ConfigHandler;
import com.fractalmc.commons.common.core.event.EventHandlerServer;
import com.fractalmc.commons.common.core.network.PacketChannel;
import com.fractalmc.commons.common.core.utils.EntityHelper;
import com.fractalmc.commons.common.core.utils.EventCalendar;
import com.fractalmc.commons.common.entity.EntityBlock;
import com.fractalmc.commons.common.packet.BlockEntityDataPackets;
import com.fractalmc.commons.common.packet.PacketSession;
import com.fractalmc.commons.common.packet.mod.PacketNewGrabbedEntityId;
import com.fractalmc.commons.common.thread.ThreadGetResources;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CommonProxy
{
    public void onPreInit(FMLPreInitializationEvent event)
    {
        EventCalendar.checkDate();

        FractalCommons.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(FractalCommons.eventHandlerServer);

        EntityRegistry.registerModEntity(new ResourceLocation("fractalcommons", "entity_block"), EntityBlock.class, "EntityBlock", 500, FractalCommons.instance, 160, 20, true);

        FractalCommons.channel = new PacketChannel(Constants.MOD_ID, PacketSession.class, BlockEntityDataPackets.PacketBlockEntityData.class, PacketNewGrabbedEntityId.class, BlockEntityDataPackets.PacketRequestBlockEntityData.class);
    }

    public void onInit(FMLInitializationEvent event)
    {
        (new ThreadGetResources(FMLCommonHandler.instance().getSide())).start();
    }

    public void onPostInit(FMLPostInitializationEvent event)
    {
        for (ConfigBase cfg : ConfigHandler.configs)
        {
            cfg.setup();
        }

        if(!(FractalCommons.config.eulaAcknowledged.equalsIgnoreCase("true") || FractalCommons.config.eulaAcknowledged.equalsIgnoreCase(getPlayerName())))
        {
            FractalCommons.internalLogger.logWarn("=============================================================");
            FractalCommons.internalLogger.logWarn(I18n.translateToLocal("fractalcommons.eula.message"));
            FractalCommons.internalLogger.logWarn(I18n.translateToLocal("fractalcommons.eula.messageServer"));
            FractalCommons.internalLogger.logWarn("=============================================================");
        }
    }

    public void onLoadComplete(FMLLoadCompleteEvent event)
    {

    }

    public void onServerStarting(FMLServerStartingEvent event)
    {

    }

    public void onFingerprintViolation(FMLFingerprintViolationEvent event)
    {

    }

    public String getPlayerId()
    {
        return EntityHelper.UUID_DUMMY.toString().replaceAll("-", "");
    }

    public String getPlayerName()
    {
        return "Server";
    }

    public void setGameProfileLookupService()
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        EntityHelper.sessionService = server.getMinecraftSessionService();
        EntityHelper.profileCache = server.getPlayerProfileCache();
    }

    public void nudgeHand(float mag)
    {

    }

    public void adjustRotation(Entity entity, float yawChange, float pitchChange)
    {
        entity.prevRotationYaw += yawChange;
        entity.rotationYaw += yawChange;
        entity.prevRotationPitch += pitchChange;
        entity.rotationPitch += pitchChange;
        entity.prevRotationYaw = entity.prevRotationYaw % 360F;
        entity.rotationYaw = entity.rotationYaw % 360F;

        for(; entity.prevRotationYaw < 0F; entity.prevRotationYaw += 360F)
        {

        }

        for(; entity.rotationYaw < 0F; entity.rotationYaw += 360F)
        {

        }

        entity.prevRotationPitch = entity.prevRotationPitch % 90.05F;
        entity.rotationPitch = entity.rotationPitch % 90.05F;
    }

    public EntityPlayer getMcPlayer()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public KeyBind registerKeyBind(KeyBind bind, KeyBind replacing) { return bind; }

    /**
     * Please note that this keybind will trigger without checking for SHIFT/CTRL/ALT being held down. That checking has to be done on your end.
     *
     * @param bind Minecraft Keybind
     */
    @SideOnly(Side.CLIENT)
    public void registerMinecraftKeyBind(KeyBinding bind) {}
}