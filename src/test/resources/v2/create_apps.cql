CREATE TABLE _keyspace.apps (
store_id text,
store text,
category text,
developer text,
title text,
icon text,
version text,
updated_date timestamp,
ingestion_time timestamp,
PRIMARY KEY (store_id, updated_date, version, store)
) WITH CLUSTERING ORDER BY (updated_date DESC, version DESC, store ASC)