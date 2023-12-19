--liquibase formatted sql

--changeset nikitakuchur:1
ALTER TABLE video
    RENAME COLUMN video_id TO youtube_video_id;

--changeset nikitakuchur:2
CREATE TABLE IF NOT EXISTS indexed_video (
    id SERIAL NOT NULL PRIMARY KEY,
    index_name VARCHAR NOT NULL,
    youtube_video_id VARCHAR NOT NULL,
    variety VARCHAR NOT NULL,
    subtitles TEXT NOT NULL
)
