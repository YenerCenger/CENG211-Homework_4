package boxes;

import grid.Direction;

public final class BoxRotations {
    private BoxRotations() {}

    public static void applyRoll(char[] faces, Direction dir) {
        int T = Face.TOP.ordinal();
        int B = Face.BOTTOM.ordinal();
        int L = Face.LEFT.ordinal();
        int R = Face.RIGHT.ordinal();
        int F = Face.FRONT.ordinal();
        int K = Face.BACK.ordinal();

        char tmp;
        switch (dir) {
            case LEFT:
                tmp = faces[T];
                faces[T] = faces[R];
                faces[R] = faces[B];
                faces[B] = faces[L];
                faces[L] = tmp;
                break;
            case RIGHT:
                tmp = faces[T];
                faces[T] = faces[L];
                faces[L] = faces[B];
                faces[B] = faces[R];
                faces[R] = tmp;
                break;
            case UP:
                tmp = faces[T];
                faces[T] = faces[F];
                faces[F] = faces[B];
                faces[B] = faces[K];
                faces[K] = tmp;
                break;
            case DOWN:
                tmp = faces[T];
                faces[T] = faces[K];
                faces[K] = faces[B];
                faces[B] = faces[F];
                faces[F] = tmp;
                break;
        }
    }
}
