class Problem {
    public static void main(String[] args) {
        final String defaultValue = "default";
        String mode = defaultValue;
        for (int i = 0; i < args.length; i += 2) {
            String parameterName = args[i];
            String parameterValue = args[i + 1];
            if(parameterName.equals("mode") && mode.equals(defaultValue)) {
                mode = parameterValue;
            }
        }
        System.out.println(mode);
    }
}