import java.sql.*;
import java.util.InputMismatchException;
import java.text.SimpleDateFormat;

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
    SELECT\s
        Concerts.concert_code,
        Concerts.performer_name,
        Concerts.concert_title,
        Concerts.entry_restrictions,
        Venues.venue_name,
        Concerts.concert_date,
        Concerts.tickets_available
    FROM Concerts
    JOIN Venues ON Concerts.venue_code = Venues.venue_code
    WHERE Concerts.concert_code = ?;
   \s""";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, concertId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Concert Title: " + rs.getString("concert_title"));
                    System.out.printf("\nConcert Code: %d\n", rs.getInt("concert_code"));
                    System.out.println("Performer(s): " + rs.getString("performer_name"));
                    System.out.println("Entry Restrictions: " + rs.getString("entry_restrictions"));
                    System.out.println("Venue: " + rs.getString("venue_name"));
                    System.out.println("Date: " + rs.getDate("concert_date"));
                    System.out.println("Tickets Available: " + rs.getInt("tickets_available"));

                    // Updated query to correctly fetch customers who bought tickets
                    String customerQuery = """
                SELECT Customers.first_name, Customers.last_name
                FROM Customers
                JOIN Transactions ON Customers.customer_code = Transactions.customer_code
                JOIN Tickets ON Transactions.transaction_code = Tickets.transaction_code
                WHERE Tickets.concert_code = ?;
                """;

                    try (PreparedStatement customerStmt = connection.prepareStatement(customerQuery)) {
                        customerStmt.setInt(1, concertId);
                        try (ResultSet customerRs = customerStmt.executeQuery()) {
                            System.out.println("\n--- Customers Who Bought Tickets ---");
                            boolean hasCustomers = false;
                            while (customerRs.next()) {
                                hasCustomers = true;
                                System.out.println(customerRs.getString("first_name") + " " + customerRs.getString("last_name"));
                            }
                            if (!hasCustomers) {
                                System.out.println("No customers have purchased tickets for this concert.");
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

        // Updated query to join Customers, Transactions, and Tickets based on the new schema
        String query = """
        SELECT 
            Customers.customer_code, Customers.first_name, Customers.last_name, Customers.email, Customers.contact_number,
            Tickets.ticket_code, Tickets.seat_number, Transactions.transaction_type
        FROM Customers
        LEFT JOIN Transactions ON Customers.customer_code = Transactions.customer_code
        LEFT JOIN Tickets ON Transactions.transaction_code = Tickets.transaction_code
        WHERE Customers.customer_code = ?;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Print customer details
                    System.out.printf("\nCustomer ID: %d\nName: %s %s\nEmail: %s\nContact: %s\n",
                            rs.getInt("customer_code"), rs.getString("first_name"), rs.getString("last_name"),
                            rs.getString("email"), rs.getString("contact_number"));

                    System.out.println("\n--- Tickets Owned ---");
                    do {
                        // Retrieve seat_number as a String to handle alphanumeric seat identifiers
                        String seatNumber = rs.getString("seat_number");
                        System.out.printf("Ticket Code: %d | Seat: %s | Transaction Type: %s\n",
                                rs.getInt("ticket_code"), seatNumber, rs.getString("transaction_type"));
                    } while (rs.next()); // Iterate through all tickets
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
                    System.out.println("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("transaction_date")));
                    System.out.println("Total Amount: ₱" + rs.getDouble("total_amount"));
                    System.out.println("Payment Method: " + rs.getString("payment_method"));
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

    // Method to view the ticket record
    public void ticketRecord(int ticketCode) {
        String query = """
        SELECT
            Tickets.ticket_code, Tickets.ticket_price, Tickets.seat_number, Tickets.ticket_type,
            Concerts.concert_title, Concerts.performer_name, Concerts.concert_date,
            Venues.venue_name, Transactions.transaction_date, Transactions.transaction_type,
            Customers.first_name AS customer_first_name,
            Customers.last_name AS customer_last_name, Customers.email AS customer_email,
            Customers.contact_number AS customer_contact
        FROM Tickets
        JOIN Concerts ON Tickets.concert_code = Concerts.concert_code
        JOIN Artists ON Concerts.artist_code = Artists.artist_code
        JOIN Venues ON Concerts.venue_code = Venues.venue_code
        JOIN Transactions ON Tickets.transaction_code = Transactions.transaction_code
        JOIN Customers ON Transactions.customer_code = Customers.customer_code
        WHERE Tickets.ticket_code = ?;
       """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ticketCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n--- Ticket Details ---");
                    System.out.println("Ticket Code: " + rs.getInt("ticket_code"));
                    System.out.println("Ticket Price: ₱" + rs.getBigDecimal("ticket_price"));
                    System.out.println("Seat Type: " + rs.getString("ticket_type"));
                    System.out.println("Seat Number: " + rs.getString("seat_number"));
                    System.out.println("Transacted: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("transaction_date")));

                    System.out.println("\n--- Concert Details ---");
                    System.out.println("Event Name: " + rs.getString("concert_title"));
                    System.out.println("Performer: " + rs.getString("performer_name"));
                    System.out.println("Concert Date: " + rs.getDate("concert_date"));
                    System.out.println("Venue: " + rs.getString("venue_name"));

                    System.out.println("\n--- Customer Details ---");
                    String currentCustomer = rs.getString("customer_first_name") + " " + rs.getString("customer_last_name");
                    System.out.println("Name: " + currentCustomer);
                    System.out.println("Email: " + rs.getString("customer_email"));
                    System.out.println("Contact Number: " + rs.getString("customer_contact"));

                    // Determine if the current customer is the original buyer
                    String transactionType = rs.getString("transaction_type");
                    if ("transfer".equalsIgnoreCase(transactionType)) {
                        // Find the original buyer
                        String originalBuyerQuery = """
                        SELECT Customers.first_name, Customers.last_name
                        FROM Transactions
                        JOIN Tickets ON Transactions.transaction_code = Tickets.transaction_code
                        JOIN Customers ON Transactions.customer_code = Customers.customer_code
                        WHERE Tickets.ticket_code = ? AND Transactions.transaction_type = 'buy'
                        ORDER BY Transactions.transaction_date ASC
                        LIMIT 1;
                       """;

                        try (PreparedStatement originalStmt = connection.prepareStatement(originalBuyerQuery)) {
                            originalStmt.setInt(1, ticketCode);
                            try (ResultSet originalRs = originalStmt.executeQuery()) {
                                if (originalRs.next()) {
                                    String originalBuyer = originalRs.getString("first_name") + " " + originalRs.getString("last_name");
                                    System.out.println("Original Buyer: " + originalBuyer);
                                } else {
                                    System.out.println("Original Buyer: Unknown (Could not find original transaction)");
                                }
                            }
                        }
                    } else {
                        System.out.println("Original Buyer: Yes");
                    }
                } else {
                    System.out.println("No ticket record found for Ticket Code: " + ticketCode);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ticket record: " + e.getMessage());
        }
    }

}
