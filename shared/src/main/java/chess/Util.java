package chess;

public class Util {
    public record IntPair(int a, int b) {
        public static final IntPair Up = new IntPair(1, 0);
        public static final IntPair Down = new IntPair(-1, 0);
        public static final IntPair Left = new IntPair(0, -1);
        public static final IntPair Right = new IntPair(0, 1);

        public static final IntPair UpLeft = Up.add(Left);
        public static final IntPair UpRight = Up.add(Right);
        public static final IntPair DownLeft = Down.add(Left);
        public static final IntPair DownRight = Down.add(Right);

        IntPair add(IntPair other) {
            return new IntPair(this.a + other.a, this.b + other.b);
        }

        IntPair mult(int n) {
            return new IntPair(this.a * n, this.b * n);
        }
    }
}
