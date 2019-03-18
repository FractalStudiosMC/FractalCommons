package com.fractalmc.commons.api.portality;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PortalityApiDummy implements IPortalityApi
{
    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderLevel()
    {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getCameraRoll(int renderLevel, float partialTick)
    {
        return 0F;
    }
}
