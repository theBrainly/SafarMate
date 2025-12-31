SafarMate Backend

Overview
SafarMate is a Node.js/Express backend with MongoDB (Mongoose) and Redis used for live bus tracking, ETA computation, and multi-channel interactions (USSD, SMS, WhatsApp).

Tech Stack
- Node.js + Express (type: module)
- MongoDB via Mongoose
- Redis (via ioredis) for live bus locations and lightweight sessions
- OSRM public routing service for ETA computations
- Docker + docker compose for local development

Getting Started
Prerequisites
- Node.js 18+
- Docker (optional, for containerized setup)

Env Vars (.env)
- PORT=2000
- MONGODB_URL=mongodb://<host>:<port>/<db>
- REDIS_HOST=127.0.0.1
- REDIS_PORT=6379

Install & Run (local)
1) Install deps
   npm ci

2) Start dev server
   node src/server.js

Docker
- Build & run
  docker compose up -d

- Rebuild
  docker compose up --build -d

Project Structure
- src/app.js – Express app wiring
- src/server.js – HTTP server bootstrap
- src/db/db.js – Mongo connection
- src/integrations/cache/redis.js – Redis client
- src/models – Mongoose models (Bus, Route, etc.)
- src/controllers – Route handlers (bus, route, ussd, sms, whatsapp)
- src/routes – Router groups (core + channels)
- src/utils – ApiError/ApiResponse/asyncHandler utilities

API Reference (Base path: /api)

Core: Buses (/api/buses)
- POST /            Create a bus
  Body: { id, route_id, plate, seat_count?, status? }
- POST /:busId/location
  Query: lat, longitude, s (speed), heading
  Upserts live bus location in Redis
- GET /:busId/location
  Returns the current location from Redis
- GET /:busId/:stopId
  Returns ETA (distance_meters, duration_seconds) from the bus to the stop (OSRM)
- GET /:stopId
  Returns buses on the same route as the stop by matching ids (trim first letter rule)

Core: Routes (/api/routes)
- POST /            Create a route
  Body: { id, code, name, active?, stops?[] }
  Stop shape: { id, route_id, name, lat, lng, sequence }
- GET /stops        List all stops with their route ids
  Response: { stops: [{ stop_id, route_id, name, lat, lng, sequence }] }
- GET /stops/:stopId
  Returns { stop_id, route_id } for the provided stop

Note on point-to-point ETA
- There is a controller available for ETA between arbitrary points, but the route group is not mounted by default. If you wish to expose it:
  - Add: `import etaRoutes from "./core/etaRoutes.js";`
  - Mount: `router.use("/eta", etaRoutes);`
  - Then use: `GET /api/eta/between?orig_lat&orig_lng&dest_lat&dest_lng`

Channels
- USSD (/api/ussd)
  POST body example: { sessionId, phoneNumber, text }
  Flow:
    - Root menu (CON text):
      1. Track Bus -> prompts stop id -> returns ETA + seats
      2. Seat Availability -> prompts bus id -> returns seats

- SMS (/api/sms)
  POST body example: { sessionId?, phoneNumber, text }
  Commands: MENU, 1 → TRACK flow, 2 → SEATS flow, TRACK <stopId>, SEATS <busId>
  Uses Redis-backed sessions with 10 min TTL

- WhatsApp (/api/whatsapp)
  POST body example: { sessionId?, from, text }
  Same commands/flows as SMS; JSON response payload: { reply: "..." }

Models
- Bus
  { id, route_id, plate, seat_count, status, last_seen_at? }
- Route
  { id, code, name, active, stops: [{ id, route_id, name, lat, lng, sequence }] }

Live Location Storage (Redis)
- Key: busId
- Hash fields: { busId, lat, lng, speed, heading, updated_at }

Error/Response Shape
- ApiResponse: { data, statusCode, message, success }
- ApiError: throws with statusCode and message

Notes
- ETA uses the public OSRM server (no key) and expects lon,lat order in URLs
- USSD returns plain text with CON/END prefixes; SMS returns plain text; WhatsApp returns JSON reply

License
ISC


