package com.fractalmc.commons.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

public class TextureAtlasSpriteBufferedImage extends TextureAtlasSprite
{
    public BufferedImage image;

    public TextureAtlasSpriteBufferedImage(ResourceLocation rl, BufferedImage image)
    {
        super(rl.toString());

        this.image = image;
    }

    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location)
    {
        return true;
    }

    public boolean load(IResourceManager manager, ResourceLocation location)
    {
        width = image.getWidth();
        height = image.getHeight();

        int[][] aint = new int[Minecraft.getMinecraft().getTextureMapBlocks().getMipmapLevels() + 1][];
        aint[0] = new int[image.getWidth() * image.getHeight()];

        image.getRGB(0, 0, image.getWidth(), image.getHeight(), aint[0], 0, image.getWidth());

        framesTextureData.add(aint);

        initSprite(width, height, 0, 0, false);

        return false;
    }
}
