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
            System.out.println("[5] Venue Management");
            System.out.println("[6] Back to Main Menu");

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
                            venueMenu();
                            break;    
                        case 6:
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

public void venueMenu() {
    boolean programRun = true;

    while (programRun) {
        System.out.println("\n--- Venue Management Menu ---");
        System.out.println("[1] Add Venue");
        System.out.println("[2] Update Venue");
        System.out.println("[3] Delete Venue");
        System.out.println("[4] View All Venues");
        System.out.println("[5] View Transactions by Venue");
        System.out.println("[6] Back to Main Menu");

        int choice = MyJDBC.getUserInput("Choice: ");

        switch (choice) {
            case 1:
                addVenue();
                break;
            case 2:
                updateVenue();
                break;
            case 3:
                deleteVenue();
                break;
            case 4:
                viewVenues();
                break;
            case 5:
                viewVenueTransactions();
                break;
            case 6:
                programRun = false; // Exit the menu
                System.out.println("Returning to Main Menu...");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}


//code for add venue
private void addVenue() {
    try {
        String name = MyJDBC.getUserStringInput("Enter Venue Name: ");
        String address = MyJDBC.getUserStringInput("Enter Address: ");
        int capacity = MyJDBC.getUserInput("Enter Seating Capacity: ");

        String query = "INSERT INTO Venues (venue_name, address, seating_capacity, total_available_seats) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setString(2, address);
            ps.setInt(3, capacity);
            ps.setInt(4, capacity); // Initial available seats = capacity
            ps.executeUpdate();
            System.out.println("Venue added successfully.");
        }
    } catch (SQLException e) {
        System.err.println("Error adding venue: " + e.getMessage());
    }
}

//code for update venue
private void updateVenue() {
    try {
        int venueCode = MyJDBC.getUserInput("Enter Venue Code to update: ");
        String name = MyJDBC.getUserStringInput("Enter New Venue Name: ");
        String address = MyJDBC.getUserStringInput("Enter New Address: ");
        int capacity = MyJDBC.getUserInput("Enter New Seating Capacity: ");

        String query = "UPDATE Venues SET venue_name = ?, address = ?, seating_capacity = ?, total_available_seats = ? WHERE venue_code = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setString(2, address);
            ps.setInt(3, capacity);
            ps.setInt(4, capacity); // Reset available seats to new capacity
            ps.setInt(5, venueCode);
            ps.executeUpdate();
            System.out.println("Venue updated successfully.");
        }
    } catch (SQLException e) {
        System.err.println("Error updating venue: " + e.getMessage());
    }
}

//code for delete venue
private void deleteVenue() {
    try {
        int venueCode = MyJDBC.getUserInput("Enter Venue Code to delete: ");
        String query = "DELETE FROM Venues WHERE venue_code = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, venueCode);
            ps.executeUpdate();
            System.out.println("Venue deleted successfully.");
        }
    } catch (SQLException e) {
        System.err.println("Error deleting venue: " + e.getMessage());
    }
}

//code for viewing venues
private void viewVenues() {
    String query = "SELECT * FROM Venues";
    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
        System.out.printf("\n%-10s %-30s %-50s %-20s %-20s\n", "Venue Code", "Venue Name", "Address", "Seating Capacity", "Available Seats");
        while (rs.next()) {
            System.out.printf("%-10d %-30s %-50s %-20d %-20d\n",
                    rs.getInt("venue_code"),
                    rs.getString("venue_name"),
                    rs.getString("address"),
                    rs.getInt("seating_capacity"),
                    rs.getInt("total_available_seats"));
        }
    } catch (SQLException e) {
        System.err.println("Error viewing venues: " + e.getMessage());
    }
}

// viewing transactions by venue
private void viewVenueTransactions() {
    try {
        int venueCode = MyJDBC.getUserInput("Enter Venue Code: ");
        String query = """
            SELECT Transactions.transaction_code, Transactions.transaction_date, Transactions.total_amount, 
                   Customers.first_name, Customers.last_name, 
                   Tickets.ticket_code, Tickets.seat_number, 
                   Concerts.performer_name, Concerts.genre 
            FROM Transactions
            JOIN Tickets ON Transactions.transaction_code = Tickets.transaction_code
            JOIN Customers ON Tickets.customer_code = Customers.customer_code
            JOIN Concerts ON Tickets.concert_code = Concerts.concert_code
            WHERE Concerts.venue_code = ?;
        """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, venueCode);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("\n%-10s %-20s %-10s %-20s %-15s %-10s %-20s %-20s\n",
                        "Trans. Code", "Date", "Amount", "Customer", "Ticket Code", "Seat", "Performer", "Genre");
                while (rs.next()) {
                    System.out.printf("%-10d %-20s %-10.2f %-20s %-15d %-10d %-20s %-20s\n",
                            rs.getInt("transaction_code"),
                            rs.getTimestamp("transaction_date"),
                            rs.getDouble("total_amount"),
                            rs.getString("first_name") + " " + rs.getString("last_name"),
                            rs.getInt("ticket_code"),
                            rs.getInt("seat_number"),
                            rs.getString("performer_name"),
                            rs.getString("genre"));
                }
            }
        }
    } catch (SQLException e) {
        System.err.println("Error viewing transactions for venue: " + e.getMessage());
    }
}

}
