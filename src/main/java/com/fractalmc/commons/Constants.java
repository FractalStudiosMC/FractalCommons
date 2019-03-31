package com.fractalmc.commons;

public class Constants
{
    // Basic Mod Constants

    public static final String MOD_ID = "fractalcommons";
    public static final String MOD_NAME = "Fractal Commons";
    public static final String MOD_NAME_ALT = "FractalCommons";

    // Versioning Constants

    public static final String VERSION_CAT = "Alpha ";

    public static final int VERSION_MAJ = 1;
    public static final int VERSION_MIN = 0;
    public static final int VERSION_PAT = 1;

    public static final String COMPILED_VERSION = VERSION_MAJ + "." + VERSION_MIN + "." + VERSION_PAT;

    public static final String VERSION_BLD = "Build 0000a";

    public static final String VERSION_MC = "[1.12]";

    // Dependency Constants

    public static final String DEPENDENCIES_FORGE = "required-after:forge@[14.21.0.2320,); "; // The space is important!
    public static final String DEPENDENCIES_MODS = "after:ic2; after:tconstruct; after:mantle;";

    public static final String DEPENDENCIES = DEPENDENCIES_FORGE + DEPENDENCIES_MODS;

    // Proxy Constants

    public static final String COMMON_PROXY_PATH = "com.fractalmc.commons.CommonProxy";
    public static final String CLIENT_PROXY_PATH = "com.fractalmc.commons.ClientProxy";

    // Other Constants

    public static final boolean USE_METADATA = true;
    public static final String CERT_FINGERPRINT = "9d4785c5d5a12b1349c3ef7c27c5914ce819410d";
    public static final String VERSION_OF_MC = "1.12.2";
}