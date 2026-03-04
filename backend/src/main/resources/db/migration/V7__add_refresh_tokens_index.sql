-- Add index on refresh_tokens.user_id to speed up lookups during logout
-- (revoke all tokens for a given user) and token rotation.
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
