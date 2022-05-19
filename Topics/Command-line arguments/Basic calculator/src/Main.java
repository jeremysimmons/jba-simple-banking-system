class Problem {
    public static void main(String[] args) {
        if(args.length < 3) {
            System.exit(0);
        }
        String operator = args[0];
        Integer left = Integer.parseInt(args[1]);
        Integer right = Integer.parseInt(args[2]);
        switch (operator) {
            case "+":
                System.out.println(left + right);
                break;
            case "-":
                System.out.println(left - right);
                break;
            case "*":
                System.out.println(left * right);
                break;
            default:
                System.out.println("Unknown operator");
                break;
        }
    }
}