
CREATE TABLE Companies (
    company_code VARCHAR(10) PRIMARY KEY,
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

CREATE TABLE Artists (
    artist_code VARCHAR(10) PRIMARY KEY,
    company_code VARCHAR(10),
    stage_name VARCHAR(50),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    actual_name VARCHAR(50),
    birth_date DATETIME,
    email VARCHAR(50),
    contact_number VARCHAR(15),
    nationality VARCHAR(50),
    FOREIGN KEY (company_code) REFERENCES Companies(company_code)
);

CREATE TABLE Venues (
    venue_code VARCHAR(10) PRIMARY KEY,
    venue_name VARCHAR(50),
    address_line1 VARCHAR(50),
    street VARCHAR(50),
    city VARCHAR(50),
    province VARCHAR(50),
    barangay VARCHAR(50),
    postal_code VARCHAR(10),
    total_seating_capacity INT,
    total_available_seats INT
);

CREATE TABLE Concerts (
    concert_code VARCHAR(10) PRIMARY KEY,
    artist_code VARCHAR(10),
    venue_code VARCHAR(10),
    performer_name VARCHAR(50),
    genre VARCHAR(50),
    entry_restrictions ENUM('G', '18+'),
    start_time DATETIME,
    end_time DATETIME,
    tickets_available INT,
    total_seats INT,
    status ENUM('approved', 'cancelled'),
    FOREIGN KEY (artist_code) REFERENCES Artists(artist_code),
    FOREIGN KEY (venue_code) REFERENCES Venues(venue_code)
);

CREATE TABLE Customers (
    customer_code VARCHAR(10) PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(50),
    contact_number VARCHAR(15)
);

CREATE TABLE Transactions (
    transaction_code VARCHAR(10) PRIMARY KEY,
    customer_code VARCHAR(10),
    transaction_type ENUM('buy', 'refund', 'transfer', 'cancel'),
    transaction_status ENUM('open', 'closed'),
    transaction_date DATETIME,
    total_amount INT,
    payment_method ENUM('cash', 'bank_transfer', 'card'),
    refund_reason VARCHAR(200),
    FOREIGN KEY (customer_code) REFERENCES Customers(customer_code)
);

CREATE TABLE Tickets (
    ticket_code VARCHAR(10) PRIMARY KEY,
    concert_code VARCHAR(10),
    customer_code VARCHAR(10),
    transaction_code VARCHAR(10),
    ticket_price INT,
    seat_number INT,
    status ENUM('valid', 'refunded', 'transferred'),
    FOREIGN KEY (concert_code) REFERENCES Concerts(concert_code),
    FOREIGN KEY (customer_code) REFERENCES Customers(customer_code),
    FOREIGN KEY (transaction_code) REFERENCES Transactions(transaction_code)
);
