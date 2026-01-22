<div align="center">

# LegisTrack

**Track NY State legislation with AI-powered summaries**

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Next.js](https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=next.js&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

</div>

---

Search NY Senate bills, get AI-generated summaries of complex legislation, and save bills to track over time.

## Features

- Search bills by keyword and session year
- View bill details, sponsors, and status
- AI summaries of dense legal text
- Save and annotate bills (requires sign-in)

## Tech Stack

**Frontend:** Next.js, React, TypeScript, Tailwind CSS

**Backend:** Spring Boot (Java 17), PostgreSQL, Redis

**Services:** Supabase (auth + database), Hugging Face (AI), NY Senate API

## How to Run

### Docker (recommended)

```bash
cp .env.example .env  # fill in your keys
docker compose up --build
```

Frontend: `http://localhost:3000` · Backend: `http://localhost:8080`

### Local Development

**1. Backend**

```bash
cd backend
./run.sh
```

**2. Frontend**

```bash
cd frontend
npm install
npm run dev
```

## Environment Variables

Create `.env` in the root:

```env
DATABASE_URL=jdbc:postgresql://...
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=...
SUPABASE_URL=https://xxx.supabase.co
SUPABASE_SERVICE_ROLE_KEY=...
NEXT_PUBLIC_SUPABASE_URL=https://xxx.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=...
NY_SENATE_API_KEY=...
HUGGINGFACE_API_KEY=...
```
