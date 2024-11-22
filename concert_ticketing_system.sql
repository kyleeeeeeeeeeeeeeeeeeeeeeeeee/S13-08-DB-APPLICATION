DROP DATABASE IF EXISTS `concerttix`;
CREATE DATABASE IF NOT EXISTS `concerttix`;
USE `concerttix`;

CREATE TABLE IF NOT EXISTS Companies (
    company_code INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(50),
    address VARCHAR(100),
    city VARCHAR(50),
    state VARCHAR(50),
    country VARCHAR(50),
    postal_code VARCHAR(10),
    company_email VARCHAR(50),
    contact_number VARCHAR(15),
    status ENUM('active', 'dissolved')
);

CREATE TABLE IF NOT EXISTS Artists (
    artist_code INT AUTO_INCREMENT PRIMARY KEY,
    company_code INT,
    stage_name VARCHAR(50),
    genre VARCHAR(50),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    birth_date DATE,
    email VARCHAR(50),
    contact_number VARCHAR(15),
    nationality VARCHAR(50),
    FOREIGN KEY (company_code) REFERENCES Companies(company_code) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Venues (
    venue_code INT AUTO_INCREMENT PRIMARY KEY,
    venue_name VARCHAR(50),
    street VARCHAR(50),
    city VARCHAR(50),
    province VARCHAR(50),
    barangay VARCHAR(50),
    postal_code VARCHAR(10),
    total_seating_capacity INT CHECK (total_seating_capacity > 0)
);

CREATE TABLE IF NOT EXISTS Concerts (
    concert_code INT AUTO_INCREMENT PRIMARY KEY,
    artist_code INT,
    venue_code INT,
	concert_title VARCHAR(50),
    performer_name VARCHAR(50),
    entry_restrictions ENUM('G', '18+'),
    concert_date DATE,
    tickets_available INT,
    seating_capacity INT,
    status ENUM('approved', 'cancelled', 'concluded'),
    FOREIGN KEY (artist_code) REFERENCES Artists(artist_code) ON DELETE CASCADE,
    FOREIGN KEY (venue_code) REFERENCES Venues(venue_code) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS AvailableVenues (
    venue_code INT NOT NULL,
    concert_date DATE NOT NULL,
    availability ENUM('available', 'booked') NOT NULL,
    PRIMARY KEY (venue_code, concert_date), 
    FOREIGN KEY (venue_code) REFERENCES Venues(venue_code) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Customers (
    customer_code INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    birth_date DATE,
    email VARCHAR(50) UNIQUE,
    contact_number VARCHAR(15)
);

CREATE TABLE Bans (
	ban_code INT AUTO_INCREMENT PRIMARY KEY,
    customer_code INT NOT NULL,
    ban_reason VARCHAR(100) NOT NULL,
    FOREIGN KEY (customer_code) REFERENCES Customers(customer_code)
);

CREATE TABLE IF NOT EXISTS Transactions (
    transaction_code INT AUTO_INCREMENT PRIMARY KEY,
    customer_code INT,
    transaction_type ENUM('buy', 'refund', 'transfer', 'cancel'),
    transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2),
    payment_method ENUM('cash', 'bank_transfer', 'card'),
    FOREIGN KEY (customer_code) REFERENCES Customers(customer_code) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Prices (
    ticket_type VARCHAR(50),
    concert_code INT,
    price DECIMAL(10, 2),
    PRIMARY KEY (ticket_type, concert_code),
    FOREIGN KEY (concert_code) REFERENCES Concerts(concert_code) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Tickets (
    ticket_code INT AUTO_INCREMENT PRIMARY KEY,
    concert_code INT,
    transaction_code INT,
    ticket_type VARCHAR(50),
    seat_number VARCHAR(10),
    ticket_price DECIMAL(10, 2),
    FOREIGN KEY (concert_code) REFERENCES Concerts(concert_code) ON DELETE CASCADE,
    FOREIGN KEY (transaction_code) REFERENCES Transactions(transaction_code) ON DELETE CASCADE,
    UNIQUE (concert_code, seat_number)
);

CREATE TABLE IF NOT EXISTS Refunds (
    refund_code INT AUTO_INCREMENT PRIMARY KEY,
    transaction_code INT,
    FOREIGN KEY (transaction_code) REFERENCES Transactions(transaction_code) ON DELETE CASCADE
);










-- Populate Companies table
INSERT INTO Companies (company_name, address, city, state, country, postal_code, company_email, contact_number, status)
VALUES
('Star Music', 'ABS-CBN Broadcasting Center, Sgt. Esguerra Ave', 'Quezon City', 'Metro Manila', 'Philippines', '1103', 'info@starmusic.ph', '09171234567', 'active'),
('Viva Records', 'Viva Communications Inc., Ortigas Center', 'Pasig City', 'Metro Manila', 'Philippines', '1605', 'contact@vivarecords.ph', '09182234567', 'active'),
('PolyEast Records', '6767 Ayala Avenue', 'Makati City', 'Metro Manila', 'Philippines', '1226', 'support@polyeast.ph', '09173234567', 'active'),
('Universal Records Philippines', 'Universal Tower, Quezon Ave', 'Quezon City', 'Metro Manila', 'Philippines', '1103', 'help@universalrecords.ph', '09184234567', 'active'),
('Sony Music Philippines', '7th Floor, Ecoplaza Building, Chino Roces Ave Ext', 'Makati City', 'Metro Manila', 'Philippines', '1231', 'sonyph@music.ph', '09195234567', 'active'),
('Tarsier Records', 'ABS-CBN Compound, Mother Ignacia Ave', 'Quezon City', 'Metro Manila', 'Philippines', '1103', 'tarsier@music.ph', '09206234567', 'active'),
('Warner Music Philippines', '26/F, Ayala North Exchange Tower 2, Ayala Avenue', 'Makati City', 'Metro Manila', 'Philippines', '1225', 'warnerph@music.ph', '09217234567', 'active'),
('Ivory Music and Video', 'Unit 1402, Tycoon Centre, Pearl Drive', 'Pasig City', 'Metro Manila', 'Philippines', '1605', 'ivory@music.ph', '09228234567', 'active'),
('Offshore Music', '24K Tower, Emerald Ave', 'Pasig City', 'Metro Manila', 'Philippines', '1605', 'offshore@music.ph', '09239234567', 'active'),
('Curve Entertainment', 'Room 204, Emerald Mansion, Emerald Ave', 'Pasig City', 'Metro Manila', 'Philippines', '1605', 'curve@music.ph', '09240234567', 'active');

-- Populate Artists table
INSERT INTO Artists (company_code, stage_name, genre, first_name, last_name, birth_date, email, contact_number, nationality)
VALUES
(10, 'Moira Dela Torre', 'Pop', 'Moira', 'Dela Torre', '1993-11-04', 'moira@starmusic.ph', '09171234567', 'Filipino'),
(1, 'BINI', 'Folk/Pop', 'N/A', 'N/A', '1991-05-14', 'biniverse@abscbn.ph', '09182234567', 'Filipino'),
(7, 'Ado', 'J-Pop', 'Unknown', 'Unknown', '2002-10-24', 'ado@universalrecords.jp', '09381234567', 'Japanese'),
(3, 'IV of Spades', 'Alternative', 'Zild', 'Benitez', '1997-04-23', 'ivspades@polyeast.ph', '09173234567', 'Filipino'),
(4, 'LANY', 'Indie Pop', 'Paul', 'Klein', '1988-04-30', 'lany@warnerph.com', '09206234567', 'American'),
(9, 'BLACKPINK', 'K-Pop', 'N/A', 'N/A', '1995-01-03', 'blackpink@universalrecords.kr', '09217234567', 'Korean'),
(5, 'Taylor Swift', 'Pop', 'Taylor', 'Swift', '1989-12-13', 'taylor@universalmusic.com', '09195234567', 'American'),
(8, 'SB19', 'P-Pop', 'N/A', 'N/A', '1996-06-16', 'sb19@ivorymusic.ph', '09181234567', 'Filipino'),
(2, 'Ariana Grande', 'Pop/R&B', 'Ariana', 'Grande', '1993-06-26', 'ariana@warnerph.com', '09239234567', 'American'),
(6, 'Eraserheads', 'Rock', 'N/A', 'N/A', '1970-11-02', 'eraserheads@offshore.ph', '09240234567', 'Filipino');

-- Populate Venues table
INSERT INTO Venues (venue_name, street, city, province, barangay, postal_code, total_seating_capacity)
VALUES
('Mall of Asia Arena', 'Seaside Blvd', 'Pasay', 'Metro Manila', 'Barangay 76', '1300', 20000),
('Araneta Coliseum', 'General Araneta St', 'Quezon', 'Metro Manila', 'Barangay Socorro', '1109', 16500),
('Cebu Coliseum', 'Osmeña Blvd', 'Cebu City', 'Cebu', 'Barangay Santo Niño', '6000', 10000),
('SMX Convention Center', 'SM City Lanang', 'Davao', 'Davao del Sur', 'Barangay Buhangin', '8000', 12000),
('Philippine Arena', 'Philippine Arena Access Road', 'Bocaue', 'Bulacan', 'Barangay Santa Maria', '3018', 55000),
('Smart Araneta Coliseum', 'General Malvar St', 'Quezon', 'Metro Manila', 'Barangay Socorro', '1109', 16000),
('New Frontier Theater', 'General Aguinaldo Ave', 'Quezon', 'Metro Manila', 'Barangay Socorro', '1109', 2500),
('PICC Plenary Hall', 'CCP Complex, Roxas Blvd', 'Pasay', 'Metro Manila', 'Barangay 13', '1307', 3500),
('Waterfront Hotel and Casino', 'Salinas Dr', 'Cebu', 'Cebu', 'Barangay Lahug', '6000', 6000),
('De La Salle University - Yuchengco Hall', '2401 Taft Ave', 'Manila', 'Metro Manila', 'Barangay 38', '1004', 1500),
('Bahay ni Billy', '18 Eugenio Lopez Jr. Dr', 'Quezon', 'Metro Manila', 'Diliman', '1101', 3);

-- Populate Concerts table
INSERT INTO Concerts (artist_code, venue_code, concert_title, performer_name, entry_restrictions, concert_date, tickets_available, seating_capacity, status)
VALUES
(1, 11, 'Private Time with Moira', 'Moira Dela Torre', 'G', '2024-12-01', 0, 3, 'approved'),
(2, 2, 'Folk Waves Live', 'Ben&Ben', 'G', '2024-12-10', 12000, 12000, 'approved'),
(3, 5, 'Kaikai Kitan Night', 'Ado', '18+', '2025-01-15', 50000, 50000, 'approved'),
(4, 1, 'Alt-Jam Session', 'IV of Spades', 'G', '2024-12-05', 18000, 18000, 'approved'),
(5, 6, 'Indie Chill Manila', 'LANY', 'G', '2024-11-25', 14000, 14000, 'approved'),
(6, 7, 'Pink World Tour', 'BLACKPINK', '18+', '2024-12-20', 2400, 2400, 'approved'),
(7, 4, 'Evermore Night', 'Taylor Swift', 'G', '2025-01-10', 12000, 12000, 'approved'),
(8, 3, 'P-Pop Kings Live', 'SB19', 'G', '2024-12-08', 10000, 10000, 'approved'),
(9, 9, 'Grande Celebration', 'Ariana Grande', '18+', '2024-12-18', 5000, 5000, 'approved'),
(10, 10, 'E-Heads Reunion', 'Eraserheads', 'G', '2024-12-30', 4500, 4500, 'approved'),
(1, 1, 'Acoustic Night with Moira', 'Moira Dela Torre', 'G', '2025-01-12', 1400, 1400, 'approved'),
(4, 8, 'Alt Revival', 'IV of Spades', 'G', '2024-11-28', 3000, 3000, 'approved'),
(5, 3, 'Pacific Indie Night', 'LANY', 'G', '2024-12-15', 9500, 9500, 'approved'),
(7, 2, 'Red Concert Series', 'Taylor Swift', '18+', '2025-01-20', 16000, 16000, 'approved'),
(6, 5, 'Pink Encore', 'BLACKPINK', '18+', '2024-11-30', 48000, 48000, 'approved'),
(10, 6, 'Rock Back in Time', 'Eraserheads', 'G', '2025-01-05', 14000, 14000, 'approved'),
(8, 7, 'P-Pop Manila Night', 'SB19', 'G', '2024-12-22', 2400, 2400, 'approved'),
(9, 4, 'Sweetener Live', 'Ariana Grande', '18+', '2025-01-25', 12000, 12000, 'approved'),
(3, 8, 'Vocal Alchemy', 'Ado', '18+', '2024-12-02', 3000, 3000, 'approved'),
(2, 9, 'Harmony and Rhythms', 'Ben&Ben', 'G', '2024-12-16', 5000, 5000, 'approved');

-- Populate AvailableVenues table
INSERT INTO AvailableVenues (venue_code, concert_date, availability)
VALUES
(11, '2024-12-01', 'booked'),
(2, '2024-12-10', 'booked'),
(5, '2025-01-15', 'booked'),
(1, '2024-12-05', 'booked'),
(6, '2024-11-25', 'booked'),
(7, '2024-12-20', 'booked'),
(4, '2025-01-10', 'booked'),
(3, '2024-12-08', 'booked'),
(9, '2024-12-18', 'booked'),
(10, '2024-12-30', 'booked'),
(1, '2025-01-12', 'booked'),
(8, '2024-11-28', 'booked'),
(3, '2024-12-15', 'booked'),
(2, '2025-01-20', 'booked'),
(5, '2024-11-30', 'booked'),
(6, '2025-01-05', 'booked'),
(7, '2024-12-22', 'booked'),
(4, '2025-01-25', 'booked'),
(8, '2024-12-02', 'booked'),
(9, '2024-12-16', 'booked');

-- Populate Customers table
INSERT INTO Customers (first_name, last_name, birth_date, email, contact_number)
VALUES
('Juan', 'Dela Cruz', '1990-01-01', 'juandelacruz@example.com', '09123456789'),
('Maria', 'Santos', '1985-05-15', 'mariasantos@example.com', '09234567890'),
('Pedro', 'Gonzales', '1978-08-20', 'pedrogonzales@example.com', '09345678901'),
('Liza', 'Manzano', '1992-03-10', 'lizamanzano@example.com', '09456789012'),
('Carlos', 'Ramos', '1980-06-25', 'carlosramos@example.com', '09567890123'),
('Anna', 'Lim', '1989-02-28', 'annalim@example.com', '09678901234'),
('Miguel', 'Castillo', '1995-09-05', 'miguelcastillo@example.com', '09789012345'),
('Karla', 'Santiago', '1987-11-30', 'karlasantiago@example.com', '09890123456'),
('Jake', 'Navarro', '1993-07-12', 'jakenavarro@example.com', '09901234567'),
('Jasmine', 'Tan', '1991-04-22', 'jasminetan@example.com', '09012345678'),
('Skall', 'Perr', '1989-11-01', 'proscalper1000@example.com', '09103454567');

-- Populate Bans table
INSERT INTO Bans (customer_code, ban_reason)
VALUES
(11, 'scalping tickets');

-- Populate Transactions table
INSERT INTO Transactions (customer_code, transaction_type, transaction_date, total_amount, payment_method)
VALUES
(1, 'buy', '2024-11-20 14:30:00', 2500.00, 'card'),
(2, 'buy', '2024-11-21 10:15:00', 1500.00, 'cash'),
(3, 'buy', '2024-11-22 11:00:00', 1200.00, 'bank_transfer'),
(4, 'buy', '2024-11-23 16:45:00', 3500.00, 'card'),
(5, 'buy', '2024-11-24 09:20:00', 2000.00, 'cash'),
(6, 'buy', '2024-11-25 08:00:00', 1800.00, 'card'),
(7, 'buy', '2024-11-26 18:25:00', 3000.00, 'bank_transfer'),
(8, 'buy', '2024-11-27 14:00:00', 1800.00, 'cash'),
(9, 'buy', '2024-11-28 13:30:00', 2200.00, 'card'),
(10, 'buy', '2024-11-29 17:10:00', 30000.00, 'bank_transfer'),
(1, 'buy', '2024-11-29 17:10:01', 30000.00, 'bank_transfer'),
(2, 'buy', '2024-11-29 17:10:02', 30000.00, 'bank_transfer'),
(3, 'refund', '2024-11-23 14:30:00', 120.00, 'card');

-- Populate Prices table with detailed ticket tiers for each concert
INSERT INTO Prices (ticket_type, concert_code, price)
VALUES
('Private Show', 1, 30000.00),
('VIP', 2, 6000.00), 
('Lower Box', 2, 3000.00),
('Upper Box', 2, 1500.00),
('General Admission', 2, 1000.00),
('VIP 1', 3, 10200.00),
('VIP 2', 3, 10200.00),
('VIP 3', 3, 10200.00),
('Lower Box', 3, 5000.00),
('Upper Box', 3, 2500.00),
('General Admission', 3, 1500.00),
('VIP', 4, 5000.00),
('Lower Box', 4, 2200.00),
('Upper Box', 4, 1500.00),
('Standing', 4, 800.00),
('VIP 1', 5, 6500.00),
('VIP 2', 5, 4500.00),
('Lower Box', 5, 3500.00),
('Upper Box', 5, 1800.00),
('General Admission', 5, 1200.00),
('Backstage', 6, 10000.00), 
('Seated', 6, 7000.00),
('Lower Box', 6, 5000.00),
('Upper Box', 6, 3500.00),
('General Admission', 6, 2000.00),
('Platinum', 7, 8000.00), 
('Gold', 7, 6000.00),
('Silver', 7, 4500.00),
('Bronze', 7, 2500.00),
('Tier 1', 8, 6000.00), 
('Tier 2', 8, 4000.00),
('Tier 3', 8, 3000.00),
('Seated', 9, 7500.00), 
('Standing', 9, 1000.00),
('Rakrakan', 10, 6000.00), 
('Tambayan', 10, 4500.00),
('VIP 1', 11, 3000.00),
('VIP 2', 11, 2200.00),
('Lower Box', 11, 1500.00),
('Upper Box', 11, 800.00),
('General Admission', 11, 500.00),
('General Admission', 12, 5000.00), 
('Meet n Greet', 13, 6500.00),
('Premium', 13, 4500.00),
('Economy', 13, 3500.00),
('VIP', 14, 7000.00),
('Not so VIP', 14, 5000.00),
('Just a P', 14, 4000.00),
('Barely a P', 14, 2500.00),
('VIP 1', 15, 12000.00),
('VIP 2', 15, 8500.00),
('Lower Box', 15, 6000.00),
('Upper Box', 15, 4000.00),
('General Admission', 15, 2500.00),
('Lower Box', 16, 3000.00),
('Upper Box', 16, 1800.00),
('General Admission', 16, 1000.00),
('VIP 1', 17, 7000.00), 
('VIP 2', 17, 5000.00),
('VIP 3', 17, 4000.00),
('Hibana 1', 18, 6000.00), 
('Hibana 2', 18, 6000.00),
('Hibana 3', 18, 6000.00),
('Private', 19, 100000.00),
('VIP 1', 20, 6000.00),
('VIP 2', 20, 4000.00),
('Lower Box', 20, 3000.00),
('Upper Box', 20, 1800.00),
('General Admission', 20, 1000.00);

-- Populate Tickets table
INSERT INTO Tickets (concert_code, transaction_code, ticket_type, seat_number, ticket_price)
VALUES
(5, 1, 'Lower Box', 'LB245', 3500.00),
(6, 2, 'Backstage', 'BS10', 10000.00),
(14, 13, 'Not so VIP', 'NVIP42', 5000.00),
(10, 4, 'Tambayan', 'AA1', 4500.00),
(10, 5, 'Rakrakan', 'A1', 6000.00),
(7, 6, 'Platinum', 'C6', 8000.00),
(13, 7, 'Meet n Greet', 1, 6500.00),
(20, 8, 'General Admission', '64B', 1000.00),
(18, 9, 'Hibana 2', '17I', 6000.00),
(1, 10, 'VIP', '1', 30000.00),
(1, 11, 'VIP', '2', 30000.00),
(1, 12, 'VIP', '3', 30000.00);

-- Populate Refunds table
INSERT INTO Refunds (transaction_code)
VALUES
(3);
