package com.fractalmc.commons.api.portality;

public class PortalityApi
{
    private static IPortalityApi portalityAPI = new PortalityApiDummy();

    public static IPortalityApi getApi()
    {
        return portalityAPI;
    }

    public static void setApi(IPortalityApi api)
    {
        PortalityApi.portalityAPI = api;
    }
}
