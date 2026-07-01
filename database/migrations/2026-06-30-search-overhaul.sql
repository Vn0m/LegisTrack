-- Search overhaul migration
-- Run this in the Supabase SQL editor on any database created before the search
-- overhaul. Fresh installs from schema.sql already include everything below.
-- Every statement is idempotent, so it is safe to re-run.

-- Chamber/committee/status filters read committee_name from the local bills table.
ALTER TABLE bills ADD COLUMN IF NOT EXISTS committee_name VARCHAR(100);

-- Semantic search relies on pgvector. These are already in schema.sql but are
-- repeated here idempotently for older databases that predate vector search.
CREATE EXTENSION IF NOT EXISTS vector;

CREATE INDEX IF NOT EXISTS bills_content_embedding_idx
    ON bills USING ivfflat (content_embedding vector_cosine_ops);
