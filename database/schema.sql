-- CSE202 Banking System: MySQL schema (created automatically by the program; kept here for the report)
CREATE DATABASE IF NOT EXISTS bank_db;
USE bank_db;

CREATE TABLE IF NOT EXISTS customers (
    customer_id   VARCHAR(10)  NOT NULL PRIMARY KEY,
    first_name    VARCHAR(60)  NULL,
    surname       VARCHAR(60)  NULL,
    company_name  VARCHAR(120) NULL,
    address       VARCHAR(200) NOT NULL,
    phone         VARCHAR(30)  NULL,
    email         VARCHAR(120) NULL,
    employed      BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS accounts (
    account_number      VARCHAR(12)   NOT NULL PRIMARY KEY,
    account_type        VARCHAR(12)   NOT NULL,
    customer_id         VARCHAR(10)   NOT NULL,
    branch              VARCHAR(80)   NOT NULL,
    date_opened         DATE          NOT NULL,
    balance             DECIMAL(15,2) NOT NULL,
    last_interest_month CHAR(7)       NULL,
    employer_name       VARCHAR(120)  NULL,
    employer_address    VARCHAR(200)  NULL,
    FOREIGN KEY (customer_id) REFERENCES customers (customer_id)
);

CREATE TABLE IF NOT EXISTS transactions (
    seq            BIGINT        NOT NULL AUTO_INCREMENT UNIQUE,
    transaction_id CHAR(36)      NOT NULL PRIMARY KEY,
    account_number VARCHAR(12)   NOT NULL,
    tx_type        VARCHAR(12)   NOT NULL,
    amount         DECIMAL(15,2) NOT NULL,
    balance_after  DECIMAL(15,2) NOT NULL,
    tx_time        DATETIME(6)   NOT NULL,
    reference      VARCHAR(100)  NULL,
    FOREIGN KEY (account_number) REFERENCES accounts (account_number)
);

CREATE TABLE IF NOT EXISTS users (
    username      VARCHAR(40)  NOT NULL PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(10)  NOT NULL,
    customer_id   VARCHAR(10)  NULL,
    FOREIGN KEY (customer_id) REFERENCES customers (customer_id)
);
