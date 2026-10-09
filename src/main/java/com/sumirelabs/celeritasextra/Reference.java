package com.sumirelabs.celeritasextra;

/** Build constants replaced by TokenEnvoy before javac inlines them into @Mod. */
public final class Reference {
    public static final String MOD_ID = "@{MOD_ID}";
    public static final String MOD_NAME = "@{MOD_NAME}";
    public static final String VERSION = "@{MOD_VERSION}";

    private Reference() {
    }
}
