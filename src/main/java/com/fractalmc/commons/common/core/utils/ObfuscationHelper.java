package com.fractalmc.commons.common.core.utils;

import com.fractalmc.commons.Constants;
import com.fractalmc.commons.FractalCommons;
import com.fractalmc.commons.common.block.BlockDummy;

import java.lang.reflect.Field;

/**
 * A utility class used to detect an obfuscated version of MC.
 */
public class ObfuscationHelper
{
    private static final String OBF_VERSION = "1.12.2";

    private static boolean isObfuscated;

    /**
     * Detect an obfuscated environment, crash the game if you're running
     * a deobf mod jar in an obfuscated environment.
     */
    public static void detectObfuscation()
    {
        isObfuscated = true;

        try
        {
            Field[] fields = Class.forName("net.minecraft.item.ItemBlock").getDeclaredFields();

            for (Field f : fields)
            {
                f.setAccessible(true);

                if (f.getName().equalsIgnoreCase("block"))
                {
                    isObfuscated = false;

                    if (!Constants.VERSION_OF_MC.equals(OBF_VERSION))
                    {
                        FractalCommons.internalLogger.logFatalError("ObfuscationHelper string OBF_VERSION is not updated to match the current MC version! Bad Dewy! Fix it NOW!");

                        FractalCommons.commonHandler.exitJava(-1, true);
                    }

                    return;
                }
            }

            BlockDummy.class.getDeclaredMethod("func_149722_s");
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("Oi! You're running the deobf version of Fractal Commons in an obfuscated environment! You really shouldn't do this...");
        }
        catch (Exception ignored)
        {

        }
    }

    /**
     * Is MC obfuscated?
     *
     * @return If MC is obfuscated
     */
    public static boolean obfuscated()
    {
        return isObfuscated;
    }
}
