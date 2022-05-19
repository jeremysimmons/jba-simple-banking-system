import java.util.Scanner;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int[] arr = {scanner.nextInt(), scanner.nextInt(), scanner.nextInt()};
        var sum = Arrays.stream(arr).sum();
        // write your code here
        System.out.println(sum);
    }
}