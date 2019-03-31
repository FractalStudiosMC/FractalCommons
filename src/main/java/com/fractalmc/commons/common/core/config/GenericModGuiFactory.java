package com.fractalmc.commons.common.core.config;

import com.fractalmc.commons.client.gui.config.GuiConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

public class GenericModGuiFactory implements IModGuiFactory
{
    @Override
    public void initialize(Minecraft mc)
    {
    }

    @Override
    public boolean hasConfigGui()
    {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen)
    {
        return new GuiConfigs(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }
}
