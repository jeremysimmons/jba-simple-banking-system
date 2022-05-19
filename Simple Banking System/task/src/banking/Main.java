package banking;

import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class Main {
    public static final int BALANCE = 1;
    public static final int ADD_INCOME = 2;
    public static final int TRANSFER = 3;
    public static final int CLOSE_ACCOUNT = 4;
    public static final int LOG_OUT = 5;
    final String BIN = "400000";
    Random random = new Random();
    private String fileName;
    private Scanner scanner;

    private Account currentAccount;

    public static void main(String[] args) {
//        System.out.println("Arguments: " + Arrays.toString(args));
        String cwd = System.getProperty("user.dir");
        System.out.println("Current dir: " + cwd);
        if (args.length == 2) {
            String fileName = args[1];
//            System.out.println("fileName: " + fileName);
//            String dbPath = Paths.get(cwd, fileName).toString();
//            System.out.println("Database:" + dbPath);
            new Main().run(fileName);
            return;
        }
        String dbPath = Paths.get(cwd, "card.s3db").toString();
        new Main().run(dbPath);
    }

    private void run(String fileName) {
        this.fileName = fileName;

        Connection conn = connect();
        closeConnection(conn);

        scanner = new Scanner(System.in);
        int selection = -1;
        do {
            printMenu();
            selection = scanner.nextInt();
            if (isLoggedIn()) {
                switch (selection) {
                    case BALANCE:
                        displayBalance();
                        break;
                    case ADD_INCOME:
                        addIncome();
                        break;
                    case CLOSE_ACCOUNT:
                        closeAccount();
                        break;
                    case TRANSFER:
                        doTransfer();
                        break;
                    case LOG_OUT:
                        logoutOfAccount();
                        break;
                }
            } else {
                if (selection == 1) {
                    createAccount();
                }
                if (selection == 2) {
                    loginToAccount();
                }
            }
        } while (selection != 0);
        System.out.println("Bye!");
    }

    private boolean isLoggedIn() {
        return currentAccount != null;
    }

    private void printMenu() {
        System.out.println();
        if (!isLoggedIn()) {
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
        } else {
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
        }
        System.out.println("0. Exit");
    }

    private void displayBalance() {
        int balance = currentAccount.getBalance();
        System.out.format("Balance: %d%n", balance);
    }

    private void addIncome() {
        // Add income item should allow us to deposit money to the account.
        System.out.println("Enter income:");
        int income = scanner.nextInt();
        currentAccount.deposit(income);
        System.out.println("Income was added!");
        updateAccount(currentAccount);
    }

    private void doTransfer() {
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        String targetAccountNumber = scanner.next();
        if (!luhnCheck(targetAccountNumber)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            return;
        }
        Optional<Account> target = getCardAccount(targetAccountNumber);
        if (target.isEmpty()) {
            System.out.println("Such a card does not exist.");
            return;
        }

        if (targetAccountNumber.equals(currentAccount.getAccountNumber())) {
            System.out.println("You can't transfer money to the same account!");
            return;
        }

        System.out.println("Enter how much money you want to transfer:");
        int amount = scanner.nextInt();
        if (currentAccount.getBalance() <= amount) {
            System.out.println("Not enough money!");
            return;
        }

        var targetAccount = target.get();
        currentAccount.withdraw(amount);
        targetAccount.deposit(amount);
        updateAccount(currentAccount);
        updateAccount(targetAccount);
    }

    private void closeAccount() {
        // If the user chooses the Close an account item, you should delete that account from the database.
        deleteAccount();
    }

    private void logoutOfAccount() {
        System.out.println("You have successfully logged out!");
        currentAccount = null;
    }

    private void loginToAccount() {
        System.out.println("Enter your card number:");
        String cardNumber = scanner.next();
        System.out.println("Enter your PIN:");
        String pin = scanner.next();
        if (accountExists(cardNumber)) {
            var account = getCardAccount(cardNumber).get();
            if (pin.equals(account.getPin())) {
                System.out.println("You have successfully logged in!");
                this.currentAccount = account;
            } else {
                System.out.println("Wrong card number or PIN!");
            }
        } else {
            System.out.println("Wrong card number or PIN!");
        }
    }

    private Connection connect() {
        try {
            String url = String.format("jdbc:sqlite:%s", this.fileName);
            Connection conn = DriverManager.getConnection(url);
            Statement statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS card (\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "number TEXT NOT NULL,\n" +
                    "pin TEXT NOT NULL,\n" +
                    "balance INTEGER DEFAULT 0)");
            return conn;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void createAccount() {
        String cardNumber = null;
        do {
            final String account = String.format("%09d", random.nextInt(999_999_999));
            final String checkDigit = calculateCheckDigit(BIN + account);
            try {
                cardNumber = BIN + account + checkDigit;
                assert checkDigit.length() == 1;
                assert cardNumber.length() == 16;
            } catch (AssertionError e) {
                System.out.println();
                System.out.format("BIN: %s%n", BIN);
                System.out.format("account: %s%n", account);
                System.out.format("checksum: %s%n", checkDigit);
                System.out.format("cardNumber: %s%n", cardNumber);
                System.out.println();
            }
        } while (accountExists(cardNumber));
        final String pin = String.format("%04d", random.nextInt(9999));
        addAccount(cardNumber, pin, 0);
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(cardNumber);
        System.out.format("Card Length:%d%n", cardNumber.length());
        System.out.println("Your card PIN:");
        System.out.println(pin);
    }

    private boolean accountExists(String cardNumber) {
        return getCardAccount(cardNumber).isPresent();
    }

    private void addAccount(String cardNumber, String pin, int balance) {

        String sql = "INSERT INTO card(number,pin,balance) VALUES(?,?,?)";
        Connection conn = this.connect();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            stmt.setString(2, pin);
            stmt.setInt(3, balance);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("addAccount");
            System.out.println(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    private Optional<Account> getCardAccount(String cardNumber) {

        Connection conn = this.connect();
        String sql = "SELECT pin, balance FROM card where number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            ResultSet result = stmt.executeQuery();
            if (result.isBeforeFirst()) {
                result.next();
                var pin = result.getString(1);
                var balance = result.getInt(2);
                return Optional.of(new Account(cardNumber, pin, balance));
            }
        } catch (SQLException e) {
            System.out.println("Main.getCardAccount failed");
            System.out.println(e.getMessage());
        } finally {
            closeConnection(conn);
        }
        return Optional.empty();
    }

    private void updateAccount(Account account) {

        String sql = "UPDATE card set balance = ? where number = ?";
        Connection conn = this.connect();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, account.getBalance());
            stmt.setString(2, account.getAccountNumber());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Main.updateAccount failed");
            System.out.println(e.getMessage());
        }
    }

    private void deleteAccount() {

        var cardNumber = currentAccount.getAccountNumber();
        String sql = "DELETE FROM card where number = ?";
        Connection conn = this.connect();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("addAccount");
            System.out.println(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    public static boolean luhnCheck(String card) {
        if (card == null)
            return false;
        char checkDigit = card.charAt(card.length() - 1);
        String digit = calculateCheckDigit(card.substring(0, card.length() - 1));
        return checkDigit == digit.charAt(0);
    }

    public static String calculateCheckDigit(String card) {
        if (card == null)
            return null;
        String digit;
        /* convert to array of int for simplicity */
        int[] digits = new int[card.length()];
        for (int i = 0; i < card.length(); i++) {
            digits[i] = Character.getNumericValue(card.charAt(i));
        }

        /* double every other starting from right - jumping from 2 in 2 */
        for (int i = digits.length - 1; i >= 0; i -= 2) {
            digits[i] += digits[i];

            /* taking the sum of digits grater than 10 - simple trick by substract 9 */
            if (digits[i] >= 10) {
                digits[i] = digits[i] - 9;
            }
        }
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i];
        }
        /* multiply by 9 step */
        sum = sum * 9;

        /* convert to string to be easier to take the last digit */
        digit = sum + "";
        return digit.substring(digit.length() - 1);
    }

}