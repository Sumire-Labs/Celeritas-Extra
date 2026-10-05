package com.sumirelabs.celeritasextra.client;

/** Geometry shared by sign/map visibility and configurable rendering distances. */
public final class VisibilityRules {
    private VisibilityRules() {}

    public static boolean behindHorizontalFace(double cameraX, double cameraZ, double centerX,
                                               double centerZ, double normalX, double normalZ, double margin) {
        return (cameraX - centerX) * normalX + (cameraZ - centerZ) * normalZ < -margin;
    }

    public static boolean beyondDistance(double distanceSquared, int limit) {
        return limit > 0 && distanceSquared > (double) limit * limit;
    }

    public static boolean exempt(String className, String[] exemptions) {
        for (String exemption : exemptions) {
            if (className.equals(exemption) || (exemption.endsWith(".*")
                    && className.startsWith(exemption.substring(0, exemption.length() - 1)))) return true;
        }
        return false;
    }
}
