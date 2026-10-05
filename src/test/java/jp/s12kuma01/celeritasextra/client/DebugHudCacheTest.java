package jp.s12kuma01.celeritasextra.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import jp.s12kuma01.celeritasextra.mixin.steady_debug_hud.MixinForgeGuiDebugOverlay;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DebugHudCacheTest {
    @Test void cachedFramesDoNotReapplyRendererRowsOrExposeTheMutableCache() throws Exception {
        var overlay = new MixinForgeGuiDebugOverlay() { };
        var wrap = MixinForgeGuiDebugOverlay.class.getDeclaredMethod("celeritasExtra$cacheLeftDebugText", Operation.class);
        var rebuild = MixinForgeGuiDebugOverlay.class.getDeclaredField("celeritasExtra$rebuild");
        wrap.setAccessible(true);
        rebuild.setAccessible(true);
        var calls = new AtomicInteger();
        Operation<List<String>> original = args -> {
            calls.incrementAndGet();
            return new ArrayList<>(List.of("60 fps", "Nothirium GL43", "Chunks:", "  Solid: 10"));
        };
        assertEquals(4, ((List<?>) wrap.invoke(overlay, original)).size());
        rebuild.setBoolean(overlay, false);
        @SuppressWarnings("unchecked")
        var cached = (List<String>) wrap.invoke(overlay, original);
        cached.add("another consumer's annotation");
        assertEquals(4, ((List<?>) wrap.invoke(overlay, original)).size());
        assertEquals(1, calls.get());
        rebuild.setBoolean(overlay, true);
        assertEquals(4, ((List<?>) wrap.invoke(overlay, original)).size());
        assertEquals(2, calls.get());
    }
}
