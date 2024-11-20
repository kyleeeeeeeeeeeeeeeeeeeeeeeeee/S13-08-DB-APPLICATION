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
            default -> "N/A";
        };
    }

    private void annualSales() {
        System.out.println("\n--- Annual Sales Report ---");
        int year = MyJDBC.getUserInput("Enter year for the report: ");

        String query = """
        SELECT 
            MONTH(transaction_date) AS month,
            SUM(total_amount) AS total_sales,
            COUNT(transaction_code) AS tickets_sold
        FROM Transactions
        WHERE transaction_type = 'buy'
          AND transaction_status = 'closed'
          AND YEAR(transaction_date) = ?
        GROUP BY MONTH(transaction_date)
        ORDER BY MONTH(transaction_date);
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n--- Sales Report for Year: " + year + " ---");
                System.out.printf("%-10s | %-15s | %-15s%n", "Month", "Total Sales", "Tickets Sold");
                System.out.println("-----------------------------------------------");

                int yearlyTotal = 0;
                int yearlyTickets = 0;

                while (rs.next()) {
                    int month = rs.getInt("month");
                    int totalSales = rs.getInt("total_sales");
                    int ticketsSold = rs.getInt("tickets_sold");

                    yearlyTotal += totalSales;
                    yearlyTickets += ticketsSold;

                    System.out.printf("%-10s | %-15d | %-15d%n", getMonthName(month), totalSales, ticketsSold);
                }

                System.out.println("-----------------------------------------------");
                System.out.printf("%-10s | %-15d | %-15d%n", "Total", yearlyTotal, yearlyTickets);

            } catch (SQLException e) {
                System.err.println("Error fetching sales data: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error preparing sales report: " + e.getMessage());
        }
    }


private void concertAnalysis() {
    System.out.println("\n--- Concert Analysis Report ---");
    int year = MyJDBC.getUserInput("Enter year for the report: ");
    String query = """
        SELECT 
            Concerts.concert_code, Concerts.performer_name, COUNT(Tickets.ticket_code) AS total_tickets_sold
        FROM Concerts
        LEFT JOIN Tickets ON Concerts.concert_code = Tickets.concert_code
        WHERE YEAR(Concerts.start_time) = ?
        GROUP BY Concerts.concert_code, Concerts.performer_name
        ORDER BY total_tickets_sold DESC;
    """;

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, year);
        try (ResultSet rs = stmt.executeQuery()) {
            System.out.printf("\n%-15s %-25s %-15s\n", "Concert Code", "Performer", "Tickets Sold");
            System.out.println("-------------------------------------------------------------");

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                System.out.printf("%-15d %-25s %-15d\n",
                        rs.getInt("concert_code"), rs.getString("performer_name"), rs.getInt("total_tickets_sold"));
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
        System.out.println("Top Sales Report functionality is under development.");
        // Implement the logic here
    }

    private void customerEngagement() {
        System.out.println("\n--- Customer Engagement Report ---");

        // Prompt the user for the year
        int year = MyJDBC.getUserInput("Enter year for the report: ");

        // Updated SQL query to correctly sum ticket and miscellaneous sales for total_sales
        String query = """
        SELECT 
            Customers.customer_code,
            Customers.first_name,
            Customers.last_name,
            COUNT(DISTINCT Transactions.transaction_code) AS total_transactions,
            SUM(CASE 
                WHEN Tickets.status != 'refunded' THEN Transactions.total_amount 
                ELSE 0 
            END) + SUM(CASE 
                WHEN Transactions.transaction_type != 'buy' THEN Transactions.total_amount 
                ELSE 0 
            END) AS total_sales,
            SUM(CASE 
                WHEN Transactions.transaction_type = 'buy' AND Tickets.status != 'refunded' THEN Transactions.total_amount 
                ELSE 0 
            END) AS ticket_sales,
            SUM(CASE 
                WHEN Transactions.transaction_type != 'buy' THEN Transactions.total_amount 
                ELSE 0 
            END) AS other_sales
        FROM Customers
        JOIN Transactions ON Customers.customer_code = Transactions.customer_code
        LEFT JOIN Tickets ON Transactions.transaction_code = Tickets.transaction_code
        WHERE YEAR(Transactions.transaction_date) = ?
        GROUP BY Customers.customer_code, Customers.first_name, Customers.last_name
        ORDER BY total_sales DESC;
    """;

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
