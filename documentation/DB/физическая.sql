-- 0. Включение расширения
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. ENUM-типы
CREATE TYPE gender_enum AS ENUM ('M', 'F');
CREATE TYPE contact_status AS ENUM ('PENDING', 'MATCHED', 'REJECTED'); 
CREATE TYPE promocode_status AS ENUM ('PENDING', 'ISSUED', 'ACTIVATED', 'EXPIRED');

-- 2. Таблица Пользователи 
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), 
    esia_id UUID NOT NULL UNIQUE, 
    first_name VARCHAR NOT NULL,
    last_name VARCHAR NOT NULL,
    birth_date DATE NOT NULL CHECK (birth_date <= CURRENT_DATE - INTERVAL '18 years'),
    gender gender_enum NOT NULL,
    city VARCHAR,
    bio TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 3. Таблица Фотографии
CREATE TABLE photos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    url VARCHAR NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_photos_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Таблица Контакты (Лайки и Мэтчи)
-- Используем составной первичный ключ!
CREATE TABLE contacts (
    initiator_id UUID NOT NULL, 
    responder_id UUID NOT NULL, 
    status contact_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    PRIMARY KEY (initiator_id, responder_id),
    
    CONSTRAINT fk_contact_initiator FOREIGN KEY (initiator_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_contact_responder FOREIGN KEY (responder_id) REFERENCES users(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_no_self_like CHECK (initiator_id <> responder_id)
);

-- 5. Таблица Сообщения (Магия от Кириллова)
CREATE TABLE messages (
    author_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    contact_initiator_id UUID NOT NULL,
    contact_responder_id UUID NOT NULL,
    text TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Первичный ключ для быстрого поиска по автору 
    PRIMARY KEY (author_id, created_at),
    
    -- Составной внешний ключ на Contacts
    CONSTRAINT fk_messages_contact FOREIGN KEY (contact_initiator_id, contact_responder_id) 
        REFERENCES contacts(initiator_id, responder_id) ON DELETE CASCADE,
        
    -- ТА САМАЯ МАГИЯ: автор должен быть одним из двух участников
    CONSTRAINT chk_author_is_participant CHECK (author_id = contact_initiator_id OR author_id = contact_responder_id)
);

-- 6. Таблица Партнеры 
CREATE TABLE partners (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    login VARCHAR UNIQUE NOT NULL,
    password_hash VARCHAR NOT NULL,
    contact_person VARCHAR NOT NULL,
    phone VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    city VARCHAR NOT NULL,
    address VARCHAR NOT NULL,
    contact_info VARCHAR
);

-- 7. Таблица Спецпредложения
CREATE TABLE offers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_id UUID NOT NULL,
    title VARCHAR NOT NULL,
    discount_info VARCHAR NOT NULL,
    valid_until DATE NOT NULL,
    photo_url VARCHAR NOT NULL,
    CONSTRAINT fk_ofers_partner FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE CASCADE
);

-- 8. Таблица Промокоды
CREATE TABLE promocodes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    offer_id UUID NOT NULL,
    contact_initiator_id UUID NOT NULL,
    contact_responder_id UUID NOT NULL,
    code VARCHAR UNIQUE NOT NULL,
    status promocode_status NOT NULL DEFAULT 'PENDING',
    activated_by_id UUID, 
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_promo_offer FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE CASCADE,
    CONSTRAINT fk_promo_contact FOREIGN KEY (contact_initiator_id, contact_responder_id) 
        REFERENCES contacts(initiator_id, responder_id) ON DELETE CASCADE,
    CONSTRAINT fk_promo_activated FOREIGN KEY (activated_by_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 9. ИНДЕКСЫ
CREATE INDEX idx_users_esia ON users(esia_id);
CREATE INDEX idx_users_city ON users(city);

CREATE INDEX idx_photos_user ON photos(user_id);

CREATE INDEX idx_contacts_initiator ON contacts(initiator_id);
CREATE INDEX idx_contacts_responder ON contacts(responder_id);


-- У Messages уже есть индекс благодаря PK (author_id, created_at), 
-- но для истории чата добавим индекс на связку контакта
CREATE INDEX idx_messages_contact ON messages(contact_initiator_id, contact_responder_id);

CREATE INDEX idx_partners_city ON partners(city);
CREATE INDEX idx_offers_partner ON offers(partner_id);
CREATE INDEX idx_offers_valid_until ON offers(valid_until);