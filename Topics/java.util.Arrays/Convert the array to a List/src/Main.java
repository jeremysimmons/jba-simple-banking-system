import java.util.Scanner;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Integer[] arr = {scanner.nextInt(), scanner.nextInt(), scanner.nextInt()};
        // write your code here
        List<Integer> ints = Arrays.asList(arr);
        System.out.println(ints.get(0));
    }
}