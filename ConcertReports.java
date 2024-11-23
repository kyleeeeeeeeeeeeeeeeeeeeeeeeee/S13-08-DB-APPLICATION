import java.sql.*;
import java.util.InputMismatchException;

public class ConcertReports {

    Connection connection;
    Statement statement;

    public ConcertReports(Connection connection) {
        this.connection = connection;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Handles the report menu
    public void reportMenu() {
        boolean programRun = true;
        boolean inputRun;

        while (programRun) {
            System.out.println("\n--- Reports Menu ---");
            System.out.println("[1] Annual Sales");
            System.out.println("[2] Concert Analysis");
            System.out.println("[3] Customer Engagement");
            System.out.println("[4] Top Sales");
            System.out.println("[5] Back to Main Menu");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = MyJDBC.getUserInput("Choice: ");

                    switch (choice) {
                        case 1:
                            inputRun = false;
                            annualSales();
                            break;
                        case 2:
                            inputRun = false;
                            concertAnalysis();
                            break;
                        case 3:
                            inputRun = false;
                            customerEngagement();
                            break;
                        case 4:
                            inputRun = false;
                            topSales();
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

    private void annualSales() {
        System.out.println("\n--- Annual Sales Report ---");
        int year = MyJDBC.getUserInput("Enter year for the report: ");

        // SQL query to calculate the annual sales by month
        String query = """
        SELECT 
            MONTH(transaction_date) AS month,
            SUM(total_amount) AS total_sales,
            COUNT(transaction_code) AS tickets_sold
        FROM Transactions
        WHERE transaction_type = 'buy'
          AND YEAR(transaction_date) = ?
        GROUP BY MONTH(transaction_date)
        ORDER BY MONTH(transaction_date);
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, year); // Set the year parameter

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n--- Sales Report for Year: " + year + " ---");
                System.out.printf("%-10s | %-15s | %-15s%n", "Month", "Total Sales", "Tickets Sold");
                System.out.println("-----------------------------------------------");

                double yearlyTotal = 0;  // Use double for accurate sales calculation
                int yearlyTickets = 0;

                while (rs.next()) {
                    int month = rs.getInt("month");
                    double totalSales = rs.getDouble("total_sales");  // Adjust to handle decimal values
                    int ticketsSold = rs.getInt("tickets_sold");

                    yearlyTotal += totalSales;
                    yearlyTickets += ticketsSold;

                    System.out.printf("%-10s | ₱%-15.2f | %-15d%n", getMonthName(month), totalSales, ticketsSold);
                }

                System.out.println("-----------------------------------------------");
                System.out.printf("%-10s | ₱%-15.2f | %-15d%n", "Total", yearlyTotal, yearlyTickets);

            } catch (SQLException e) {
                System.err.println("Error fetching sales data: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error preparing sales report: " + e.getMessage());
        }
    }

    // Helper method to convert month number to month name
    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "January";
            case 2 -> "February";
            case 3 -> "March";
            case 4 -> "April";
            case 5 -> "May";
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> "Unknown";
        };
    }

    private void concertAnalysis() {
        System.out.println("\n--- Concert Analysis Report ---");

        // Get the year for the report
        int year = MyJDBC.getUserInput("Enter year for the report: ");

        // SQL query to get concert analysis data
        String query = """
        SELECT 
            Concerts.concert_code,
            Concerts.concert_title,  -- Concert title is now selected
            Concerts.performer_name,
            COUNT(Tickets.ticket_code) AS total_tickets_sold
        FROM Concerts
        LEFT JOIN Tickets ON Concerts.concert_code = Tickets.concert_code
        LEFT JOIN Transactions ON Tickets.transaction_code = Transactions.transaction_code
        WHERE YEAR(Concerts.concert_date) = ?
        AND Transactions.transaction_type = 'buy'  -- Only consider 'buy' transactions
        GROUP BY Concerts.concert_code, Concerts.concert_title, Concerts.performer_name
        ORDER BY total_tickets_sold DESC;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, year); // Set the year parameter

            try (ResultSet rs = stmt.executeQuery()) {
                // Print the headers for the report
                System.out.printf("\n%-15s %-30s %-25s %-15s\n", "Concert Code", "Concert Title", "Performer", "Tickets Sold");
                System.out.println("-------------------------------------------------------------");

                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    // Print each concert's data
                    System.out.printf("%-15d %-30s %-25s %-15d\n",
                            rs.getInt("concert_code"),
                            rs.getString("concert_title"),
                            rs.getString("performer_name"),
                            rs.getInt("total_tickets_sold"));
                }

                if (!hasResults) {
                    System.out.println("No concert data found for the given year.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating concert analysis report: " + e.getMessage());
        }
    }

    private void topSales() {
        System.out.println("\n--- Top Selling Concerts Report ---");

        int year = MyJDBC.getUserInput("Enter year for the report: ");
        String query = """
            SELECT 
                Concerts.concert_code,
                Concerts.performer_name,
                Concerts.concert_title,  -- Added the title column
                SUM(Transactions.total_amount) AS total_sales
            FROM Tickets
            JOIN Transactions ON Tickets.transaction_code = Transactions.transaction_code
            JOIN Concerts ON Tickets.concert_code = Concerts.concert_code
            WHERE YEAR(Transactions.transaction_date) = ?
              AND Transactions.transaction_type = 'buy'
            GROUP BY Concerts.concert_code, Concerts.performer_name, Concerts.concert_title
            ORDER BY total_sales DESC;
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("\n%-15s %-25s %-30s %-15s\n", "Concert Code", "Performer", "Concert Title", "Total Sales");
                System.out.println("---------------------------------------------------------------------");

                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    System.out.printf("%-15d %-25s %-30s ₱%-15.2f\n",
                            rs.getInt("concert_code"),
                            rs.getString("performer_name"),
                            rs.getString("concert_title"),  // Display concert title
                            rs.getDouble("total_sales"));
                }

                if (!hasResults) {
                    System.out.println("No sales data found for the given year.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating top sales report: " + e.getMessage());
        }
    }

    private void customerEngagement() {
        System.out.println("\n--- Customer Engagement Report ---");

        // Prompt the user for the year
        int year = MyJDBC.getUserInput("Enter year for the report: ");

        // Updated SQL query to correctly sum ticket and miscellaneous sales
        String query = """
    SELECT\s
        Customers.customer_code,
        Customers.first_name,
        Customers.last_name,
        COUNT(DISTINCT Transactions.transaction_code) AS total_transactions,
        SUM(CASE\s
            WHEN Transactions.transaction_type = 'buy' AND Refunds.ticket_code IS NULL THEN Transactions.total_amount\s
            ELSE 0\s
        END) AS ticket_sales,
        SUM(CASE\s
            WHEN Transactions.transaction_type != 'buy' THEN Transactions.total_amount\s
            ELSE 0\s
        END) AS other_sales,
        SUM(CASE\s
            WHEN Transactions.transaction_type = 'buy' AND Refunds.ticket_code IS NULL THEN Transactions.total_amount\s
            ELSE 0\s
        END) + SUM(CASE\s
            WHEN Transactions.transaction_type != 'buy' THEN Transactions.total_amount\s
            ELSE 0\s
        END) AS total_sales
    FROM Customers
    JOIN Transactions ON Customers.customer_code = Transactions.customer_code
    LEFT JOIN Tickets ON Transactions.transaction_code = Tickets.transaction_code
    LEFT JOIN Refunds ON Tickets.ticket_code = Refunds.ticket_code
    WHERE YEAR(Transactions.transaction_date) = ?
    GROUP BY Customers.customer_code, Customers.first_name, Customers.last_name
    ORDER BY total_sales DESC;
   \s""";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("\n%-15s %-20s %-20s %-20s %-15s %-15s %-15s\n",
                        "Customer ID", "First Name", "Last Name",
                        "Total Transactions", "Total Sales",
                        "Ticket Sales", "Misc Sales");
                System.out.println("----------------------------------------------------------------------------------------------");

                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    System.out.printf(
                            "%-15d %-20s %-20s %-20d ₱%-15.2f ₱%-15.2f ₱%-15.2f\n",
                            rs.getInt("customer_code"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getInt("total_transactions"),
                            rs.getDouble("total_sales"),
                            rs.getDouble("ticket_sales"),
                            rs.getDouble("other_sales")
                    );
                }
                if (!hasResults) {
                    System.out.println("No transactions found for the given year.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating Customer Engagement Report: " + e.getMessage());
        }
    }

}
