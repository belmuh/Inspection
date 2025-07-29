-- schema.sql

-- Drop tables if they exist to allow for clean re-creation (useful for development/testing)
DROP TABLE IF EXISTS inspection_photos;
DROP TABLE IF EXISTS inspection_answers;
DROP TABLE IF EXISTS inspections;
DROP TABLE IF EXISTS questions;

-- 1. questions table
CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY, -- BIGSERIAL for auto-incrementing BIGINT primary key
    question_text VARCHAR(500) NOT NULL UNIQUE, -- Question text, cannot be null, must be unique
    order_index INTEGER NOT NULL, -- Order of the question, cannot be null
    is_active BOOLEAN NOT NULL DEFAULT TRUE, -- Is the question active? Default to true
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP -- Timestamp of creation
);

-- 2. inspections table
CREATE TABLE inspections (
    id BIGSERIAL PRIMARY KEY, -- BIGSERIAL for auto-incrementing BIGINT primary key
    car_id VARCHAR(100) NOT NULL, -- ID of the car, cannot be null
    inspection_date TIMESTAMP WITH TIME ZONE NOT NULL, -- Date and time of the inspection
    status VARCHAR(20) NOT NULL, -- Status of the inspection (e.g., 'COMPLETED', 'IN_PROGRESS')
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP -- Timestamp of record creation
);

-- Add an index on car_id for faster lookups
CREATE INDEX idx_inspections_car_id ON inspections (car_id);
-- Add a unique constraint to ensure only one question text is allowed
-- (This might be redundant if the Question entity handles it, but good for DB level)
-- ALTER TABLE questions ADD CONSTRAINT unique_question_text UNIQUE (question_text);


-- 3. inspection_answers table
CREATE TABLE inspection_answers (
    id BIGSERIAL PRIMARY KEY, -- BIGSERIAL for auto-incrementing BIGINT primary key
    inspection_id BIGINT NOT NULL, -- Foreign key to inspections table
    question_id BIGINT NOT NULL, -- Foreign key to questions table
    answer VARCHAR(10) NOT NULL, -- The answer ('YES' or 'NO'), cannot be null
    description TEXT, -- Optional description for the answer
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Timestamp of creation

    -- Foreign key constraints
    CONSTRAINT fk_inspection_answers_inspection FOREIGN KEY (inspection_id) REFERENCES inspections(id) ON DELETE CASCADE,
    CONSTRAINT fk_inspection_answers_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE RESTRICT,

    -- Ensure a question is answered only once per inspection
    UNIQUE (inspection_id, question_id)
);

-- 4. inspection_photos table
CREATE TABLE inspection_photos (
    id BIGSERIAL PRIMARY KEY, -- BIGSERIAL for auto-incrementing BIGINT primary key
    answer_id BIGINT NOT NULL, -- Foreign key to inspection_answers table
    photo_url VARCHAR(500) NOT NULL, -- URL of the photo, cannot be null
    is_new BOOLEAN NOT NULL, -- Is this a newly taken photo?
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Timestamp when the photo was uploaded

    -- Foreign key constraint
    CONSTRAINT fk_inspection_photos_answer FOREIGN KEY (answer_id) REFERENCES inspection_answers(id) ON DELETE CASCADE
);