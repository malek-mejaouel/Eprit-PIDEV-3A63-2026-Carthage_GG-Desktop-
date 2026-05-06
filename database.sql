-- CarthageGG Database Schema (Accurate to DAO Layer)
CREATE DATABASE IF NOT EXISTS carthage_gg;
USE carthage_gg;

-- Users Table (Matches UserDAO)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    roles TEXT NOT NULL, -- JSON array
    username VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    avatar VARCHAR(255),
    google_id VARCHAR(255),
    discord_id VARCHAR(255),
    is_active TINYINT(1) DEFAULT 1,
    is_verified TINYINT(1) DEFAULT 0,
    verified_role_badge VARCHAR(50),
    verification_date DATETIME,
    banned_until DATETIME,
    ban_reason TEXT,
    last_login_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Games Table (Matches GameDAO)
CREATE TABLE IF NOT EXISTS games (
    game_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    genre VARCHAR(50),
    description TEXT
);

-- Tournaments Table (Matches TournamentDAO)
CREATE TABLE IF NOT EXISTS tournaments (
    tournament_id INT AUTO_INCREMENT PRIMARY KEY,
    tournament_name VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    prize_pool DECIMAL(10, 2),
    location VARCHAR(255),
    game_id INT,
    user_id INT,
    FOREIGN KEY (game_id) REFERENCES games(game_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Teams Table (Matches TeamDAO)
CREATE TABLE IF NOT EXISTS teams (
    team_id INT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL,
    logo VARCHAR(255),
    creation_date DATE,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Matches Table (Matches MatchDAO)
CREATE TABLE IF NOT EXISTS matches (
    match_id INT AUTO_INCREMENT PRIMARY KEY,
    match_date DATETIME,
    score_team_a INT DEFAULT 0,
    score_team_b INT DEFAULT 0,
    tournament_id INT,
    game_id INT,
    team_a_id INT,
    team_b_id INT,
    is_rivalry BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id),
    FOREIGN KEY (game_id) REFERENCES games(game_id),
    FOREIGN KEY (team_a_id) REFERENCES teams(team_id),
    FOREIGN KEY (team_b_id) REFERENCES teams(team_id)
);

-- News Table (Matches NewsDAO)
CREATE TABLE IF NOT EXISTS news (
    news_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    published_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    category_id INT
);

-- Comments Table (for News comments)
CREATE TABLE IF NOT EXISTS comments (
    commentaire_id INT AUTO_INCREMENT PRIMARY KEY,
    contenu TEXT NOT NULL,
    date_commentaire DATETIME DEFAULT CURRENT_TIMESTAMP,
    gif_url VARCHAR(255),
    upvotes INT DEFAULT 0,
    downvotes INT DEFAULT 0,
    news_id INT NOT NULL,
    user_id INT,
    FOREIGN KEY (news_id) REFERENCES news(news_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Event Table (Matches EventDAO - Note singular name)
CREATE TABLE IF NOT EXISTS event (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_at DATETIME,
    end_at DATETIME,
    location_id INT,
    max_seats INT
);

-- Products Table (Matches ProductDAO)
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category_id INT,
    stock INT DEFAULT 0,
    image VARCHAR(255)
);

-- Streams Table (Matches StreamDAO)
CREATE TABLE IF NOT EXISTS streams (
    stream_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    platform VARCHAR(50),
    channel_name VARCHAR(255),
    youtube_video_id VARCHAR(255),
    thumbnail VARCHAR(255),
    is_live TINYINT(1) DEFAULT 0,
    viewer_count INT DEFAULT 0,
    created_by INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- Locations (for Events)
CREATE TABLE IF NOT EXISTS locations (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    capacity INT DEFAULT 100,
    latitude DOUBLE DEFAULT 0.0,
    longitude DOUBLE DEFAULT 0.0
);

-- Reservations Table
DROP TABLE IF EXISTS reservation;
CREATE TABLE reservation (
	id INT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(255) NOT NULL COMMENT 'Full name of the person making the reservation',
	price DECIMAL(10, 2) NOT NULL COMMENT 'Price of the reservation',
	reservation_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Date when the reservation was created',
	event_id INT NOT NULL COMMENT 'Reference to the event being reserved',
	seats INT DEFAULT 1 COMMENT 'Number of seats reserved',
	status ENUM('WAITING', 'CONFIRMED', 'CANCELLED') DEFAULT 'WAITING' COMMENT 'Reservation status: WAITING (pending admin approval), CONFIRMED (approved), CANCELLED (rejected/cancelled)',
	user_id INT COMMENT 'Reference to the user who made the reservation',
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when reservation was created',
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp when reservation was last updated',
	FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
	FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
	INDEX idx_user_id (user_id),
	INDEX idx_event_id (event_id),
	INDEX idx_status (status)
);

-- Sample data for testing (optional)
INSERT INTO reservation (name, price, event_id, seats, status, user_id) VALUES
('Test Reservation 1', 50.00, 1, 2, 'WAITING', 1),
('Test Reservation 2', 75.50, 1, 1, 'CONFIRMED', 1);

-- Categories (for News/Products)
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Orders Table (Matches OrderDAO)
CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Reclamation Table
CREATE TABLE IF NOT EXISTS reclamation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    priority VARCHAR(20) DEFAULT 'normal',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Reclamation Messages Table
CREATE TABLE IF NOT EXISTS reclamation_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reclamation_id INT NOT NULL,
    sender_id INT NOT NULL,
    message TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reclamation_id) REFERENCES reclamation(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Reservations Table (Matches ReservationDAO)
CREATE TABLE IF NOT EXISTS reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    reservation_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    event_id INT NOT NULL,
    status ENUM('WAITING', 'CONFIRMED', 'CANCELLED') DEFAULT 'WAITING',
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE
);

-- Insert Admin User (Password: admin123)
INSERT INTO users (email, password, roles, username, first_name, last_name, is_active)
VALUES ('admin@carthagegg.tn', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GdR00N.N0f2W', '["ROLE_ADMIN"]', 'admin', 'Admin', 'CarthageGG', 1);

-- Updates for Rivalry Detection
ALTER TABLE matches ADD COLUMN IF NOT EXISTS is_rivalry BOOLEAN DEFAULT FALSE;
