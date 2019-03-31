package com.fractalmc.commons;

import com.fractalmc.commons.client.core.EventHandlerClient;
import com.fractalmc.commons.client.keybind.KeyBind;
import com.fractalmc.commons.client.render.EntityLatchedRenderer;
import com.fractalmc.commons.client.render.RenderLatchedRenderer;
import com.fractalmc.commons.client.render.RendererHelper;
import com.fractalmc.commons.client.render.entity.RenderBlock;
import com.fractalmc.commons.common.core.config.ConfigHandler;
import com.fractalmc.commons.common.core.utils.EntityHelper;
import com.fractalmc.commons.common.core.utils.ResourceHelper;
import com.fractalmc.commons.common.entity.EntityBlock;
import com.google.common.base.Splitter;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class ClientProxy extends CommonProxy
{
    @Override
    public void onPreInit(FMLPreInitializationEvent event)
    {
        super.onPreInit(event);

        ResourceHelper.init();
        RendererHelper.init();

        File keybindConf = new File(ResourceHelper.getConfigFolder(), "fractalcommons_keybinds.cfg");

        ConfigHandler.configKeybind = new Configuration(keybindConf);
        ConfigHandler.configKeybind.load();

        List cms = Splitter.on("\\n").splitToList(I18n.translateToLocal("fractalcommons.config.cat.keybind.comment"));
        String cm = "";

        for(int ll = 0; ll < cms.size(); ll++)
        {
            cm = cm + cms.get(ll);

            if(ll != cms.size() - 1)
            {
                cm = cm + "\n";
            }
        }

        ConfigHandler.configKeybind.addCustomCategoryComment("keybinds", cm);

        EntityHelper.injectMinecraftPlayerGameProfile();

        FractalCommons.eventHandlerClient = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(FractalCommons.eventHandlerClient);

        RenderingRegistry.registerEntityRenderingHandler(EntityLatchedRenderer.class, new RenderLatchedRenderer.RenderFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntityBlock.class, new RenderBlock.RenderFactory());
    }

    @Override
    public void onInit(FMLInitializationEvent event)
    {
        super.onInit(event);
    }

    @Override
    public void onPostInit(FMLPostInitializationEvent event)
    {
        super.onPostInit(event);

        if (ConfigHandler.configKeybind.hasChanged())
        {
            ConfigHandler.configKeybind.save();
        }
    }

    @Override
    public void onLoadComplete(FMLLoadCompleteEvent event)
    {
        super.onLoadComplete(event);
    }

    @Override
    public void onServerStarting(FMLServerStartingEvent event)
    {
        super.onServerStarting(event);
    }

    @Override
    public void onFingerprintViolation(FMLFingerprintViolationEvent event)
    {
        super.onFingerprintViolation(event);
    }

    @Override
    public String getPlayerId()
    {
        return Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "");
    }

    @Override
    public String getPlayerName()
    {
        return Minecraft.getMinecraft().getSession().getUsername();
    }

    @Override
    public void setGameProfileLookupService()
    {
        YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), UUID.randomUUID().toString());

        EntityHelper.sessionService = yggdrasilAuthenticationService.createMinecraftSessionService();

        GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();

        EntityHelper.profileCache = new PlayerProfileCache(gameProfileRepository, new File(Minecraft.getMinecraft().mcDataDir, MinecraftServer.USER_CACHE_FILE.getName()));
    }

    @Override
    public void adjustRotation(Entity ent, float yawChange, float pitchChange)
    {
        float prevYaw = 0.0F;
        float yaw = 0.0F;
        float prevPitch = 0.0F;
        float pitch = 0.0F;

        if(ent instanceof EntityPlayerSP)
        {
            EntityPlayerSP player = (EntityPlayerSP)ent;
            prevYaw = player.prevRotationYaw - player.prevRenderArmYaw;
            yaw = player.rotationYaw - player.renderArmYaw;
            prevPitch = player.prevRotationPitch - player.prevRenderArmPitch;
            pitch = player.rotationPitch - player.renderArmPitch;
        }

        super.adjustRotation(ent, yawChange, pitchChange);

        if(ent instanceof EntityPlayerSP)
        {
            EntityPlayerSP player = (EntityPlayerSP)ent;
            player.prevRenderArmYaw = player.prevRotationYaw;
            player.renderArmYaw = player.rotationYaw;
            player.prevRenderArmPitch = player.prevRotationPitch;
            player.renderArmPitch = player.rotationPitch;
            player.prevRenderArmYaw -= prevYaw;
            player.renderArmYaw -= yaw;
            player.prevRenderArmPitch -= prevPitch;
            player.renderArmPitch -= pitch;
        }
    }

    @Override
    public void nudgeHand(float mag)
    {
        Minecraft.getMinecraft().player.renderArmPitch += mag;
    }

    @Override
    public EntityPlayer getMcPlayer()
    {
        return Minecraft.getMinecraft().player;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public KeyBind registerKeyBind(KeyBind bind, KeyBind replacing)
    {
        if(replacing != null)
        {
            if(bind.equals(replacing))
            {
                return replacing;
            }
            for(int i = FractalCommons.eventHandlerClient.keyBindList.size() - 1; i >= 0; i--)
            {
                KeyBind keybind = FractalCommons.eventHandlerClient.keyBindList.get(i);
                if(keybind.equals(replacing))
                {
                    keybind.usages--;
                    if(keybind.usages <= 0)
                    {
                        FractalCommons.eventHandlerClient.keyBindList.remove(i);
                    }
                    bind.ignoreHold = keybind.ignoreHold;
                }
            }
        }

        for(KeyBind keybind : FractalCommons.eventHandlerClient.keyBindList)//Check to see if the keybind is already registered. If it is, increase usages count. If not, add it.
        {
            if(keybind.equals(bind))
            {
                keybind.usages++;
                return keybind;
            }
        }

        bind.usages++;
        FractalCommons.eventHandlerClient.keyBindList.add(bind);

        return bind;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerMinecraftKeyBind(KeyBinding bind)
    {
        FractalCommons.eventHandlerClient.mcKeyBindList.put(bind, (new KeyBind(bind.getKeyCode(), false, false, false, true)).setIsMinecraftBind());
    }
}