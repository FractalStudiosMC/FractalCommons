package com.fractalmc.commons.common.grab.handlers;

import com.fractalmc.commons.common.grab.GrabHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;

public class GrabbedFallingBlockHandler implements GrabHandler.GrabbedEntityHandler
{
    @Override
    public boolean eligible(Entity grabbed)
    {
        return grabbed instanceof EntityFallingBlock;
    }

    @Override
    public void handle(GrabHandler grabHandler)
    {
        ((EntityFallingBlock)grabHandler.grabbed).fallTime = 2;
    }
}
