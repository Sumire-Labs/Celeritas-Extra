package jp.s12kuma01.celeritasextra.client.particle;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Drives authoritative particle discovery off the GUI and off the per-particle hot path.
 * <p>
 * In 1.12.2 {@code Minecraft.effectRenderer} is (re)created per world load, and that is when
 * vanilla and most mod particle factories are present. We therefore run the reflection scan once
 * per {@code effectRenderer} instance (detected on the client tick, which is robust to the
 * world-load ordering). Results are persisted as a refreshable cache so previously discovered
 * particles — including lambda/anonymous mod particles, which can only be found at spawn time —
 * appear from launch on later sessions; stale entries from removed mods are pruned on each scan.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ParticleDiscoveryHandler {

    /** The effectRenderer instance most recently scanned; a change means a new world was loaded. */
    private static ParticleManager lastScanned = null;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ParticleManager effectRenderer = Minecraft.getMinecraft().effectRenderer;
        if (effectRenderer == null || effectRenderer == lastScanned) {
            return;
        }
        lastScanned = effectRenderer;

        ParticleClassRegistry registry = ParticleClassRegistry.getInstance();
        registry.pruneDiscoveredCache();
        registry.scanFactories(effectRenderer);
        flushIfDirty(registry);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld() == null || !event.getWorld().isRemote) {
            return;
        }
        // Persist anything changed during this session: classes discovered at spawn time and
        // per-class enable/disable toggles (which mutate the registry directly).
        flushIfDirty(ParticleClassRegistry.getInstance());
    }

    private static void flushIfDirty(ParticleClassRegistry registry) {
        if (registry.isDirty()) {
            // writeChanges() persists the registry state and marks it clean.
            CeleritasExtraClientMod.options().writeChanges();
        }
    }
}
