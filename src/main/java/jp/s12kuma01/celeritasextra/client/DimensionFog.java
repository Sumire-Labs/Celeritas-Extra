package jp.s12kuma01.celeritasextra.client;

import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions.ResolvedFog;
import net.minecraft.client.Minecraft;

public final class DimensionFog {
    private DimensionFog() {}

    public static ResolvedFog current() {
        var world = Minecraft.getMinecraft().world;
        var settings = CeleritasExtraClientMod.options().renderSettings;
        return world == null ? new ResolvedFog(settings.fog, settings.fogStart, settings.fogDistance)
                : settings.resolveFog(world.provider.getDimension());
    }
}
