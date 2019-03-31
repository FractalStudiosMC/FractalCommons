package com.fractalmc.commons;

import com.fractalmc.commons.client.core.EventHandlerClient;
import com.fractalmc.commons.common.core.FractalLogger;
import com.fractalmc.commons.common.core.config.ConfigHandler;
import com.fractalmc.commons.common.core.config.annotations.IntBool;
import com.fractalmc.commons.common.core.config.annotations.IntMinMax;
import com.fractalmc.commons.common.core.event.EventHandlerServer;
import com.fractalmc.commons.common.core.network.PacketChannel;
import com.fractalmc.commons.common.core.utils.ObfuscationHelper;
import com.fractalmc.commons.common.modules.portality.common.WorldPortals;
import com.fractalmc.commons.common.modules.updately.UpdateChecker;
import com.fractalmc.commons.common.core.config.ConfigBase;
import com.fractalmc.commons.common.core.config.annotations.ConfigProp;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.lang.reflect.Field;

@Mod(
          modid = Constants.MOD_ID,
          name = Constants.MOD_NAME,
          version = Constants.COMPILED_VERSION,
          acceptedMinecraftVersions = Constants.COMPILED_VERSION,
          dependencies = Constants.DEPENDENCIES,
          certificateFingerprint = Constants.CERT_FINGERPRINT,
          useMetadata = Constants.USE_METADATA
)
public class FractalCommons
{
    public static FractalLogger internalLogger = FractalLogger.createFractalLogger(Constants.MOD_NAME_ALT);
    public static FMLCommonHandler commonHandler;
    public static PacketChannel channel;
    public static EventHandlerClient eventHandlerClient;
    public static EventHandlerServer eventHandlerServer;

    public static Config config;

    public static boolean userIsPatron;
    private static boolean hasPostInit;
    private static boolean hasInitiative;

    public class Config extends ConfigBase
    {
        @ConfigProp(category = "clientOnly", side = Side.CLIENT, changeable = false)
        @IntBool
        public int enableStencils = 1;

        @ConfigProp(category = "clientOnly", side = Side.CLIENT)
        @IntBool
        public int enableLatchedRendererSpawn = 1;

        //EULA module
        @ConfigProp(module = "eula")
        public String eulaAcknowledged = "";

        // Portality module
        @ConfigProp(module = "portality", side = Side.CLIENT, hidden = true)
        @IntMinMax(min = 0, max = 10)
        public int maxRecursion = 2;

        @ConfigProp(module = "portality", side = Side.CLIENT, hidden = true)
        @IntMinMax(min = 1, max = 0xff)
        public int stencilValue = 0x2f;

        @ConfigProp(module = "portality", side = Side.CLIENT, hidden = true)
        @IntMinMax(min = 0, max = 16)
        public int renderDistanceChunks = 0;

        @ConfigProp(module = "portality", side = Side.CLIENT, hidden = true)
        @IntMinMax(min = 1, max = 100)
        public int maxRendersPerTick = 10;

        //End Modules

        public Config(File file)
        {
            super(file);
        }

        @Override
        public String getModId()
        {
            return Constants.MOD_ID;
        }

        @Override
        public String getModName()
        {
            return Constants.MOD_NAME;
        }

        @Override
        public void onConfigChange(Field field, Object original) //Nested int array and keybind original is the new var, no ori cause lazy
        {

        }
    }

    @Mod.Instance(Constants.MOD_ID)
    public static FractalCommons instance;

    @SidedProxy(clientSide = Constants.CLIENT_PROXY_PATH, serverSide = Constants.COMMON_PROXY_PATH)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event)
    {
        ObfuscationHelper.detectObfuscation();

        config = ConfigHandler.registerConfig(new Config(event.getSuggestedConfigurationFile()));

        proxy.onPreInit(event);

        UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(Constants.MOD_NAME, Constants.VERSION_OF_MC, Constants.COMPILED_VERSION, false));
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event)
    {
        proxy.onInit(event);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event)
    {
        hasPostInit = true;
        hasInitiative = Loader.isModLoaded("im");

        proxy.onPostInit(event);
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event)
    {
        proxy.onLoadComplete(event);
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event)
    {
        proxy.onServerStarting(event);
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event)
    {
        UpdateChecker.serverStarted();
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event)
    {
        eventHandlerServer.shuttingDownServer();
        WorldPortals.onServerStopping();
    }

    @Mod.EventHandler
    public void onIMCMessage(FMLInterModComms.IMCEvent event)
    {
        for(FMLInterModComms.IMCMessage message : event.getMessages())
        {
            if(message.key.equalsIgnoreCase("update") && message.isStringMessage())
            {
                String[] split = message.getStringValue().split(">");
                if(split.length != 4)
                {
                    internalLogger.logInfo("Invalid update checker string " + message.getStringValue() + ". Invalid argument count!");
                }
                else //Mod name, MC version, mod version, clientSideOnly
                {
                    UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(split[0], split[1], split[2], split[3].equalsIgnoreCase("true")));
                }
            }
        }
    }

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event)
    {
        proxy.onFingerprintViolation(event);

        if (event.isDirectory())
        {
            internalLogger.logWarn("An invalid fingerprint was detected for Fractal Commons. You're in a dev environment though, so it's perfectly normal! :D");
        }
        else
        {
            internalLogger.logError("An invalid fingerprint was detected for Fractal Commons! It should be " + event.getExpectedFingerprint() + " !");
        }
    }
}
