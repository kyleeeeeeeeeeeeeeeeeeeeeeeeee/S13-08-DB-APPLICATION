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
        System.out.println("Concert Record functionality is under development.");
        // Implement logic here
    }

    private void customerRecord() {
        System.out.println("Customer Record functionality is under development.");
        // Implement logic here
    }

    private void transactionRecord() {
        System.out.println("Transaction Record functionality is under development.");
        // Implement logic here
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
