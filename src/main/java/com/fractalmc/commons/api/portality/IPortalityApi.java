package com.fractalmc.commons.api.portality;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPortalityApi
{
    @SideOnly(Side.CLIENT)
    int getRenderLevel();

    @SideOnly(Side.CLIENT)
    float getCameraRoll(int renderLevel, float partialTick);
}
