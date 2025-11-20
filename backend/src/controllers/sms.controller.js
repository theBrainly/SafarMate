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
    // ignore
  }
  return null;
}

// Simple SMS command handler
// Supported:
// - MENU
// - TRACK <stopId>
// - SEATS <busId>
export const handleSMS = async (req, res) => {
  const { phoneNumber, text, sessionId } = req.body || {};
  const input = (text || "").trim();

  const sessionKey = sessionId ? `sms:session:${sessionId}` : phoneNumber ? `sms:user:${phoneNumber}` : null;
  let session = {};
  if (sessionKey) {
    try {
      const raw = await client.get(sessionKey);
      if (raw) session = JSON.parse(raw);
    } catch (_) {}
  }

  const help = [
    "MENU: show options",
    "TRACK <stopId>: ETA to stop (e.g., TRACK s21a)",
    "SEATS <busId>: seat availability (e.g., SEATS b1)",
  ].join("\n");

  if (!input || /^menu$/i.test(input)) {
    session = { step: "menu" };
    if (sessionKey) await client.set(sessionKey, JSON.stringify(session), "EX", 600);
    return res.type("text/plain").send(
      [
        "Select Option:",
        "1) TRACK <stopId>",
        "2) SEATS <busId>",
        "---",
        help,
      ].join("\n")
    );
  }

  const tokens = input.split(/\s+/);
  const cmd = tokens[0]?.toUpperCase();

  if (cmd === "1" && !tokens[1]) {
    session = { step: "await_stop" };
    if (sessionKey) await client.set(sessionKey, JSON.stringify(session), "EX", 600);
    return res.type("text/plain").send("Reply with: TRACK <stopId> (e.g., TRACK s21a)");
  }

  if (session.step === "await_stop" && cmd !== "TRACK") {
    return res.type("text/plain").send("Please send: TRACK <stopId> (e.g., TRACK s21a)");
  }

  if (cmd === "TRACK" && tokens[1]) {
    const stopId = tokens[1];
    const routeDoc = await Route.findOne({ "stops.id": stopId });
    if (!routeDoc) return res.type("text/plain").send("Stop not found.");
    const stop = (routeDoc.stops || []).find((s) => s.id === stopId);
    if (!stop) return res.type("text/plain").send("Stop not found.");
    const bus = await Bus.findOne({ route_id: routeDoc.id, status: "active" }).lean();
    if (!bus) return res.type("text/plain").send("No active bus on this route.");
    const etaSec = await computeEtaSecondsFromBusToStop(bus.id, stop);
    const etaMin = toMinutes(etaSec);
    const minutesText = etaMin == null ? "unknown" : `${etaMin} mins`;
    const seats = typeof bus.seat_count === "number" ? bus.seat_count : "N/A";
    if (sessionKey) await client.del(sessionKey);
    return res
      .type("text/plain")
      .send(`Next bus for ${routeDoc.code} arriving in ${minutesText}. ${seats} seats available.`);
  }

  if (cmd === "2" && !tokens[1]) {
    session = { step: "await_bus" };
    if (sessionKey) await client.set(sessionKey, JSON.stringify(session), "EX", 600);
    return res.type("text/plain").send("Reply with: SEATS <busId> (e.g., SEATS b1)");
  }

  if (session.step === "await_bus" && cmd !== "SEATS") {
    return res.type("text/plain").send("Please send: SEATS <busId> (e.g., SEATS b1)");
  }

  if (cmd === "SEATS" && tokens[1]) {
    const busId = tokens[1];
    const bus = await Bus.findOne({ id: busId }).lean();
    if (!bus) return res.type("text/plain").send("Bus not found.");
    const seats = typeof bus.seat_count === "number" ? bus.seat_count : "N/A";
    if (sessionKey) await client.del(sessionKey);
    return res.type("text/plain").send(`Bus ${busId}: ${seats} seats available.`);
  }

  return res.type("text/plain").send(`Unknown command.\n${help}`);
};


