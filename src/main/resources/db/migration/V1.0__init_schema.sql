-- ============================================
-- AI-Langchain4j Database Migration
-- Version: release_1.0
-- Date: 2026-02-09
-- Description: Initial schema setup
-- ============================================

-- 1. Create chat_messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for session_id lookup
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id ON chat_messages(session_id);

-- 2. Create embeddings table (for PgVector)
-- Note: Requires pgvector extension to be installed
-- CREATE EXTENSION IF NOT EXISTS vector;

-- CREATE TABLE IF NOT EXISTS embeddings (
--     embedding_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
--     embedding VECTOR(1536),
--     text TEXT,
--     metadata JSONB
-- );
