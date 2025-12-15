package boxes;

import java.util.Random;

public final class BoxFactory {
    private BoxFactory() {}

    public static char[] randomFaces(Random rng) {
        // Placeholder: TODO enforce "max 2 of same letter" for initial generation.
        char[] f = new char[Face.values().length];
        for (int i = 0; i < f.length; i++) {
            f[i] = (char) ('A' + rng.nextInt(8));
        }
        return f;
    }
}
