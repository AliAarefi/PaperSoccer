package Simulator;

public class Environment {
    private int board[][];
    private int ballPosition[];

    public Environment(int x, int y) {
        if ((x | y) % 2 == 1) return;
        board = new int[x * y][x * y];
        ballPosition = new int[]{x / 2, y / 2};
    }
}
