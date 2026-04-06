package jp.s12kuma01.celeritasextra.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.Random;

/**
 * Custom star renderer with configurable star count.
 * Ported from Angelica's NotFine RenderStars.
 * Uses the same algorithm as vanilla but with a configurable star count.
 */
public class RenderStars {

    /**
     * Generate star vertex data into the tessellator.
     * Called from the mixin that overrides RenderGlobal.generateStars().
     * Uses the same seed (10842L) and algorithm as vanilla for deterministic rendering.
     */
    public static void generateStars(int totalStars) {
        if (totalStars <= 0) return;

        Random random = new Random(10842L);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION);

        for (int i = 0; i < totalStars; ++i) {
            double x = random.nextFloat() * 2.0F - 1.0F;
            double y = random.nextFloat() * 2.0F - 1.0F;
            double z = random.nextFloat() * 2.0F - 1.0F;
            double size = 0.15F + random.nextFloat() * 0.1F;
            double distSq = x * x + y * y + z * z;

            if (distSq < 1.0D && distSq > 0.01D) {
                distSq = 1.0D / Math.sqrt(distSq);
                x *= distSq;
                y *= distSq;
                z *= distSq;

                double starX = x * 100.0D;
                double starY = y * 100.0D;
                double starZ = z * 100.0D;

                double azimuth = Math.atan2(x, z);
                double azSin = Math.sin(azimuth);
                double azCos = Math.cos(azimuth);

                double polar = Math.atan2(Math.sqrt(x * x + z * z), y);
                double polSin = Math.sin(polar);
                double polCos = Math.cos(polar);

                double rotation = random.nextDouble() * Math.PI * 2.0D;
                double rotSin = Math.sin(rotation);
                double rotCos = Math.cos(rotation);

                for (int corner = 0; corner < 4; ++corner) {
                    double u = ((corner & 2) - 1) * size;
                    double v = ((corner + 1 & 2) - 1) * size;
                    double vertical = u * rotCos - v * rotSin;
                    double horizontal = v * rotCos + u * rotSin;
                    double cornerY = vertical * polSin;
                    double offsetAz = -vertical * polCos;
                    double cornerX = offsetAz * azSin - horizontal * azCos;
                    double cornerZ = horizontal * azSin + offsetAz * azCos;
                    buffer.pos(starX + cornerX, starY + cornerY, starZ + cornerZ).endVertex();
                }
            }
        }

        tessellator.draw();
    }
}
