CREATE TABLE _keyspace.current_version_apps (
store_id text,
store text,
category text,
developer text,
title text,
icon text,
version text,
updated_date timestamp,
ingestion_time timestamp,
PRIMARY KEY (store_id, store, version)
) WITH CLUSTERING ORDER BY (store ASC, version DESC)