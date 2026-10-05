package jp.s12kuma01.celeritasextra.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector3f;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemFrameLodStateTest {
    private final List<BakedQuad> quads = Arrays.stream(EnumFacing.values())
            .map(face -> new BakedQuad(new int[28], -1, face, null, true,
                    net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM)).toList();

    @AfterEach
    void clearState() {
        ItemFrameLodState.active = false;
    }

    @Test
    void generatedItemGeneralBucketRetainsOnlyFrontFace() {
        ItemFrameLodState.active = true;
        List<BakedQuad> result = filter(false, ItemTransformVec3f.DEFAULT);
        assertEquals(List.of(EnumFacing.SOUTH), result.stream().map(BakedQuad::getFace).toList());
        assertEquals(6, quads.size(), "Shared baked geometry must not be mutated");
    }

    @Test
    void blocksAndDisabledLodKeepOriginalGeometry() {
        assertSame(quads, filter(false, ItemTransformVec3f.DEFAULT));
        ItemFrameLodState.active = true;
        assertSame(quads, filter(true, ItemTransformVec3f.DEFAULT));
    }

    @Test
    void translatedItemKeepsBothDepthFacesAndOffAxisItemKeepsEverything() {
        ItemFrameLodState.active = true;
        ItemTransformVec3f translated = new ItemTransformVec3f(new Vector3f(), new Vector3f(0, 0, 0.2f), new Vector3f(1, 1, 1));
        assertEquals(List.of(EnumFacing.NORTH, EnumFacing.SOUTH), filter(false, translated).stream().map(BakedQuad::getFace).toList());
        ItemTransformVec3f rotated = new ItemTransformVec3f(new Vector3f(0, 45, 0), new Vector3f(), new Vector3f(1, 1, 1));
        assertSame(quads, filter(false, rotated));
    }

    @Test
    void halfTurnSelectsTheOppositeModelFace() {
        ItemFrameLodState.active = true;
        ItemTransformVec3f rotated = new ItemTransformVec3f(new Vector3f(0, 180, 0), new Vector3f(), new Vector3f(1, 1, 1));
        assertEquals(List.of(EnumFacing.NORTH), filter(false, rotated).stream().map(BakedQuad::getFace).toList());
    }

    private List<BakedQuad> filter(boolean gui3d, ItemTransformVec3f fixed) {
        IBakedModel model = new IBakedModel() {
            public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) { return quads; }
            public boolean isAmbientOcclusion() { return false; }
            public boolean isGui3d() { return gui3d; }
            public boolean isBuiltInRenderer() { return false; }
            public TextureAtlasSprite getParticleTexture() { return null; }
            public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
            public ItemCameraTransforms getItemCameraTransforms() {
                ItemTransformVec3f none = ItemTransformVec3f.DEFAULT;
                return new ItemCameraTransforms(none, none, none, none, none, none, none, fixed);
            }
        };
        return ItemFrameLodState.filterLodQuads(model, null, null, 0);
    }
}
