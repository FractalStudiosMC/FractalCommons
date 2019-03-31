package com.fractalmc.commons.common.modules.portality.client.render.world.factory;

import com.fractalmc.commons.common.modules.portality.client.render.world.chunk.ListedRenderChunkWorldPortal;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.world.World;

public class ListChunkFactory implements IRenderChunkFactory
{
    public RenderChunk create(World worldIn, RenderGlobal globalRenderer, int index)
    {
        return new ListedRenderChunkWorldPortal(worldIn, globalRenderer, index);
    }
}