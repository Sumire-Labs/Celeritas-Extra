package com.sumirelabs.celeritasextra.client;

import java.util.List;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;

/** Draws the vanilla ITEM layout from a resident VBO. */
final class StaticItemVbo {

    private static final int STRIDE = 28;
    private static final List<?> ITEM_ELEMENTS = List.of(DefaultVertexFormats.POSITION_3F,
        DefaultVertexFormats.COLOR_4UB, DefaultVertexFormats.TEX_2F, DefaultVertexFormats.NORMAL_3B,
        DefaultVertexFormats.PADDING_1B);

    private final VertexBuffer vertices;
    private final int vao;

    private StaticItemVbo(VertexBuffer vertices, int vao) {
        this.vertices = vertices;
        this.vao = vao;
    }

    static boolean supports(VertexFormat format) {
        return OpenGlHelper.vboSupported && format == DefaultVertexFormats.ITEM && format.getSize() == STRIDE
            && format.getElements().equals(ITEM_ELEMENTS) && GL.getCapabilities().OpenGL30
            && GL.getCapabilities().glPushClientAttrib != 0;
    }

    static StaticItemVbo upload(int[] data) {
        var bytes = MemoryUtil.memAlloc(data.length * Integer.BYTES);
        try {
            bytes.asIntBuffer().put(data);
            var vbo = new VertexBuffer(DefaultVertexFormats.ITEM);
            int previous = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
            int vao = GL30.glGenVertexArrays();
            boolean ready = false;
            GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);
            try {
                if (vao == 0)
                    return null;
                vbo.bufferData(bytes);
                GL30.glBindVertexArray(vao);
                vbo.bindBuffer();
                if (GL15.glGetBufferParameteri(GL15.GL_ARRAY_BUFFER, GL15.GL_BUFFER_SIZE) != bytes.remaining())
                    return null;
                configureArrays();
                ready = true;
                return new StaticItemVbo(vbo, vao);
            } finally {
                GL30.glBindVertexArray(previous);
                GL11.glPopClientAttrib();
                if (!ready) {
                    GL30.glDeleteVertexArrays(vao);
                    vbo.deleteGlBuffers();
                }
            }
        } finally {
            MemoryUtil.memFree(bytes);
        }
    }

    void draw() {
        int previous = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        GL30.glBindVertexArray(vao);
        try {
            vertices.drawArrays(GL11.GL_QUADS);
        } finally {
            GL30.glBindVertexArray(previous);
            GlStateManager.resetColor();
        }
    }

    void delete() {
        GL30.glDeleteVertexArrays(vao);
        vertices.deleteGlBuffers();
    }

    private static void configureArrays() {
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, STRIDE, 0);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, STRIDE, 12);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, STRIDE, 16);
        GL11.glNormalPointer(GL11.GL_BYTE, STRIDE, 24L);
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlStateManager.glEnableClientState(GL11.GL_NORMAL_ARRAY);
    }

}
