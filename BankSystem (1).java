import java.io.*;
import java.util.*;

public class BankSystem {
    private static final String DATA_FILE = "accounts.txt";
    private static Map<Integer, BankAccount> accounts = new HashMap<>();
    private static int nextAccountNumber = 1000;

    public static void main(String[] args) {
        loadAccounts();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("Welcome to the Bank System!");

        while (running) {
            System.out.println("\n1. Create Account");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. View Account Details");
            System.out.println("5. Exit");
            System.out.print("Select an option: ");

            int choice = getIntInput(scanner);

            switch (choice) {
                case 1:
                    createAccount(scanner);
                    break;
                case 2:
                    depositMoney(scanner);
                    break;
                case 3:
                    withdrawMoney(scanner);
                    break;
                case 4:
                    viewAccountDetails(scanner);
                    break;
                case 5:
                    saveAccounts();
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please select again.");
            }
        }

        scanner.close();
    }

    private static void createAccount(Scanner scanner) {
        System.out.print("Enter account holder name: ");
        scanner.nextLine(); // consume leftover newline
        String name = scanner.nextLine();

        System.out.print("Enter account type (Checking/Savings): ");
        String type = scanner.nextLine();

        System.out.print("Enter opening balance: ");
        double balance = getDoubleInput(scanner);

        int accountNumber = nextAccountNumber++;
        BankAccount account = new BankAccount(accountNumber, name, type, balance);
        accounts.put(accountNumber, account);

        System.out.println("Account created successfully! Account Number: " + accountNumber);
    }

    private static void depositMoney(Scanner scanner) {
        System.out.print("Enter account number: ");
        int accountNumber = getIntInput(scanner);

        BankAccount account = accounts.get(accountNumber);
        if (account != null) {
            System.out.print("Enter amount to deposit: ");
            double amount = getDoubleInput(scanner);
            account.deposit(amount);
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void withdrawMoney(Scanner scanner) {
        System.out.print("Enter account number: ");
        int accountNumber = getIntInput(scanner);

        BankAccount account = accounts.get(accountNumber);
        if (account != null) {
            System.out.print("Enter amount to withdraw: ");
            double amount = getDoubleInput(scanner);
            account.withdraw(amount);
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void viewAccountDetails(Scanner scanner) {
        System.out.print("Enter account number: ");
        int accountNumber = getIntInput(scanner);

        BankAccount account = accounts.get(accountNumber);
        if (account != null) {
            account.printDetails();
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void loadAccounts() {
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                BankAccount account = BankAccount.fromString(line);
                accounts.put(account.getAccountNumber(), account);
                nextAccountNumber = Math.max(nextAccountNumber, account.getAccountNumber() + 1);
            }
            System.out.println("Accounts loaded successfully.");
        } catch (IOException e) {
            System.out.println("No saved accounts found. Starting fresh.");
        }
    }

    private static void saveAccounts() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (BankAccount account : accounts.values()) {
                writer.println(account.toString());
            }
            System.out.println("Accounts saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving accounts: " + e.getMessage());
        }
    }

    private static int getIntInput(Scanner scanner) {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Enter a number: ");
                scanner.next(); // clear invalid input
            }
        }
    }

    private static double getDoubleInput(Scanner scanner) {
        while (true) {
            try {
                return scanner.nextDouble();
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Enter a valid amount: ");
                scanner.next(); // clear invalid input
            }
        }
    }
}

class BankAccount {
    private int accountNumber;
    private String holderName;
    private String accountType;
    private double balance;
    private List<String> transactions;

    public BankAccount(int accountNumber, String holderName, String accountType, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.accountType = accountType;
        this.balance = balance;
        this.transactions = new ArrayList<>();
        addTransaction("Account created with balance $" + String.format("%.2f", balance));
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Deposit amount must be positive.");
        } else {
            balance += amount;
            addTransaction("Deposited $" + String.format("%.2f", amount));
            System.out.println("Deposited successfully.");
        }
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
        } else if (amount > balance) {
            System.out.println("Insufficient funds.");
        } else {
            balance -= amount;
            addTransaction("Withdrew $" + String.format("%.2f", amount));
            System.out.println("Withdrawal successful.");
        }
    }

    public void printDetails() {
        System.out.println("\n=== Account Details ===");
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Holder Name: " + holderName);
        System.out.println("Account Type: " + accountType);
        System.out.printf("Balance: $%.2f%n", balance);
        System.out.println("Transaction History:");
        for (String transaction : transactions) {
            System.out.println("- " + transaction);
        }
        System.out.println("=========================");
    }

    private void addTransaction(String message) {
        transactions.add(new Date() + ": " + message);
    }

    // Save object to a line in file
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(accountNumber).append(",");
        sb.append(holderName).append(",");
        sb.append(accountType).append(",");
        sb.append(balance).append(",");
        for (String transaction : transactions) {
            sb.append(transaction.replace(",", ";")).append("|"); // replace commas in transactions
        }
        return sb.toString();
    }

    // Load object from a line
    public static BankAccount fromString(String line) {
        String[] parts = line.split(",", 5);
        int accNo = Integer.parseInt(parts[0]);
        String name = parts[1];
        String type = parts[2];
        double balance = Double.parseDouble(parts[3]);
        BankAccount account = new BankAccount(accNo, name, type, balance);

        if (parts.length == 5) {
            String[] trans = parts[4].split("\\|");
            account.transactions.clear();
            for (String t : trans) {
                if (!t.isEmpty()) {
                    account.transactions.add(t.replace(";", ","));
                }
            }
        }
        return account;
    }
}
