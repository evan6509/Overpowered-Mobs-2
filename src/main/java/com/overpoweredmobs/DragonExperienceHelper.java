package com.overpoweredmobs;

public final class DragonExperienceHelper {
    private DragonExperienceHelper() {}

    public static int scaleTotal(int vanillaExperience, double multiplier) {
        if (vanillaExperience <= 0) return 0;
        return Math.max(0, (int) Math.ceil(vanillaExperience * multiplier));
    }

    public static int burstExperience(int totalExperience) {
        // Vanilla awards ten 8% bursts. Use integer arithmetic to avoid float rounding.
        return (int) (totalExperience * 8L / 100L);
    }

    public static int finalExperience(int totalExperience) {
        // Include the splitting remainder so the complete death awards the scaled total.
        return totalExperience - 10 * burstExperience(totalExperience);
    }
}
