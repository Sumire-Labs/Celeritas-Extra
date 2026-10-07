package com.sumirelabs.celeritasextra.compat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;

/** Inspects the GUI API without defining renderer classes during Mixin startup. */
public final class CeleritasGuiCompatibility {
    static final String SCREEN = "org/taumc/celeritas/impl/gui/CeleritasVideoOptionsScreen.class";
    private CeleritasGuiCompatibility() { }

    public static boolean hasControllerGui() {
        try (InputStream input = CeleritasGuiCompatibility.class.getClassLoader().getResourceAsStream(SCREEN)) {
            return input != null && hasControllerGui(input);
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    static boolean hasControllerGui(InputStream input) throws IOException {
        var node = new ClassNode();
        new ClassReader(input).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return node.fields.stream().anyMatch(field -> field.name.equals("controller")
                && field.desc.equals("Lorg/embeddedt/embeddium/impl/gui/CeleritasVideoOptionsController;"));
    }
}
