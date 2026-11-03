package com.sumirelabs.celeritasextra.client;

/** Precomputed normals; one decision is shared by all four lines of each sign render. */
public final class SignTextVisibility {

    private static final double[] STANDING_X = new double[16], STANDING_Z = new double[16];
    private static final double[] WALL_X = new double[16], WALL_Z = new double[16];
    static {
        for (int metadata = 0; metadata < 16; metadata++) {
            double angle = metadata * Math.PI / 8;
            STANDING_X[metadata] = -Math.sin(angle);
            STANDING_Z[metadata] = Math.cos(angle);
            angle = switch (metadata) {
                case 2 -> Math.PI;
                case 4 -> Math.PI / 2;
                case 5 -> -Math.PI / 2;
                default -> 0;
            };
            WALL_X[metadata] = -Math.sin(angle);
            WALL_Z[metadata] = Math.cos(angle);
        }
    }
    private SignTextVisibility() {}

    public static boolean hidden(double cameraX, double cameraZ, double centerX, double centerZ,
        int metadata, boolean standing) {
        int index = metadata & 15;
        return VisibilityRules.behindHorizontalFace(cameraX, cameraZ, centerX, centerZ,
            standing ? STANDING_X[index] : WALL_X[index], standing ? STANDING_Z[index] : WALL_Z[index], 0.5);
    }

}
