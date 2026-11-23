CREATE TABLE esia_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    login VARCHAR UNIQUE NOT NULL, 
    password_hash VARCHAR NOT NULL,
    first_name VARCHAR NOT NULL,
    last_name VARCHAR NOT NULL,
    birth_date DATE NOT NULL , 
    gender gender_enum NOT NULL -- Используем  ENUM, который был создан ранее (CREATE TYPE gender_enum AS ENUM ('M', 'F');)

);