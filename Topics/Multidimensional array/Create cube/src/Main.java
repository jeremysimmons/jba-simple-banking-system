class ArrayOperations {
    public static int[][][] createCube() {
        // write your code here
        var cube = new int[3][3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                var value = j * 3;
                cube[i][j] = new int[] { value + 0, value + 1, value + 2 };
            }
        }
        return cube;
    }
}