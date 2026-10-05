package com.sumirelabs.celeritasextra.client.render.cloud;

import com.sumirelabs.celeritasextra.CeleritasExtraMod;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Supplies the acquired cloud pattern above other resource packs while Modern Clouds is enabled. */
public final class ModernCloudAssets implements IResourcePack {

    public static final ResourceLocation CLOUD_TEXTURE =
            new ResourceLocation("celeritasextra", "textures/environment/clouds_1_21_6.png");
    private static final ResourceLocation VANILLA_CLOUD_TEXTURE =
            new ResourceLocation("minecraft", "textures/environment/clouds.png");

    private static boolean available;
    private final byte[] texture;

    private ModernCloudAssets(byte[] texture) {
        this.texture = texture;
    }

    public static boolean isAvailable() {
        return available;
    }

    /**
     * Called before resource reload, while the incoming packs are accessible directly. Reading
     * through Minecraft's resource manager here would consult stale (or not yet loaded) packs.
     * Minecraft orders packs from lowest to highest priority, with vanilla first.
     */
    public static List<IResourcePack> withCloudTexture(List<IResourcePack> packs, boolean enabled) {
        available = false;
        for (int i = packs.size() - 1; i >= 0; i--) {
            IResourcePack source = packs.get(i);
            // Follow Minecraft's pack initialization order. AssetMover waits for pending
            // downloads in getResourceDomains(); probing files first races that download.
            if (!source.getResourceDomains().contains("celeritasextra")
                    || !source.resourceExists(CLOUD_TEXTURE)) {
                continue;
            }
            try (InputStream stream = source.getInputStream(CLOUD_TEXTURE)) {
                byte[] texture = stream.readAllBytes();
                available = true;
                if (!enabled) {
                    return packs;
                }
                List<IResourcePack> result = new ArrayList<>(packs);
                // The explicit Modern Clouds choice takes precedence over other cloud textures.
                // This pack exposes only clouds.png, so all other resources keep their ordering.
                result.add(new ModernCloudAssets(texture));
                return result;
            } catch (IOException exception) {
                CeleritasExtraMod.LOGGER.warn("Could not load the modern cloud texture", exception);
                return packs;
            }
        }
        return packs;
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        if (!resourceExists(location)) {
            throw new FileNotFoundException(location.toString());
        }
        return new ByteArrayInputStream(texture);
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        return VANILLA_CLOUD_TEXTURE.equals(location);
    }

    @Override
    public Set<String> getResourceDomains() {
        return Set.of("minecraft");
    }

    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer serializer, String section) {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        throw new FileNotFoundException("pack.png");
    }

    @Override
    public String getPackName() {
        return "Celeritas Extra Modern Cloud Texture";
    }
}
