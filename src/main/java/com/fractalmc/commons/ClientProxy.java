package com.fractalmc.commons;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.*;

public class ClientProxy extends CommonProxy
{
    @Override
    public void onPreInit(FMLPreInitializationEvent event)
    {
        super.onPreInit(event);
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
    }

    @Override
    public EntityPlayer getMcPlayer()
    {
        return Minecraft.getMinecraft().player;
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
}