class Problem {
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            var arg = args[i];
            if(arg.indexOf("test") > -1) {
                System.out.println(i);
                return;
            }
        }
        System.out.println(-1);

    }
}