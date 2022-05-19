import java.util.Arrays;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        int input_rows = scanner.nextInt();
        int input_cols = scanner.nextInt();
        boolean[][] input = new boolean[input_rows][input_cols];
        for (int r = 0; r < input_rows; r++) {
            for (int c = 0; c < input_cols; c++) {
                input[r][c] = scanner.nextInt() == 1;
            }
        }
        int neededSeets = scanner.nextInt();
        for (int i = 0; i < input_rows; i++) {
            int seatsInRow = 0;
            for (int j = 0; j < input_cols; j++) {
                boolean sold = input[i][j];
                boolean available = !sold;
                if (available) {
                    seatsInRow++;
                }
                if (seatsInRow == neededSeets) {
                    System.out.println(i + 1);
                    System.exit(0);
                }
                if (sold) {
                    seatsInRow = 0;
                }
            }
        }
        System.out.println(0);
    }
}