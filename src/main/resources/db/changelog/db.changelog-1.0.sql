--liquibase formatted sql

--changeset nikitakuchur:1
CREATE TABLE IF NOT EXISTS VIDEO (
    id SERIAL NOT NULL PRIMARY KEY,
    video_id VARCHAR NOT NULL UNIQUE,
    variety VARCHAR NOT NULL,
    srt TEXT NOT NULL
)
