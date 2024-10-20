package chess;

public class Util {
    public record IntPair(int a, int b) {
        public static final IntPair UP = new IntPair(1, 0);
        public static final IntPair DOWN = new IntPair(-1, 0);
        public static final IntPair LEFT = new IntPair(0, -1);
        public static final IntPair RIGHT = new IntPair(0, 1);

        public static final IntPair UP_LEFT = UP.add(LEFT);
        public static final IntPair UP_RIGHT = UP.add(RIGHT);
        public static final IntPair DOWN_LEFT = DOWN.add(LEFT);
        public static final IntPair DOWN_RIGHT = DOWN.add(RIGHT);

        IntPair add(IntPair other) {
            return new IntPair(this.a + other.a, this.b + other.b);
        }

        IntPair mult(int n) {
            return new IntPair(this.a * n, this.b * n);
        }
    }
}
