import java.util.Arrays;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        int input_rows = scanner.nextInt();
        int input_cols = scanner.nextInt();
        int[][] input = new int[input_rows][input_cols];
        int[][] output = new int[input_cols][input_rows];
        for (int r = 0; r < input_rows; r++) {
            for (int c = 0; c < input_cols; c++) {
                input[r][c] = scanner.nextInt();
            }
        }
        for (int i = 0; i < input_rows; i++) {
            for (int j = 0; j < input_cols; j++) {
                var oRow = j;
                var oCol = input_rows - i - 1;
//                System.out.format("[%d,%d] = %d%n", oRow, oCol, input[i][j]);
                output[oRow][oCol] = input[i][j];
            }
        }
        for (int i = 0; i < input_cols; i++) {
            System.out.println(Arrays.toString(output[i]).replaceAll("[\\[\\],]", ""));
        }
    }
}