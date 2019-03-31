package com.fractalmc.commons.common.modules.portality.common.core;

import com.fractalmc.commons.api.portality.IPortalityApi;
import com.fractalmc.commons.common.modules.portality.client.render.WorldPortalRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ApiImpl implements IPortalityApi
{
    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderLevel()
    {
        return WorldPortalRenderer.renderLevel;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getCameraRoll(int renderLevel, float partialTick)
    {
        return WorldPortalRenderer.getRollFactor(renderLevel, partialTick);
    }
}
