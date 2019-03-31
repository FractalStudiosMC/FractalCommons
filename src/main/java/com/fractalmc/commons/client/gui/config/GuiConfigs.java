package com.fractalmc.commons.client.gui.config;

import com.fractalmc.commons.client.gui.config.window.WindowCats;
import com.fractalmc.commons.client.gui.config.window.WindowConfigs;
import com.fractalmc.commons.client.gui.config.window.WindowSetter;
import com.fractalmc.commons.client.gui.window.IWorkspace;
import com.fractalmc.commons.client.gui.window.Window;
import com.fractalmc.commons.client.gui.window.element.Element;
import com.fractalmc.commons.client.gui.window.element.ElementToggle;
import com.fractalmc.commons.common.core.config.ConfigBase;
import com.fractalmc.commons.common.core.config.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiConfigs extends IWorkspace
{
    public int oriScale;
    public GuiScreen oriScreen;

    public WindowConfigs windowConfigs;
    public WindowCats windowCats;
    public WindowSetter windowSetter;

    public boolean needsRestart;

    public int keyBindTimeout;

    public GuiConfigs(GuiScreen screen)
    {
        VARIABLE_LEVEL = 0;

        Minecraft mc = Minecraft.getMinecraft();

        oriScale = mc.gameSettings.guiScale;
        mc.gameSettings.guiScale = 2;

        oriScreen = screen;

        windowConfigs = new WindowConfigs(this, 0, 0, 0, 0, 0, 0);
        windowCats = new WindowCats(this, 0, 0, 0, 0, 0, 0);
        windowSetter = new WindowSetter(this, 0, 0, 0, 0, 0, 0);
        addWindowOnTop(windowConfigs);
        addWindowOnTop(windowCats);
        addWindowOnTop(windowSetter);

        for(ConfigBase config : ConfigHandler.configs)
        {
            config.enterConfigScreen();
        }

        tooltipTime = 0;
    }

    public GuiConfigs(int scale, GuiScreen screen)
    {
        this(screen);
        oriScale = scale;
        oriScreen = screen;
    }

    @Override
    public void initGui()
    {
        super.initGui();
    }

    @Override
    public void onGuiClosed()
    {
        for(ConfigBase config : ConfigHandler.configs)
        {
            config.exitConfigScreen();
        }

        Keyboard.enableRepeatEvents(false);

        Minecraft.getMinecraft().gameSettings.guiScale = oriScale;
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        if(keyBindTimeout > 0)
        {
            keyBindTimeout--;
        }
    }

    @Override
    public void addToDock(int i, Window w)
    {
    }

    @Override
    public boolean canClickOnElement(Window window, Element element)
    {
        return true;
    }

    public void needsRestart()
    {
        if(!needsRestart)
        {
            windowConfigs.elements.add(new ElementToggle(windowConfigs, windowConfigs.width - 16 - 10, windowConfigs.height - 22, 16, 16, -100, true, 1, 1, "!", "fractalcommons.config.gui.needsRestart", true));
            windowConfigs.resized();
        }
        needsRestart = true;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float renderTick)
    {
        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        ScaledResolution resolution = new ScaledResolution(mc);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0.0D, -5000.0D, 5000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();

        GlStateManager.pushMatrix();

        GlStateManager.clearColor((float)currentTheme.workspaceBackground[0] / 255F, (float)currentTheme.workspaceBackground[1] / 255F, (float)currentTheme.workspaceBackground[2] / 255F, 255F);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        boolean onWindow = drawWindows(mouseX, mouseY);

        int scroll = Mouse.getDWheel();

        updateElementHovered(mouseX, mouseY, scroll);

        GlStateManager.popMatrix();

        updateKeyStates();

        updateWindowDragged(mouseX, mouseY);

        updateElementDragged(mouseX, mouseY);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
    }

    @Override
    public void keyTyped(char c, int key)
    {
        if(key == 1)
        {
            this.mc.displayGuiScreen(oriScreen);

            if(this.mc.currentScreen == null)
            {
                this.mc.setIngameFocus();
            }
        }
        else if(elementSelected != null)
        {
            elementSelected.keyInput(c, key);
        }
    }
}
