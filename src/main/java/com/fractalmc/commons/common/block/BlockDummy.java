package com.fractalmc.commons.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/**
 * A dummy block for use in obfuscation detection.
 */
public class BlockDummy extends Block
{
    /** Dummy Constructor */
    public BlockDummy()
    {
        super(Material.AIR);
    }

    /** Dummy method for obfuscation detection */
    @Override
    public Block setBlockUnbreakable()
    {
        return super.setBlockUnbreakable();
    }
}
