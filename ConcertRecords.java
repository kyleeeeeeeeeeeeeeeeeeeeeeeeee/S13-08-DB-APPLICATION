import java.sql.*;
import java.util.InputMismatchException;

public class ConcertRecords {

    Connection connection;
    Statement statement;

    public ConcertRecords(Connection connection) {
        this.connection = connection;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Handles the record menu
    public void recordMenu() {
        boolean programRun = true;
        boolean inputRun;

        while (programRun) {
            System.out.println("\n--- Records Menu ---");
            System.out.println("[1] Concert Record");
            System.out.println("[2] Customer Record");
            System.out.println("[3] Ticket Record");
            System.out.println("[4] Transaction Record");
            System.out.println("[5] Back to Main Menu");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = MyJDBC.getUserInput("Choice: ");

                    switch (choice) {
                        case 1:
                            inputRun = false;
                            concertRecord(); // Placeholder
                            break;
                        case 2:
                            inputRun = false;
                            customerRecord(); // Placeholder
                            break;
                        case 3:
                            inputRun = false;
                            // Ask user for ticket code before calling ticketRecord()
                            int ticketCode = MyJDBC.getUserInput("Enter Ticket Code to be viewed: ");
                            ticketRecord(ticketCode); // Call ticketRecord with the ticketCode provided by user
                            break;
                        case 4:
                            inputRun = false;
                            transactionRecord(); // Placeholder
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

    private void concertRecord() {
        System.out.println("\n--- Concert Record ---");
        int concertId = MyJDBC.getUserInput("Enter Concert ID to view: ");

        String query = """
            SELECT concert_code, performer_name, genre, entry_restrictions, venue, date_time, tickets_available
            FROM Concerts
            WHERE concert_code = ?;
        """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, concertId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("\nConcert Code: %d\n", rs.getInt("concert_code"));
                    System.out.println("Performer(s): " + rs.getString("performer_name"));
                    System.out.println("Genre: " + rs.getString("genre"));
                    System.out.println("Entry Restrictions: " + rs.getString("entry_restrictions"));
                    System.out.println("Venue: " + rs.getString("venue"));
                    System.out.println("Date and Time: " + rs.getTimestamp("date_time"));
                    System.out.println("Tickets Available: " + rs.getInt("tickets_available"));

                    String customerQuery = """
                        SELECT Customers.first_name, Customers.last_name
                        FROM Customers
                        JOIN Tickets ON Customers.customer_code = Tickets.customer_code
                        WHERE Tickets.concert_code = ?;
                    """;
                    try (PreparedStatement customerStmt = connection.prepareStatement(customerQuery)) {
                        customerStmt.setInt(1, concertId);
                        try (ResultSet customerRs = customerStmt.executeQuery()) {
                            System.out.println("\n--- Customers Who Bought Tickets ---");
                            while (customerRs.next()) {
                                System.out.println(customerRs.getString("first_name") + " " + customerRs.getString("last_name"));
                            }
                        }
                    }
                } else {
                    System.out.println("No concert found for the given Concert ID.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching concert record: " + e.getMessage());
        }
    }

private void customerRecord() {
    System.out.println("\n--- Customer Record ---");
    int customerId = MyJDBC.getUserInput("Enter Customer ID to view: ");
    String query = """
        SELECT 
            Customers.customer_code, Customers.first_name, Customers.last_name, Customers.email, Customers.contact_number,
            Tickets.ticket_code, Tickets.seat_number, Tickets.status
        FROM Customers
        LEFT JOIN Tickets ON Customers.customer_code = Tickets.customer_code
        WHERE Customers.customer_code = ?;
    """;

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, customerId);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                System.out.printf("\nCustomer ID: %d\nName: %s %s\nEmail: %s\nContact: %s\n",
                        rs.getInt("customer_code"), rs.getString("first_name"), rs.getString("last_name"),
                        rs.getString("email"), rs.getString("contact_number"));

                System.out.println("\n--- Tickets Owned ---");
                do {
                    System.out.printf("Ticket Code: %d | Seat: %d | Status: %s\n",
                            rs.getInt("ticket_code"), rs.getInt("seat_number"), rs.getString("status"));
                } while (rs.next());
            } else {
                System.out.println("No customer record found for the given ID.");
            }
        }
    } catch (SQLException e) {
        System.err.println("Error fetching customer record: " + e.getMessage());
    }
}
    
private void transactionRecord() {
    System.out.println("\n--- Transaction Record ---");

    int transactionCode = MyJDBC.getUserInput("Enter Transaction Code to view: ");
    String query = """
        SELECT 
            Transactions.transaction_code,
            Transactions.transaction_type,
            Transactions.transaction_date,
            Transactions.total_amount,
            Transactions.payment_method,
            Transactions.refund_reason,
            Customers.first_name, Customers.last_name
        FROM Transactions
        JOIN Customers ON Transactions.customer_code = Customers.customer_code
        WHERE Transactions.transaction_code = ?;
    """;

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, transactionCode);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                System.out.println("\n--- Transaction Details ---");
                System.out.println("Transaction Code: " + rs.getInt("transaction_code"));
                System.out.println("Type: " + rs.getString("transaction_type"));
                System.out.println("Date: " + rs.getTimestamp("transaction_date"));
                System.out.println("Total Amount: ₱" + rs.getDouble("total_amount"));
                System.out.println("Payment Method: " + rs.getString("payment_method"));
                System.out.println("Refund Reason: " + rs.getString("refund_reason"));
                System.out.println("\n--- Customer Details ---");
                System.out.println("Name: " + rs.getString("first_name") + " " + rs.getString("last_name"));
            } else {
                System.out.println("No transaction found for the given Transaction Code.");
            }
        }
    } catch (SQLException e) {
        System.err.println("Error fetching transaction record: " + e.getMessage());
    }
}

    // Method to view the ticket record (already implemented)
    public void ticketRecord(int ticketCode) {
        String query = """
                SELECT 
                    Tickets.ticket_code, Tickets.ticket_price, Tickets.seat_number, Tickets.status,
                    Concerts.performer_name, Concerts.genre, Concerts.start_time, Concerts.end_time,
                    Customers.first_name AS customer_first_name, Customers.last_name AS customer_last_name,
                    Customers.email AS customer_email, Customers.contact_number AS customer_contact
                FROM Tickets
                JOIN Concerts ON Tickets.concert_code = Concerts.concert_code
                JOIN Customers ON Tickets.customer_code = Customers.customer_code
                WHERE Tickets.ticket_code = ?;
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ticketCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n--- Ticket Record ---");
                    System.out.println("Ticket Code: " + rs.getInt("ticket_code"));
                    System.out.println("Ticket Price: " + rs.getBigDecimal("ticket_price"));
                    System.out.println("Seat Number: " + rs.getInt("seat_number"));
                    System.out.println("Status: " + rs.getString("status"));
                    System.out.println("\n--- Concert Details ---");
                    System.out.println("Performer: " + rs.getString("performer_name"));
                    System.out.println("Genre: " + rs.getString("genre"));
                    System.out.println("Start Time: " + rs.getTimestamp("start_time"));
                    System.out.println("End Time: " + rs.getTimestamp("end_time"));
                    System.out.println("\n--- Customer Details ---");
                    System.out.println("Name: " + rs.getString("customer_first_name") + " " + rs.getString("customer_last_name"));
                    System.out.println("Email: " + rs.getString("customer_email"));
                    System.out.println("Contact Number: " + rs.getString("customer_contact"));
                } else {
                    System.out.println("No ticket record found for Ticket Code: " + ticketCode);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ticket record: " + e.getMessage());
        }
    }
}