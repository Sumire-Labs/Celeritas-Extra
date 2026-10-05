package com.sumire.celeritasextra.client.render.cloud;

import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModernCloudAssetsTest {
    private static final ResourceLocation CLOUD = new ResourceLocation("minecraft", "textures/environment/clouds.png");
    private static final IResourcePack VANILLA = new TestPack(Map.of(CLOUD, new byte[]{1}));
    private static final IResourcePack ACQUIRED = new TestPack(Map.of(ModernCloudAssets.CLOUD_TEXTURE, new byte[]{2}));

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void waitsForPackInitializationBeforeCheckingTheDownloadedTexture(boolean enabled) throws IOException {
        IResourcePack delayed = new TestPack(Map.of(ModernCloudAssets.CLOUD_TEXTURE, new byte[]{2})) {
            private boolean ready;

            @Override
            public Set<String> getResourceDomains() {
                // AssetMover's InternalResourcePack waits for pending downloads at this point.
                ready = true;
                return super.getResourceDomains();
            }

            @Override
            public boolean resourceExists(ResourceLocation location) {
                return ready && super.resourceExists(location);
            }
        };

        List<IResourcePack> result = ModernCloudAssets.withCloudTexture(List.of(VANILLA, delayed), enabled);

        assertTrue(ModernCloudAssets.isAvailable(), "First reload must unlock the option even when it is off");
        assertArrayEquals(new byte[]{enabled ? (byte) 2 : (byte) 1}, readCloud(result));
    }

    @Test
    void exposesAcquiredTextureAtStandardPathWithoutChangingCallerList() throws IOException {
        List<IResourcePack> original = List.of(VANILLA, ACQUIRED);
        List<IResourcePack> result = ModernCloudAssets.withCloudTexture(original, true);

        assertArrayEquals(new byte[]{2}, readCloud(result));
        assertEquals(2, original.size());
        assertTrue(ModernCloudAssets.isAvailable());
        assertFalse(result.getLast().resourceExists(ModernCloudAssets.CLOUD_TEXTURE));
        assertFalse(result.getLast().resourceExists(new ResourceLocation("textures/environment/clouds.png.mcmeta")));
    }

    @Test
    void enabledModernCloudsOverrideUserAndServerCloudsAndDisablingRestoresThem() throws IOException {
        IResourcePack user = new TestPack(Map.of(CLOUD, new byte[]{3}));
        IResourcePack server = new TestPack(Map.of(CLOUD, new byte[]{4}));

        assertArrayEquals(new byte[]{2}, readCloud(ModernCloudAssets.withCloudTexture(
                List.of(VANILLA, ACQUIRED, user), true)));
        assertArrayEquals(new byte[]{2}, readCloud(ModernCloudAssets.withCloudTexture(
                List.of(VANILLA, ACQUIRED, user, server), true)));
        assertArrayEquals(new byte[]{3}, readCloud(ModernCloudAssets.withCloudTexture(
                List.of(VANILLA, ACQUIRED, user), false)));
        assertArrayEquals(new byte[]{4}, readCloud(ModernCloudAssets.withCloudTexture(
                List.of(VANILLA, ACQUIRED, user, server), false)));
    }

    @Test
    void forcingModernCloudsDoesNotOverrideOtherUserTextures() throws IOException {
        var block = new ResourceLocation("minecraft", "textures/blocks/stone.png");
        IResourcePack user = new TestPack(Map.of(CLOUD, new byte[]{3}, block, new byte[]{6}));
        var packs = ModernCloudAssets.withCloudTexture(List.of(VANILLA, ACQUIRED, user), true);
        FallbackResourceManager manager = new FallbackResourceManager(new MetadataSerializer());
        packs.forEach(manager::addResourcePack);
        try (IResource resource = manager.getResource(block)) {
            assertArrayEquals(new byte[]{6}, resource.getInputStream().readAllBytes());
        }
        assertArrayEquals(new byte[]{2}, readCloud(packs));
    }

    @Test
    void disablingRestoresVanillaAndLeavesTheOptionAvailable() throws IOException {
        List<IResourcePack> original = List.of(VANILLA, ACQUIRED);
        ModernCloudAssets.withCloudTexture(original, true);
        List<IResourcePack> result = ModernCloudAssets.withCloudTexture(original, false);

        assertSame(original, result);
        assertArrayEquals(new byte[]{1}, readCloud(result));
        assertTrue(ModernCloudAssets.isAvailable());
    }

    @Test
    void missingOrRemovedAssetRestoresVanillaAndDisablesTheOption() throws IOException {
        ModernCloudAssets.withCloudTexture(List.of(VANILLA, ACQUIRED), true);
        List<IResourcePack> original = List.of(VANILLA);

        assertSame(original, ModernCloudAssets.withCloudTexture(original, true));
        assertArrayEquals(new byte[]{1}, readCloud(original));
        assertFalse(ModernCloudAssets.isAvailable());
        assertTrue(ModernCloudAssets.withCloudTexture(List.of(), true).isEmpty());
    }

    @Test
    void highestPrioritySourceIsUsedOnEveryReload() throws IOException {
        IResourcePack replacement = new TestPack(Map.of(ModernCloudAssets.CLOUD_TEXTURE, new byte[]{5}));
        assertArrayEquals(new byte[]{5}, readCloud(ModernCloudAssets.withCloudTexture(
                List.of(VANILLA, ACQUIRED, replacement), true)));
        assertArrayEquals(new byte[]{2}, readCloud(ModernCloudAssets.withCloudTexture(
                List.of(VANILLA, ACQUIRED), true)));
    }

    @Test
    void unreadableAssetLeavesVanillaAvailable() throws IOException {
        IResourcePack broken = new TestPack(Map.of(ModernCloudAssets.CLOUD_TEXTURE, new byte[0])) {
            @Override
            public InputStream getInputStream(ResourceLocation location) throws IOException {
                throw new IOException("Download is unavailable");
            }
        };
        List<IResourcePack> original = List.of(VANILLA, broken);

        assertSame(original, ModernCloudAssets.withCloudTexture(original, true));
        assertArrayEquals(new byte[]{1}, readCloud(original));
        assertFalse(ModernCloudAssets.isAvailable());
    }

    private static byte[] readCloud(List<IResourcePack> packs) throws IOException {
        FallbackResourceManager manager = new FallbackResourceManager(new MetadataSerializer());
        packs.forEach(manager::addResourcePack);
        try (IResource resource = manager.getResource(CLOUD)) {
            return resource.getInputStream().readAllBytes();
        }
    }

    private static class TestPack implements IResourcePack {
        private final Map<ResourceLocation, byte[]> resources;

        private TestPack(Map<ResourceLocation, byte[]> resources) {
            this.resources = resources;
        }

        @Override
        public InputStream getInputStream(ResourceLocation location) throws IOException {
            if (!resourceExists(location)) throw new FileNotFoundException(location.toString());
            return new ByteArrayInputStream(resources.get(location));
        }

        @Override
        public boolean resourceExists(ResourceLocation location) { return resources.containsKey(location); }

        @Override
        public Set<String> getResourceDomains() { return Set.of("minecraft", "celeritasextra"); }

        @Override
        public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer serializer, String section) { return null; }

        @Override
        public BufferedImage getPackImage() { return null; }

        @Override
        public String getPackName() { return "Test pack"; }
    }
}
