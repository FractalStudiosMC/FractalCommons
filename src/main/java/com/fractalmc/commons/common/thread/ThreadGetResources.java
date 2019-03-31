package com.fractalmc.commons.common.thread;

import com.fractalmc.commons.FractalCommons;
import com.fractalmc.commons.common.modules.updately.UpdateChecker;
import com.google.gson.Gson;
import net.minecraftforge.fml.relauncher.Side;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

public class ThreadGetResources extends Thread
{
    private static final String PATRON_LIST  = "https://fractalstudiosmc.github.io/HostyHost/patrons.json";
    private static final String VERSION_LIST = "https://fractalstudiosmc.github.io/HostyHost/versions.json";

    private final Side side;

    public ThreadGetResources(Side side)
    {
        this.setName("Fractal Commons Online Resource Thread");
        this.setDaemon(true);
        this.side = side;
    }

    @Override
    public void run()
    {
        //Check to see if the current client is a patron.
        if(side.isClient())
        {
            try
            {
                Gson gson = new Gson();
                Reader fileIn = new InputStreamReader(new URL(PATRON_LIST).openStream());
                String[] json = gson.fromJson(fileIn, String[].class);
                fileIn.close();

                if(json != null)
                {
                    for(String s : json)
                    {
                        if(s.replaceAll("-", "").equalsIgnoreCase(FractalCommons.proxy.getPlayerId()))
                        {
                            FractalCommons.userIsPatron = true;
                        }
                    }
                }
            }
            catch(UnknownHostException e)
            {
                FractalCommons.internalLogger.logWarn("Error retrieving Fractal Commons patron list: UnknownHostException. Is your internet connection working?");
            }
            catch(Exception e)
            {
                FractalCommons.internalLogger.logWarn("Error retrieving Fractal Commons patron list.");
                e.printStackTrace();
            }
        }
        try
        {
            Gson gson = new Gson();
            Reader fileIn = new InputStreamReader(new URL(VERSION_LIST).openStream());
            UpdateChecker.processModsList(gson.fromJson(fileIn, Map.class));
        }
        catch(UnknownHostException e)
        {
            FractalCommons.internalLogger.logWarn("Error retrieving mods versions list: UnknownHostException. Is your internet connection working?");
        }
        catch(Exception e)
        {
            FractalCommons.internalLogger.logWarn("Error retrieving mods versions list.");
            e.printStackTrace();
        }
    }
}
