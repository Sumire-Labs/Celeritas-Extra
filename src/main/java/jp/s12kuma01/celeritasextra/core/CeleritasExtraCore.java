package jp.s12kuma01.celeritasextra.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Core loading plugin for Celeritas Extra.
 * Required for early class loading to ensure mixins can be applied to Block classes.
 *
 * Note: MixinBooter's IEarlyMixinLoader is deprecated in CleanroomLoader.
 * Mixin configs are loaded via the MixinConfigs manifest attribute instead.
 */
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("CeleritasExtraCore")
public class CeleritasExtraCore implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // No data injection needed
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
