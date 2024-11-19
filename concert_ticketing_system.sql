DROP DATABASE IF EXISTS `concerttix`;
CREATE DATABASE IF NOT EXISTS `concerttix`;
USE `concerttix`;

CREATE TABLE IF NOT EXISTS Companies (
    company_code INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(50),
    address_line1 VARCHAR(50),
    address_line2 VARCHAR(50),
    city VARCHAR(50),
    state VARCHAR(50),
    country VARCHAR(50),
    postal_code VARCHAR(10),
    company_email VARCHAR(50),
    company_contact_number VARCHAR(15),
    status ENUM('active', 'dissolved')
);

CREATE TABLE IF NOT EXISTS Artists (
    artist_code INT AUTO_INCREMENT PRIMARY KEY,
    company_code INT,
    stage_name VARCHAR(50),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    birth_date DATE,
    email VARCHAR(50),
    contact_number VARCHAR(15),
    nationality VARCHAR(50),
    FOREIGN KEY (company_code) REFERENCES Companies(company_code)
);

CREATE TABLE IF NOT EXISTS Venues (
    venue_code INT AUTO_INCREMENT PRIMARY KEY,
    venue_name VARCHAR(50),
    address_line1 VARCHAR(50),
    street VARCHAR(50),
    city VARCHAR(50),
    province VARCHAR(50),
    barangay VARCHAR(50),
    postal_code VARCHAR(10),
    total_seating_capacity INT
);

CREATE TABLE IF NOT EXISTS Concerts (
    concert_code INT AUTO_INCREMENT PRIMARY KEY,
    artist_code INT,
    venue_code INT,
    performer_name VARCHAR(50),
    genre VARCHAR(50),
    entry_restrictions ENUM('G', '18+'),
    start_time DATETIME,
    end_time DATETIME,
    tickets_available INT,
    total_seats INT,
    status ENUM('approved', 'cancelled', 'concluded'),
    FOREIGN KEY (artist_code) REFERENCES Artists(artist_code),
    FOREIGN KEY (venue_code) REFERENCES Venues(venue_code)
);

CREATE TABLE IF NOT EXISTS Customers (
    customer_code INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(50),
    contact_number VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS Transactions (
    transaction_code INT AUTO_INCREMENT PRIMARY KEY,
    customer_code INT,
    transaction_type ENUM('buy', 'refund', 'transfer', 'cancel'),
    transaction_status ENUM('open', 'closed'),
    transaction_date DATETIME,
    total_amount DECIMAL(10, 2),
    payment_method ENUM('cash', 'bank_transfer', 'card'),
    refund_reason VARCHAR(200),
    FOREIGN KEY (customer_code) REFERENCES Customers(customer_code)
);

CREATE TABLE IF NOT EXISTS Tickets (
    ticket_code INT AUTO_INCREMENT PRIMARY KEY,
    concert_code INT,
    customer_code INT,
    transaction_code INT,
    ticket_price DECIMAL(10, 2),
    seat_number INT,
    status ENUM('valid', 'refunded', 'transferred'),
    FOREIGN KEY (concert_code) REFERENCES Concerts(concert_code),
    FOREIGN KEY (customer_code) REFERENCES Customers(customer_code),
    FOREIGN KEY (transaction_code) REFERENCES Transactions(transaction_code)
);

INSERT INTO Companies (company_code, company_name, address_line1, address_line2, city, state, country, postal_code, company_email, company_contact_number, status) 
VALUES
(1, 'Philippine Music Entertainment', '123 Rizal Ave', 'Suite 400', 'Manila', 'Metro Manila', 'Philippines', '1000', 'contact@phmusicent.com', '09171234567', 'active'),
(2, 'Global Sound Records', '456 Sunset Blvd', '', 'Los Angeles', 'California', 'USA', '90001', 'info@globalsound.com', '18001234567', 'active');

INSERT INTO Artists (artist_code, company_code, stage_name, first_name, last_name, birth_date, email, contact_number, nationality) 
VALUES
(1, 1, 'Juan Dela Cruz', 'Juan', 'Dela Cruz', '1992-05-10', 'juan.dc@phmusicent.com', '09211234567', 'Filipino'),
(2, 2, 'Taylor Beats', 'Taylor', 'Adams', '1988-03-14', 'taylor.adams@globalsound.com', '+14151234567', 'American'),
(3, 2, 'Ado Hibana', 'Ado', 'Hibana', '1999-10-24', 'ado.hibana@globalsound.com', '+81312345678', 'Japanese');

INSERT INTO Venues (venue_code, venue_name, address_line1, street, city, province, barangay, postal_code, total_seating_capacity) 
VALUES
(1, 'Mall of Asia Arena', '123 Seaside Blvd', 'Entertainment City', 'Pasay', 'Metro Manila', 'Barangay 76', '1300', 15000),
(2, 'Araneta Coliseum', 'Araneta Center', 'Cubao', 'Quezon City', 'Metro Manila', 'Barangay Socorro', '1109', 20000);

INSERT INTO Concerts (concert_code, artist_code, venue_code, performer_name, genre, entry_restrictions, start_time, end_time, tickets_available, total_seats, status) 
VALUES
(1, 1, 1, 'Juan Dela Cruz Live', 'OPM', 'G', '2024-12-10 19:00:00', '2024-12-10 22:00:00', 14000, 15000, 'approved'),
(2, 2, 2, 'Taylor Beats World Tour', 'Pop', '18+', '2024-12-20 20:00:00', '2024-12-20 23:00:00', 18000, 20000, 'approved'),
(3, 3, 1, 'Ado Hibana World Tour 2025', 'J-Pop', 'G', '2025-05-08 18:00:00', '2025-05-08 23:59:00', 9999, 10000, 'approved');

INSERT INTO Customers (customer_code, first_name, last_name, email, contact_number) 
VALUES
(1, 'Pedro', 'Santos', 'pedro.santos@gmail.com', '09181234567'),
(2, 'Maria', 'Lopez', 'maria.lopez@gmail.com', '09181237890'),
(3, 'Max', 'Chavez', 'epicstrykermaxy@gmail.com', '09901327543');

INSERT INTO Transactions (transaction_code, customer_code, transaction_type, transaction_status, transaction_date, total_amount, payment_method, refund_reason) 
VALUES
(1, 1, 'buy', 'closed', '2024-11-15 14:30:00', 750.00, 'cash', NULL),
(2, 2, 'buy', 'closed', '2024-11-16 10:00:00', 1500.00, 'bank_transfer', NULL),
(3, 1, 'transfer', 'closed', '2024-11-17 14:30:00', 100.00, 'cash', NULL),
(4, 2, 'transfer', 'closed', '2024-11-17 10:00:00', 100.00, 'card', NULL),
(5, 3, 'refund', 'closed', '2024-11-19 10:00:00', 100.00, 'bank_transfer', 'wala lang');

INSERT INTO Tickets (ticket_code, concert_code, customer_code, transaction_code, ticket_price, seat_number, status) 
VALUES
(1, 1, 1, 1, 750.00, 101, 'valid'),
(2, 2, 3, 2, 1500.00, 202, 'refunded');
