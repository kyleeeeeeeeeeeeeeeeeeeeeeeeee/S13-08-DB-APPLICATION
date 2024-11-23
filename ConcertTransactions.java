import java.sql.*;
import java.util.InputMismatchException;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

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
                            transferTicketsMenu();
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
        System.out.println("\n--- Sell Tickets ---");
        int customerCode = MyJDBC.getUserInput("Enter Customer ID: ");
        int concertCode = MyJDBC.getUserInput("Enter Concert ID: ");
        double ticketPrice = 0.0;
        String ticketType = null;
        String selectedSeatType = null;

        try {
            connection.setAutoCommit(false);

            // Check if banned
            String checkBanQuery = """
        SELECT 1 FROM Bans WHERE customer_code = ?;
        """;
            try (PreparedStatement ps = connection.prepareStatement(checkBanQuery)) {
                ps.setInt(1, customerCode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Customer is banned.");
                        return; // Exit early if the customer is banned
                    }
                }
            }

            // Check if customer meets entry restrictions for the concert
            String checkCustomerAge = """
        SELECT 1
        FROM Customers cm
        JOIN Concerts cr ON cr.concert_code = ? 
        WHERE cm.customer_code = ? 
          AND (cr.entry_restrictions != '18+' 
               OR (cr.entry_restrictions = '18+' 
                   AND TIMESTAMPDIFF(YEAR, cm.birth_date, CURDATE()) >= 18));
        """;
            try (PreparedStatement ps = connection.prepareStatement(checkCustomerAge)) {
                ps.setInt(1, concertCode);  // concertCode
                ps.setInt(2, customerCode);   // customerCode
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Customer meets the entry restrictions
                        System.out.println("Customer meets the entry restrictions.");
                    } else {
                        // Customer does not meet entry restrictions
                        System.out.println("Customer does not meet the entry restrictions.");
                        connection.rollback();  // Rollback transaction if any
                        return;
                    }
                }
            }

            // Check concert validity and ticket availability
            String validateConcertQuery = """
        SELECT tickets_available
        FROM Concerts
        WHERE concert_code = ? AND status = 'approved';
        """;
            int ticketsAvailable = 0;
            try (PreparedStatement ps = connection.prepareStatement(validateConcertQuery)) {
                ps.setInt(1, concertCode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ticketsAvailable = rs.getInt("tickets_available");
                        if (ticketsAvailable <= 0) {
                            System.out.println("No tickets available for this concert.");
                            connection.rollback();
                            return;
                        }
                    } else {
                        System.out.println("Concert not found or not approved.");
                        connection.rollback();
                        return;
                    }
                }
            }

            // Get price and available seat types from Prices table
            String getPriceAndSeatTypesQuery = """
        SELECT price, ticket_type 
        FROM Prices
        WHERE concert_code = ?;
        """;
            List<String> seatTypes = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(getPriceAndSeatTypesQuery)) {
                ps.setInt(1, concertCode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ticketPrice = rs.getDouble("price");
                        ticketType = rs.getString("ticket_type");

                        // Collect available seat types
                        seatTypes.add(ticketType);  // Assuming each concert has only one ticket type
                    } else {
                        System.out.println("No price found for this concert.");
                        connection.rollback();
                        return;
                    }
                }
            }

            // Display available seat types and allow the user to select one
            if (!seatTypes.isEmpty()) {
                System.out.println("Available seat types:");
                for (int i = 0; i < seatTypes.size(); i++) {
                    System.out.println((i + 1) + ". " + seatTypes.get(i));
                }
                int seatTypeIndex = MyJDBC.getUserInput("Select Seat Type: ") - 1; // Assuming user input is 1-based
                if (seatTypeIndex >= 0 && seatTypeIndex < seatTypes.size()) {
                    selectedSeatType = seatTypes.get(seatTypeIndex);
                } else {
                    System.out.println("Invalid seat type selection.");
                    connection.rollback();
                    return;
                }
            }

            // Input Seat Number
            String seatNumber = MyJDBC.getUserStringInput("Enter Seat Number: ");
            String checkSeatQuery = """
        SELECT 1
        FROM Tickets
        WHERE concert_code = ? AND seat_number = ?;
        """;
            boolean isSeatTaken = false;
            try (PreparedStatement ps = connection.prepareStatement(checkSeatQuery)) {
                ps.setInt(1, concertCode);
                ps.setString(2, seatNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        isSeatTaken = true;
                        System.out.println("Seat is already taken.");
                        connection.rollback();
                        return;
                    }
                }
            }

            // Insert ticket into Tickets table
            String insertTicketQuery = """
        INSERT INTO Tickets (concert_code, transaction_code, ticket_type, seat_number, ticket_price)
        VALUES (?, ?, ?, ?, ?);
        """;
            try (PreparedStatement ps = connection.prepareStatement(insertTicketQuery)) {
                ps.setInt(1, concertCode);
                ps.setInt(2, customerCode);
                ps.setString(3, selectedSeatType);  // Use the selected seat type
                ps.setString(4, seatNumber);
                ps.setDouble(5, ticketPrice);
                ps.executeUpdate();
            }

            // Decrement Concerts -> tickets_available
            String updateConcertQuery = """
        UPDATE Concerts
        SET tickets_available = tickets_available - 1
        WHERE concert_code = ?;
        """;
            try (PreparedStatement ps = connection.prepareStatement(updateConcertQuery)) {
                ps.setInt(1, concertCode);
                ps.executeUpdate();
            }

            // Record ticket sale to Transactions
            String insertTransactionQuery = """
        INSERT INTO Transactions(customer_code, transaction_type, transaction_date, total_amount, payment_method)
        VALUES (?, ?, ?, ?, ?);
        """;
            try (PreparedStatement ps = connection.prepareStatement(insertTransactionQuery)) {
                String transactionDate = LocalDate.now().toString();  // Automatically set the current date
                String paymentMethod = MyJDBC.getUserStringInput("Enter Payment Method: ");
                ps.setInt(1, customerCode);
                ps.setString(2, "buy");
                ps.setString(3, transactionDate);  // Use current date
                ps.setDouble(4, ticketPrice);
                ps.setString(5, paymentMethod);
                ps.executeUpdate();
            }

            connection.commit(); // Commit transaction
            System.out.println("Ticket sale completed successfully.");

        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback transaction on error
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            System.err.println("Error selling ticket: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true); // Restore default auto-commit
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    private void refundTickets() {
        System.out.println("\n--- Refund Tickets ---");
        int customerId = MyJDBC.getUserInput("Enter Customer ID: ");
        String ticketIdsInput = MyJDBC.getUserStringInput("Enter Ticket IDs to refund (comma-separated): ");
        String[] ticketIds = ticketIdsInput.split(",");

        final double REFUND_FEE_PERCENTAGE = 0.10; // 10% fee
        double totalRefundFee = 0.0; // To track total refund fee
        String paymentMethod = MyJDBC.getUserStringInput("Enter payment method for the refund fee (cash, bank_transfer, card): ");

        try {
            connection.setAutoCommit(false);

            // Step 1: Check if the customer is in the Bans table
            String banCheckQuery = "SELECT * FROM Bans WHERE customer_code = ?";
            try (PreparedStatement banCheckStmt = connection.prepareStatement(banCheckQuery)) {
                banCheckStmt.setInt(1, customerId);
                try (ResultSet banCheckRs = banCheckStmt.executeQuery()) {
                    if (banCheckRs.next()) {
                        System.out.println("Customer is banned. Refund cannot be processed.");
                        connection.rollback();
                        return;
                    }
                }
            }

            // Step 2: Validate ticket ownership by checking if the ticket was originally bought by the customer
            String validateTicketQuery = """
        SELECT t.ticket_price, tr.transaction_code, t.ticket_code
        FROM Tickets t
        JOIN Transactions tr ON t.transaction_code = tr.transaction_code
        WHERE t.ticket_code = ? AND tr.customer_code = ? AND tr.transaction_type = 'buy';
        """;
            try (PreparedStatement validateStmt = connection.prepareStatement(validateTicketQuery)) {
                for (String ticketId : ticketIds) {
                    validateStmt.setInt(1, Integer.parseInt(ticketId.trim()));
                    validateStmt.setInt(2, customerId);
                    try (ResultSet rs = validateStmt.executeQuery()) {
                        if (rs.next()) {
                            double ticketPrice = rs.getDouble("ticket_price");
                            int originalTransactionCode = rs.getInt("transaction_code");
                            int ticketCode = rs.getInt("ticket_code");

                            // Calculate refund fee
                            double refundFee = ticketPrice * REFUND_FEE_PERCENTAGE;
                            totalRefundFee += refundFee;

                            // Step 3: Insert into Refunds table
                            String insertRefundQuery = "INSERT INTO Refunds (transaction_code, ticket_code) VALUES (?, ?)";
                            try (PreparedStatement refundStmt = connection.prepareStatement(insertRefundQuery)) {
                                refundStmt.setInt(1, originalTransactionCode); // Original transaction code
                                refundStmt.setInt(2, ticketCode); // Ticket code
                                refundStmt.executeUpdate();
                            }

                        } else {
                            System.out.printf("Ticket ID %s is either not owned by you or has already been refunded. Aborting transaction.\n", ticketId);
                            connection.rollback();
                            return;
                        }
                    }
                }
            }

            // Step 4: Record the refund transaction in the Transactions table
            String recordTransactionQuery = """
        INSERT INTO Transactions (customer_code, transaction_type, transaction_date, total_amount, payment_method)
        VALUES (?, 'refund', CURRENT_TIMESTAMP, ?, ?);
        """;
            try (PreparedStatement recordStmt = connection.prepareStatement(recordTransactionQuery, Statement.RETURN_GENERATED_KEYS)) {
                recordStmt.setInt(1, customerId);
                recordStmt.setDouble(2, totalRefundFee); // Only the refund fee
                recordStmt.setString(3, paymentMethod);
                recordStmt.executeUpdate();
            }

            connection.commit();
            System.out.printf("Refund successful. Total refund fee charged: ₱%.2f.\n", totalRefundFee);
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
        int concertCode = MyJDBC.getUserInput("Enter Concert Code to cancel: ");
        final double REFUND_FEE_PERCENTAGE = 0.10; // Refund fee of 10%

        try {
            connection.setAutoCommit(false); // Disable auto-commit for transaction management

            // Step 1: Check if concert code is valid
            String validateConcertQuery = "SELECT COUNT(*) AS concert_count FROM Concerts WHERE concert_code = ?";
            try (PreparedStatement ps = connection.prepareStatement(validateConcertQuery)) {
                ps.setInt(1, concertCode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("concert_count") == 0) {
                        System.err.println("No concert found with the provided Concert Code.");
                        return; // Exit if the concert does not exist
                    }
                }
            }

            // Step 2: Update concert status to cancelled
            String updateConcertQuery = "UPDATE Concerts SET status = 'cancelled' WHERE concert_code = ?";
            try (PreparedStatement ps = connection.prepareStatement(updateConcertQuery)) {
                ps.setInt(1, concertCode);
                ps.executeUpdate();
                System.out.println("Concert status updated to 'cancelled'.");
            }

            // Step 3: Process refunds for all tickets associated with the cancelled concert
            String selectTicketsQuery = """
            SELECT Tickets.ticket_code, Tickets.ticket_price, Transactions.customer_code
            FROM Tickets
            JOIN Transactions ON Tickets.transaction_code = Transactions.transaction_code
            WHERE Tickets.concert_code = ?;
        """;
            try (PreparedStatement ps = connection.prepareStatement(selectTicketsQuery)) {
                ps.setInt(1, concertCode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.isBeforeFirst()) { // Check if no tickets exist
                        System.out.println("No tickets found for the specified concert.");
                    }

                    while (rs.next()) {
                        double ticketPrice = rs.getDouble("ticket_price");
                        int customerCode = rs.getInt("customer_code");

                        // Calculate refund amount after applying the refund fee
                        double refundAmount = ticketPrice * (1 - REFUND_FEE_PERCENTAGE);

                        // Step 4: Record the refund transaction
                        String recordTransactionQuery = """
                        INSERT INTO Transactions (customer_code, transaction_type, transaction_date, total_amount)
                        VALUES (?, 'refund', CURRENT_TIMESTAMP, ?);
                    """;
                        try (PreparedStatement transactionPs = connection.prepareStatement(recordTransactionQuery)) {
                            transactionPs.setInt(1, customerCode); // Set customer_code parameter
                            transactionPs.setDouble(2, refundAmount); // Set refundAmount parameter
                            transactionPs.executeUpdate(); // Execute insert query for refund
                        }
                    }
                }
            }

            // Step 5: Update the venue's availability to 'available'
            String updateVenueAvailabilityQuery = """
            UPDATE AvailableVenues
            SET availability = 'available'
            WHERE venue_code = (SELECT venue_code FROM Concerts WHERE concert_code = ?);
        """;
            try (PreparedStatement ps = connection.prepareStatement(updateVenueAvailabilityQuery)) {
                ps.setInt(1, concertCode);
                ps.executeUpdate();
                System.out.println("Venue availability updated to 'available'.");
            }

            // Step 6: Commit all changes
            connection.commit();
            System.out.println("Concert cancelled, refund transactions recorded, and venue set to available.");

        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback transaction if any error occurs
                System.err.println("Transaction rolled back due to an error: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                connection.setAutoCommit(true); // Restore auto-commit mode
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    // Handles the flow for transferring tickets
    private void transferTicketsMenu() {
        try {
            int oldCustomerCode = MyJDBC.getUserInput("Enter current customer's ID: ");
            int newCustomerCode = MyJDBC.getUserInput("Enter new customer's ID: ");
            System.out.println("Enter ticket IDs separated by commas (e.g., 1,2,3): ");
            String ticketInput = MyJDBC.getUserStringInput("Ticket IDs: ");
            String paymentMethod = MyJDBC.getUserStringInput("Enter payment method (cash, bank_transfer, card): ");
            String[] ticketStrings = ticketInput.split(",");
            int[] ticketIds = new int[ticketStrings.length];
            for (int i = 0; i < ticketStrings.length; i++) {
                ticketIds[i] = Integer.parseInt(ticketStrings[i].trim());
            }

            // Call the method that performs the transfer
            transferTickets(oldCustomerCode, newCustomerCode, ticketIds, paymentMethod);

        } catch (NumberFormatException e) {
            System.out.println("Invalid ticket IDs. Please enter numbers separated by commas.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public void transferTickets(int oldCustomerCode, int newCustomerCode, int[] ticketCodes, String paymentMethod) {
        try {
            connection.setAutoCommit(false); // Start transaction

            // Step 1: Verify the old customer owns the tickets and check the last transaction status
            String checkOwnershipQuery = """
            SELECT T.ticket_code, Tr.transaction_code, Tr.transaction_type
            FROM Tickets T
            JOIN Transactions Tr ON T.transaction_code = Tr.transaction_code
            WHERE Tr.customer_code = ? AND T.ticket_code = ?;
        """;

            // Step 2: Insert the new transaction record
            String insertTransactionQuery = """
            INSERT INTO Transactions (customer_code, transaction_type, transaction_date, total_amount, payment_method)
            VALUES (?, 'transfer', CURRENT_TIMESTAMP, 100.00, ?);
        """;

            // Step 3: Update the ticket record with the new customer and transaction
            String updateTicketQuery = """
            UPDATE Tickets
            SET transaction_code = ?
            WHERE ticket_code = ? AND transaction_code = ?;
        """;

            int newTransactionCode;

            for (int ticketCode : ticketCodes) {
                int oldTransactionCode;

                // Step 1: Verify ownership and check if the ticket has already been transferred
                try (PreparedStatement checkStmt = connection.prepareStatement(checkOwnershipQuery)) {
                    checkStmt.setInt(1, oldCustomerCode);
                    checkStmt.setInt(2, ticketCode);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("Ticket code " + ticketCode + " not found for customer " + oldCustomerCode);
                            connection.rollback();
                            return;
                        }

                        // Fetch transaction details
                        oldTransactionCode = rs.getInt("transaction_code");
                        String transactionType = rs.getString("transaction_type");

                        if ("transfer".equalsIgnoreCase(transactionType)) {
                            System.out.println("Ticket code " + ticketCode + " has already been transferred and cannot be transferred again.");
                            connection.rollback();
                            return;
                        }

                        System.out.println("Ownership verified for Ticket Code: " + ticketCode);
                    }
                }

                // Step 2: Create a new transaction for the transfer
                try (PreparedStatement transactionStmt = connection.prepareStatement(insertTransactionQuery, Statement.RETURN_GENERATED_KEYS)) {
                    transactionStmt.setInt(1, newCustomerCode);
                    transactionStmt.setString(2, paymentMethod);
                    transactionStmt.executeUpdate();

                    try (ResultSet generatedKeys = transactionStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            newTransactionCode = generatedKeys.getInt(1);
                            System.out.println("New transaction created with transaction code: " + newTransactionCode);
                        } else {
                            System.out.println("Failed to create transaction record.");
                            connection.rollback();
                            return;
                        }
                    }
                }

                // Step 3: Update the ticket record with the new transaction code
                try (PreparedStatement updateTicketStmt = connection.prepareStatement(updateTicketQuery)) {
                    updateTicketStmt.setInt(1, newTransactionCode);
                    updateTicketStmt.setInt(2, ticketCode);
                    updateTicketStmt.setInt(3, oldTransactionCode);

                    int affectedRows = updateTicketStmt.executeUpdate();
                    if (affectedRows == 0) {
                        System.out.println("Failed to transfer Ticket Code: " + ticketCode);
                        connection.rollback();
                        return;
                    }
                    System.out.println("Ticket Code " + ticketCode + " successfully transferred.");
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
