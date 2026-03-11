CREATE TABLE card (
    id SERIAL PRIMARY KEY,
    card_number VARCHAR(16) UNIQUE NOT NULL,
    card_password VARCHAR(16) NOT NULL,
    balance DECIMAL(10,2) NOT NULL,
    version INT NOT NULL DEFAULT 0
);