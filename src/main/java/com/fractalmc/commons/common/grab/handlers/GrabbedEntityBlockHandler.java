package com.fractalmc.commons.common.grab.handlers;

import com.fractalmc.commons.common.entity.EntityBlock;
import com.fractalmc.commons.common.grab.GrabHandler;
import net.minecraft.entity.Entity;

public class GrabbedEntityBlockHandler implements GrabHandler.GrabbedEntityHandler
{
    @Override
    public boolean eligible(Entity grabbed)
    {
        return grabbed instanceof EntityBlock;
    }

    @Override
    public void handle(GrabHandler grabHandler)
    {
        ((EntityBlock)grabHandler.grabbed).timeExisting = 2;
    }
}
