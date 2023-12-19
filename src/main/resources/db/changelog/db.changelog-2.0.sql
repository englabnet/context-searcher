--liquibase formatted sql

--changeset nikitakuchur:2
ALTER TABLE video
    RENAME COLUMN video_id TO youtube_video_id;
