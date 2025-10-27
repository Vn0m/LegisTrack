CREATE TABLE bills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    base_print_no_str VARCHAR(100) UNIQUE NOT NULL, 
    title TEXT NOT NULL,
    summary TEXT,
    memo TEXT, -- For AI summarization
    chamber VARCHAR(20) NOT NULL, 
    year INTEGER,
    sponsor_name VARCHAR(255), 
    status VARCHAR(100), -- Current status
    published_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    content_embedding VECTOR(768)
);

CREATE TABLE labels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    label TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE bill_labels (
    bill_id UUID REFERENCES bills(id) ON DELETE CASCADE,
    label_id UUID REFERENCES labels(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (bill_id, label_id)
);

CREATE TABLE saved_bills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    bill_id UUID REFERENCES bills(id) ON DELETE CASCADE,
    saved_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    notes TEXT,
    
    UNIQUE(user_id, bill_id)
);

CREATE TABLE ai_summaries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    bill_id UUID REFERENCES bills(id) ON DELETE CASCADE,
    summary_text TEXT NOT NULL,
    key_points JSONB, 
    model_used VARCHAR(100) DEFAULT 'google/flan-t5-small',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX ON bills USING ivfflat (content_embedding vector_cosine_ops);

CREATE INDEX ON bills (base_print_no_str);
CREATE INDEX ON bills (chamber, year);
CREATE INDEX ON bills (sponsor_name);
CREATE INDEX ON bills (status);
CREATE INDEX ON bills (published_date);
CREATE INDEX ON saved_bills (user_id);
CREATE INDEX ON ai_summaries (bill_id);
CREATE INDEX ON bill_labels (bill_id);
CREATE INDEX ON bill_labels (label_id);
CREATE INDEX ON labels (label);

CREATE INDEX ON bills USING gin (title gin_trgm_ops);
CREATE INDEX ON bills USING gin (summary gin_trgm_ops);


ALTER TABLE saved_bills ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Bills are publicly readable" ON bills FOR SELECT USING (true);
CREATE POLICY "Labels are publicly readable" ON labels FOR SELECT USING (true);
CREATE POLICY "Bill labels are publicly readable" ON bill_labels FOR SELECT USING (true);
CREATE POLICY "Users can view own saved bills" ON saved_bills FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "AI summaries are publicly readable" ON ai_summaries FOR SELECT USING (true);



