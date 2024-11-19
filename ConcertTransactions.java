import java.sql.*;
import java.util.InputMismatchException;

public class ConcertTransactions {

    Connection connection;
    Statement statement;

    public ConcertTransactions(Connection connection) {
        this.connection = connection;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing statement: " + e.getMessage());
        }
    }

    // Handles the transaction menu
    public void transactionMenu() {
        boolean programRun = true;
        boolean inputRun;

        while (programRun) {
            System.out.println("\n--- Transactions Menu ---");
            System.out.println("[1] Ticket Selling");
            System.out.println("[2] Ticket Refunding");
            System.out.println("[3] Ticket Transferring");
            System.out.println("[4] Concert Cancelling");
            System.out.println("[5] Back to Main Menu");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = MyJDBC.getUserInput("Choice: ");

                    switch (choice) {
                        case 1:
                            inputRun = false;
                            sellTickets();
                            break;
                        case 2:
                            inputRun = false;
                            refundTickets();
                            break;
                        case 3:
                            inputRun = false;
                            transferTickets();
                            break;
                        case 4:
                            inputRun = false;
                            cancelConcert();
                            break;
                        case 5:
                            inputRun = false;
                            programRun = false;
                            System.out.println("Returning to main menu...");
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

    private void sellTickets() {
        System.out.println("Ticket Selling functionality is under development.");
        // Implement logic here
    }

    private void refundTickets() {
        System.out.println("Ticket Refunding functionality is under development.");
        // Implement logic here
    }

    private void cancelConcert() {
        System.out.println("Concert Cancelling functionality is under development.");
        // Implement logic here
    }

    // Handles the flow for transferring tickets
    private void transferTickets() {
        try {
            int oldCustomerCode = MyJDBC.getUserInput("Enter current customer's ID: ");
            int newCustomerCode = MyJDBC.getUserInput("Enter new customer's ID: ");
            System.out.println("Enter ticket IDs separated by commas (e.g., 1,2,3): ");
            String ticketInput = MyJDBC.getUserStringInput("Ticket IDs: ");
            String[] ticketStrings = ticketInput.split(",");
            int[] ticketIds = new int[ticketStrings.length];
            for (int i = 0; i < ticketStrings.length; i++) {
                ticketIds[i] = Integer.parseInt(ticketStrings[i].trim());
            }

            transferTickets(oldCustomerCode, newCustomerCode, ticketIds);

        } catch (NumberFormatException e) {
            System.out.println("Invalid ticket IDs. Please enter numbers separated by commas.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    // Handles the actual ticket transfer logic
    public void transferTickets(int oldCustomerCode, int newCustomerCode, int[] ticketCodes) {
        try {
            connection.setAutoCommit(false); // Start transaction

            // Step 1: Read the record of the customer transferring tickets
            String oldCustomerQuery = "SELECT * FROM Customers WHERE customer_code = ?";
            try (PreparedStatement oldCustomerStmt = connection.prepareStatement(oldCustomerQuery)) {
                oldCustomerStmt.setInt(1, oldCustomerCode);
                try (ResultSet oldCustomerRs = oldCustomerStmt.executeQuery()) {
                    if (!oldCustomerRs.next()) {
                        System.out.println("Customer transferring tickets not found.");
                        connection.rollback();
                        return;
                    }
                }
            }

            // Step 2: Read the records of the tickets to be transferred
            String ticketQuery = "SELECT * FROM Tickets WHERE ticket_code = ? AND customer_code = ?";
            for (int ticketCode : ticketCodes) {
                try (PreparedStatement ticketStmt = connection.prepareStatement(ticketQuery)) {
                    ticketStmt.setInt(1, ticketCode);
                    ticketStmt.setInt(2, oldCustomerCode);
                    try (ResultSet ticketRs = ticketStmt.executeQuery()) {
                        if (!ticketRs.next()) {
                            System.out.println("Ticket code " + ticketCode + " not found for this customer.");
                            connection.rollback();
                            return;
                        }
                    }
                }
            }

            // Step 3: Ask for payment method for the transfer
            System.out.println("Enter payment method for the transfer (cash, bank_transfer, card): ");
            String paymentMethod = MyJDBC.getUserStringInput("Payment method: ");

            // Validate payment method
            if (!paymentMethod.equals("cash") && !paymentMethod.equals("bank_transfer") && !paymentMethod.equals("card")) {
                System.out.println("Invalid payment method. Please choose 'cash', 'bank_transfer', or 'card'.");
                connection.rollback();
                return;
            }

            // Step 4: Record the transaction with a fixed transfer fee of 100 pesos
            String insertTransactionQuery = """
                    INSERT INTO Transactions (customer_code, transaction_type, transaction_status, transaction_date, total_amount, payment_method)
                    VALUES (?, 'transfer', 'closed', CURRENT_TIMESTAMP, 100.00, ?)
                    """;
            try (PreparedStatement transactionStmt = connection.prepareStatement(insertTransactionQuery, Statement.RETURN_GENERATED_KEYS)) {
                transactionStmt.setInt(1, oldCustomerCode);
                transactionStmt.setString(2, paymentMethod);
                transactionStmt.executeUpdate();
                try (ResultSet generatedKeys = transactionStmt.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        System.out.println("Failed to create transaction record.");
                        connection.rollback();
                        return;
                    }
                }
            }

            // Step 5: Update ticket records
            String updateTicketQuery = "UPDATE Tickets SET customer_code = ?, status = 'transferred' WHERE ticket_code = ?";
            for (int ticketCode : ticketCodes) {
                try (PreparedStatement updateTicketStmt = connection.prepareStatement(updateTicketQuery)) {
                    updateTicketStmt.setInt(1, newCustomerCode);
                    updateTicketStmt.setInt(2, ticketCode);
                    updateTicketStmt.executeUpdate();
                }
            }

            // Step 6: Update concert record (if applicable)
            String updateConcertQuery = """
                    UPDATE Concerts
                    SET tickets_available = tickets_available - ?
                    WHERE concert_code = (SELECT concert_code FROM Tickets WHERE ticket_code = ?)
                    """;
            for (int ticketCode : ticketCodes) {
                try (PreparedStatement updateConcertStmt = connection.prepareStatement(updateConcertQuery)) {
                    updateConcertStmt.setInt(1, ticketCodes.length);
                    updateConcertStmt.setInt(2, ticketCode);
                    updateConcertStmt.executeUpdate();
                }
            }

            connection.commit(); // Commit transaction
            System.out.println("Ticket transfer successful!");

        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback transaction on error
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            System.err.println("Error transferring tickets: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true); // Restore default auto-commit
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
}
