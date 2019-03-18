package com.fractalmc.commons;

import com.fractalmc.commons.common.core.FractalLogger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

@Mod(
          modid = Constants.MOD_ID,
          name = Constants.MOD_NAME,
          version = Constants.COMPILED_VERSION,
          acceptedMinecraftVersions = Constants.COMPILED_VERSION,
          dependencies = Constants.DEPENDENCIES,
          certificateFingerprint = Constants.CERT_FINGERPRINT
)
public class FractalCommons
{
    public static FractalLogger internalLogger = FractalLogger.createFractalLogger(Constants.MOD_NAME_ALT);

    @Mod.Instance(Constants.MOD_ID)
    public static FractalCommons instance;

    @SidedProxy(clientSide = Constants.CLIENT_PROXY_PATH, serverSide = Constants.COMMON_PROXY_PATH)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event)
    {
        proxy.onPreInit(event);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event)
    {
        proxy.onInit(event);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event)
    {
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
