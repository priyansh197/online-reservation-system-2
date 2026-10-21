-- Online Reservation System schema (MySQL 8.0.16+ for CHECK constraints)
CREATE DATABASE IF NOT EXISTS online_reservation
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE online_reservation;

CREATE TABLE IF NOT EXISTS users (
  user_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name     VARCHAR(100) NOT NULL,
  username      VARCHAR(50)  NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_users_username UNIQUE (username),
  CONSTRAINT ck_users_role CHECK (role IN ('USER','ADMIN'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS trains (
  train_number        INT PRIMARY KEY,
  train_name          VARCHAR(120) NOT NULL,
  source_station      VARCHAR(100) NOT NULL,
  destination_station VARCHAR(100) NOT NULL,
  active              BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT ck_trains_name CHECK (CHAR_LENGTH(TRIM(train_name)) > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reservations (
  reservation_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  pnr                 VARCHAR(20)  NOT NULL,
  user_id             BIGINT       NOT NULL,
  passenger_name      VARCHAR(100) NOT NULL,
  train_number        INT          NOT NULL,
  class_type          VARCHAR(30)  NOT NULL,
  journey_date        DATE         NOT NULL,
  source_station      VARCHAR(100) NOT NULL,
  destination_station VARCHAR(100) NOT NULL,
  booking_status      VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
  created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_pnr UNIQUE (pnr),
  CONSTRAINT fk_res_user  FOREIGN KEY (user_id)      REFERENCES users(user_id),
  CONSTRAINT fk_res_train FOREIGN KEY (train_number) REFERENCES trains(train_number),
  CONSTRAINT ck_res_status CHECK (booking_status IN ('CONFIRMED','CANCELLED')),
  INDEX idx_res_user (user_id),
  INDEX idx_res_train (train_number),
  INDEX idx_res_date (journey_date)
) ENGINE=InnoDB;

-- Cancellation: the row is DELETEd from reservations (as the internship brief asks)
-- and archived here in the same transaction so history is not lost.
CREATE TABLE IF NOT EXISTS cancelled_reservations (
  cancel_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  pnr                 VARCHAR(20)  NOT NULL,
  user_id             BIGINT       NOT NULL,
  passenger_name      VARCHAR(100) NOT NULL,
  train_number        INT          NOT NULL,
  class_type          VARCHAR(30)  NOT NULL,
  journey_date        DATE         NOT NULL,
  source_station      VARCHAR(100) NOT NULL,
  destination_station VARCHAR(100) NOT NULL,
  booked_at           TIMESTAMP    NULL,
  cancelled_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_cancel_pnr (pnr)
) ENGINE=InnoDB;

-- Optional: least-privilege application account (change the password!)
-- CREATE USER 'reservation_app'@'localhost' IDENTIFIED BY 'change_me';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON online_reservation.* TO 'reservation_app'@'localhost';
