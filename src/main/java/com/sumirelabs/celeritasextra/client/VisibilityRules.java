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
                && className.startsWith(exemption.substring(0, exemption.length() - 1))))
                return true;
        }
        return false;
    }

    /** Per-settings cache; replacing the rules or saving in-place edits rebuilds class decisions. */
    public static final class ExemptionCache {

        private volatile State state;

        public boolean matches(Class<?> type, String[] rules) {
            State current = state;
            if (current == null || current.source != rules)
                state = current = new State(rules);
            return current.matches.get(type);
        }

        public void invalidate() {
            state = null;
        }

        private static final class State {

            final String[] source;
            final ClassValue<Boolean> matches;

            State(String[] rules) {
                source = rules;
                String[] snapshot = rules.clone();
                matches = new ClassValue<>() {

                    @Override
                    protected Boolean computeValue(Class<?> type) {
                        return exempt(type.getName(), snapshot);
                    }

                };
            }

        }

    }

}
