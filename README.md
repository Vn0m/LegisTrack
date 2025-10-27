# LegisTrack

Track and understand NY State legislation.

## Tech Stack
- **Frontend**: Next.js, React, TypeScript
- **Backend**: Spring Boot (Java)
- **Database**: PostgreSQL (Supabase)
- **AI**: Hugging Face (bill summarization, embeddings)

## Setup

1. **Backend**: 
   ```bash
   cd backend
   ./run.sh
   ```

2. **Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Environment Variables
Create `.env` files in both frontend and backend directories:
- `NY_SENATE_API_KEY` - Get from [NY Senate API](https://legislation.nysenate.gov/api/3/)
- `HUGGINGFACE_API_KEY` - Get from [Hugging Face](https://huggingface.co/settings/tokens)
- `REDIS_URL` - Optional, for caching

## Features
- Search NY Senate bills by keyword/year
- View bill details (status, sponsors, full text)
- Generate AI summaries of bill memos
- Save bills for later 

