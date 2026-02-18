CREATE TABLE ingestion_job
(
    id          uuid not null,
    status      text not null,
    started_at  timestamptz,
    finished_at timestamptz,
    primary key (id)
);

CREATE TABLE processed_entries
(
    id           uuid not null,
    entry_name   text not null,
    sender_email text not null,
    primary key (id)
);

create index idx_processed_entries_sender
    on processed_entries (sender_email);

INSERT INTO ingestion_job (id, status)
VALUES ('9359e5d1-e303-46b3-b7d8-0dce60ffa7f7', 'IDLE');
