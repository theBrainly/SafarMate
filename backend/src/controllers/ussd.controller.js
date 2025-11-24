import Route from "../models/route.model.js";
import Bus from "../models/bus.models.js";
import { client } from "../integrations/cache/redis.js";
import axios from "axios";

function toMinutes(seconds) {
  if (seconds == null) return null;
  return Math.max(0, Math.round(seconds / 60));
}

async function computeEtaSecondsFromBusToStop(busId, stop) {
  const latStr = await client.hget(busId, "lat");
  const lngStr = await client.hget(busId, "lng");
  if (latStr == null || lngStr == null) return null;

  const lat1 = Number(latStr);
  const lng1 = Number(lngStr);
  const lat2 = Number(stop.lat);
  const lng2 = Number(stop.lng);
  if ([lat1, lng1, lat2, lng2].some((n) => Number.isNaN(n))) return null;

  const url = `http://router.project-osrm.org/route/v1/driving/${lng1},${lat1};${lng2},${lat2}?overview=false`;
  try {
    const resp = await axios.get(url, { timeout: 8000, headers: { Accept: "application/json" } });
    const data = resp.data;
    if (data && Array.isArray(data.routes) && data.routes.length > 0) {
      return Math.round(data.routes[0].duration);
    }
  } catch (_) {
    // fallthrough to return null
  }
  return null;
}

export const handleUSSD = async (req, res) => {
  const { sessionId, phoneNumber, text } = req.body || {};

  // Normalize incoming text
  const input = (text || "").trim();
  const parts = input.length ? input.split("*") : [];

  // Menu 0: Root menu
  if (parts.length === 0) {
    const menu = [
      "CON Select Option:",
      "1. Track Bus",
      "2. Seat Availability",
    ].join("\n");
    res.type("text/plain").send(menu);
    return;
  }

  // Option 1: Track Bus → ask for stop id then show ETA
  if (parts[0] === "1") {
    // Ask for stop id
    if (parts.length === 1) {
      res.type("text/plain").send("CON Enter Stop ID (e.g., s21a):");
      return;
    }

    const stopId = parts[1];
    const routeDoc = await Route.findOne({ "stops.id": stopId });
    if (!routeDoc) {
      res.type("text/plain").send("END Stop not found.");
      return;
    }
    const stop = (routeDoc.stops || []).find((s) => s.id === stopId);
    if (!stop) {
      res.type("text/plain").send("END Stop not found.");
      return;
    }

    // Find any active bus on this route
    const bus = await Bus.findOne({ route_id: routeDoc.id, status: "active" }).lean();
    if (!bus) {
      res.type("text/plain").send("END No active bus on this route.");
      return;
    }

    const etaSec = await computeEtaSecondsFromBusToStop(bus.id, stop);
    const etaMin = toMinutes(etaSec);

    // Seats simple: use seat_count as upper bound if nothing else available
    let seatsText = "seats info unavailable";
    if (typeof bus.seat_count === "number") {
      seatsText = `${bus.seat_count} seats available`;
    }

    const minutesText = etaMin == null ? "unknown" : `${etaMin} mins`;
    const msg = `END Next bus for ${routeDoc.code} arriving in ${minutesText}. ${seatsText}.`;
    res.type("text/plain").send(msg);
    return;
  }

  // Option 2: Seat Availability → ask for Bus ID then show seats
  if (parts[0] === "2") {
    if (parts.length === 1) {
      res.type("text/plain").send("CON Enter Bus ID (e.g., b1):");
      return;
    }
    const busId = parts[1];
    const bus = await Bus.findOne({ id: busId }).lean();
    if (!bus) {
      res.type("text/plain").send("END Bus not found.");
      return;
    }
    const seats = typeof bus.seat_count === "number" ? bus.seat_count : "N/A";
    res.type("text/plain").send(`END Bus ${busId}: ${seats} seats available.`);
    return;
  }

  // Unknown input → show root menu again
  const menu = [
    "CON Select Option:",
    "1. Track Bus",
    "2. Seat Availability",
  ].join("\n");
  res.type("text/plain").send(menu);
};


