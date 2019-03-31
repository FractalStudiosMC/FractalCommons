package com.fractalmc.commons.client.gui.config.window;

import com.fractalmc.commons.client.gui.config.GuiConfigs;
import com.fractalmc.commons.client.gui.config.window.element.ElementCatsList;
import com.fractalmc.commons.client.gui.window.Window;
import com.fractalmc.commons.client.gui.window.element.Element;
import com.fractalmc.commons.client.gui.window.element.ElementListTree;
import com.fractalmc.commons.common.core.config.ConfigBase;

import java.util.ArrayList;
import java.util.Map;

public class WindowCats extends Window
{
    public ElementCatsList configs;

    public GuiConfigs parent;
    public ConfigBase selectedConfig;

    public WindowCats(GuiConfigs parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "", true);
        this.parent = parent;

        configs = new ElementCatsList(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - (BORDER_SIZE * 2 + 2) - 11, 3, false, false);
        elements.add(configs);
    }

    @Override
    public void resized()
    {
        posX = 100;
        posY = 1;
        width = 120;
        height = workspace.height - 2;

        super.resized();
    }

    public void draw(int mouseX, int mouseY) //4 pixel border?
    {
        if(parent.windowConfigs.configs.selectedIdentifier.isEmpty())
        {
            return;
        }
        else
        {
            for(ElementListTree.Tree tree : parent.windowConfigs.configs.trees)
            {
                if(tree.selected)
                {
                    ConfigBase conf = (ConfigBase)tree.attachedObject;
                    if(conf != selectedConfig)
                    {
                        //NEW SELECTED CONFIG;
                        selectedConfig = conf;
                        titleLocale = selectedConfig.getModName();
                        configs.selectedIdentifier = "";
                        configs.trees.clear();

                        for(Map.Entry<ConfigBase.CategoryInfo, ArrayList<ConfigBase.PropInfo>> e : selectedConfig.categories.entrySet())
                        {
                            configs.createTree(null, e.getKey(), 13, 0, false, false);
                        }
                    }
                }
            }
        }
        super.draw(mouseX, mouseY);
    }

    @Override
    public void elementTriggered(Element element)
    {
    }

    @Override
    public boolean canBeDragged()
    {
        return false;
    }

    @Override
    public boolean isStatic()
    {
        return true;
    }

    @Override
    public boolean canMinimize() { return false; }
}
