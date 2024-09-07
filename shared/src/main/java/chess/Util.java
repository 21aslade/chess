package chess;

public class Util {
    public record IntPair(int a, int b) {
        IntPair mult(int n) {
            return new IntPair(this.a * n, this.b * n);
        }
    }
}
