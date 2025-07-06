package tgw.wolf_tweaks.util;

public final class Converter {

    private Converter() {
    }

    public static double genericSpeedToBlockPerSec(double speed) {
        return 42.157_796 * speed;
    }

    public static double jumpStrengthToJumpHeight(double strength) {
        double height = 0;
        double velocity = strength;
        while (velocity > 0) {
            height += velocity;
            velocity = (velocity - 0.08) * 0.98 * 0.98;
        }
        return height;
    }
}
