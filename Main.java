import java.sql.Connection;
import java.util.InputMismatchException;

public class Main {

    static Connection connection = null;

    public static void main(String[] args) {

        boolean programRun = true;
        boolean inputRun;

        while (programRun) {
            System.out.println("Concert Ticket Management System");
            System.out.println("[1] Start Connection");
            System.out.println("[2] Exit Program");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = MyJDBC.getUserInput("Choice: ");

                    switch (choice) {
                        case 1:
                            inputRun = false;
                            connectionsMenu();
                            break;
                        case 2:
                            inputRun = false;
                            programRun = false;
                            System.out.println("Exiting program...");
                            break;
                        default:
                            throw new InputMismatchException("Invalid input.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private static void connectionsMenu() {
        if (connection == null) {
            connection = MyJDBC.setupConnection();
        }

        ConcertRecords records = new ConcertRecords(connection);
        ConcertTransactions transactions = new ConcertTransactions(connection);
        ConcertReports reports = new ConcertReports(connection);

        boolean programRun = true;
        boolean inputRun;

        while (programRun) {
            System.out.println("\n--- Concert Ticket Management System ---");
            System.out.println("[1] Records");
            System.out.println("[2] Transactions");
            System.out.println("[3] Reports");
            System.out.println("[4] Exit");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = MyJDBC.getUserInput("Choice: ");

                    switch (choice) {
                        case 1:
                            inputRun = false;
                            records.recordMenu();
                            break;
                        case 2:
                            inputRun = false;
                            transactions.transactionMenu();
                            break;
                        case 3:
                            inputRun = false;
                            reports.reportMenu();
                            break;
                        case 4:
                            inputRun = false;
                            programRun = false;
                            System.out.println("Exiting to main menu...");
                            break;
                        default:
                            throw new InputMismatchException("Invalid input.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
