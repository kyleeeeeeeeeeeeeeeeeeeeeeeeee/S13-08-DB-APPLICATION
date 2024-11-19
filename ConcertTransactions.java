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
    System.out.println("\n--- Refund Tickets ---");
    int customerId = MyJDBC.getUserInput("Enter Customer ID: ");
    String ticketIdsInput = MyJDBC.getUserStringInput("Enter Ticket IDs to refund (comma-separated): ");
    String[] ticketIds = ticketIdsInput.split(",");

    final double REFUND_FEE_PERCENTAGE = 0.10; // 10% refund fee

    try {
        connection.setAutoCommit(false);

        // Step 1: Validate tickets and calculate refund amount
        double totalRefundAmount = 0.0;
        String validateTicketQuery = """
            SELECT ticket_price
            FROM Tickets
            WHERE ticket_code = ? AND customer_code = ? AND status != 'refunded';
        """;
        try (PreparedStatement validateStmt = connection.prepareStatement(validateTicketQuery)) {
            for (String ticketId : ticketIds) {
                validateStmt.setInt(1, Integer.parseInt(ticketId.trim()));
                validateStmt.setInt(2, customerId);
                try (ResultSet rs = validateStmt.executeQuery()) {
                    if (rs.next()) {
                        double ticketPrice = rs.getDouble("ticket_price");
                        double refundAmount = ticketPrice * (1 - REFUND_FEE_PERCENTAGE);
                        totalRefundAmount += refundAmount;
                    } else {
                        System.out.printf("Ticket ID %s is invalid or already refunded. Aborting transaction.\n", ticketId);
                        connection.rollback();
                        return;
                    }
                }
            }
        }

        // Step 2: Update ticket statuses
        String updateTicketQuery = "UPDATE Tickets SET status = 'refunded' WHERE ticket_code = ? AND customer_code = ?";
        try (PreparedStatement ticketStmt = connection.prepareStatement(updateTicketQuery)) {
            for (String ticketId : ticketIds) {
                ticketStmt.setInt(1, Integer.parseInt(ticketId.trim()));
                ticketStmt.setInt(2, customerId);
                ticketStmt.executeUpdate();
            }
        }

        // Step 3: Update concert available seats
        String updateConcertQuery = """
            UPDATE Concerts
            SET tickets_available = tickets_available + ?
            WHERE concert_code = (SELECT DISTINCT concert_code FROM Tickets WHERE ticket_code = ?);
        """;
        try (PreparedStatement concertStmt = connection.prepareStatement(updateConcertQuery)) {
            for (String ticketId : ticketIds) {
                concertStmt.setInt(1, 1); // Increase by 1 seat for each ticket refunded
                concertStmt.setInt(2, Integer.parseInt(ticketId.trim()));
                concertStmt.executeUpdate();
            }
        }

        // Step 4: Record the refund transaction
        String insertTransactionQuery = """
            INSERT INTO Transactions (customer_code, transaction_type, transaction_status, transaction_date, total_amount)
            VALUES (?, 'refund', 'closed', CURRENT_TIMESTAMP, ?);
        """;
        try (PreparedStatement transactionStmt = connection.prepareStatement(insertTransactionQuery)) {
            transactionStmt.setInt(1, customerId);
            transactionStmt.setDouble(2, totalRefundAmount);
            transactionStmt.executeUpdate();
        }

        connection.commit();
        System.out.printf("Refund successful. Total refunded amount: ₱%.2f (after %.0f%% fee).\n",
                totalRefundAmount, REFUND_FEE_PERCENTAGE * 100);
    } catch (SQLException e) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            System.err.println("Rollback failed: " + rollbackEx.getMessage());
        }
        System.err.println("Error processing refund: " + e.getMessage());
    } finally {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("Error resetting auto-commit: " + e.getMessage());
        }
    }
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
