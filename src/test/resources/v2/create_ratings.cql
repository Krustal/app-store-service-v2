CREATE TABLE _keyspace.ratings (
store_id text,
store text,
region text,
ingest_time timestamp,
store_observed_time timestamp,
all_ratings_histogram map<int, bigint>,
all_ratings_average double,
all_ratings_count bigint,
curr_ratings_histogram map<int, bigint>,
curr_ratings_average double,
curr_ratings_count bigint,
version text,
PRIMARY KEY ((store_id, store), ingest_time, region)
) WITH CLUSTERING ORDER BY (ingest_time DESC, region DESC)