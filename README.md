<readme>
<![CDATA[
**Geofencing_Delivery_API** is a production-ready B2B logistics engine that combines Google Maps geofencing and fleet-level route optimisation with real-time vehicle tracking. It exposes a clean REST/Socket.IO surface so your ERP, WMS, or custom front-end can automate everything from “truck left depot” webhooks to dynamic, multi-vehicle tour rebuilding in seconds. Use it to slash empty-mile costs, enforce service-level geozones, and surface live ETAs your customers actually trust.

---

## Overview
Geofencing_Delivery_API wraps two core Google Maps capabilities—**Geofencing API** (for entry/exit triggers) and **Route Optimization API** (for cost-aware, constraint-driven tour building) behind a stateless Node service, then maintains a persistent WebSocket channel to broadcast second-by-second GPS deltas to subscribed clients.

![1](https://github.com/user-attachments/assets/6fecf491-eee2-4ba1-a4c8-024ec353b19e)



### Why it matters
* **Control** – Instant alerts if a vehicle detours outside an allowed zone, cutting theft/loss risk.  
* **Efficiency** – Google optimises stop order, capacity, time windows, and driver hours in one call, typically saving 10–25 % route-miles.  
* **Transparency** – WebSocket pushes keep dashboards and customer portals in perfect sync without heavyweight polling.  

---

## Features
| Category          | Capability                                                                                                                     |
|-------------------|--------------------------------------------------------------------------------------------------------------------------------|
| Geofencing        | Create/update/delete circular or polygon zones; receive **enter**, **exit**, or **dwell-timeout** events via webhook or MQTT. |
| Routing           | `/optimizeTours` wrapper supports multi-vehicle, capacity, load-balancing, and hard/soft time windows.                         |
| Real-Time Tracking| Bidirectional Socket.IO channel streams encrypted GPS payloads at configurable intervals (default 1 s).                       |
| Dynamic Reroute   | Trigger re-optimisation mid-route on traffic, order changes, or SLA breaches.                                                  |
| Webhooks          | Out-of-the-box events: `ZONE_ENTER`, `ZONE_EXIT`, `ETA_UPDATED`, `DELIVERY_COMPLETED`.                                         |
| Role-Based Auth   | JWT-secured Admin, Dispatcher, Driver scopes.                                                                                  |
| Extensible        | Clean TypeScript interfaces & OpenAPI 3.1 spec for easy client generation.                                                    |
| Container-Ready   | One-line `docker compose up` spins the full stack.                                                                             |

---

##Technologies Used:

**Kotlin**

Express (REST) & Socket.IO (WebSockets) for full-duplex comms 
Medium

**Google Maps**

Geofencing API 
**Google for Developers**

Route Optimization API 
Google for Developers

PostgreSQL 14 (delivery & zone data)

Redis (pub/sub & cache)

Docker / Docker Compose for reproducible deployments 
Medium

dotenv for environment management 

---

## Usage
## Creating a geofence

POST /api/v1/geofences
{
  "name": "Warehouse A Perimeter",
  "type": "polygon",
  "coordinates": [[51.513,-0.11],[51.514,-0.1], …]
}

## Optimising a delivery tour
POST /api/v1/routes/optimize
{
  "vehicleIds": ["truck-7","truck-12"],
  "shipments": [ … ],
  "objective": "MIN_TRAVEL_TIME"
}
The service proxies the payload to Google’s Route Optimization API and returns an ordered stop list plus cost metrics

## Subscribing to live telemetry
const socket = io("wss://api.example.com", { auth:{token:JWT} });
socket.on("position", data => updateMap(data));


## Installation

> **Prerequisites**: Docker 20+, Docker Compose v2, or Node 18+ & PostgreSQL 14.

```bash
# 1. Clone
git clone https://github.com/<org>/Geofencing_Delivery_API.git
cd Geofencing_Delivery_API

# 2. Configure secrets
cp .env.example .env        # then edit GOOGLE_MAPS_KEY, DATABASE_URL, REDIS_URL

# 3. Launch (Docker)
docker compose up -d        # Production-equivalent stack: API + DB + Redis + NGINX

---





