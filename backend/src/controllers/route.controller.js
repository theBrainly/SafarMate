import Route from "../models/route.model.js";
import { ApiError } from "../utils/ApiError.js";
import { ApiResponse } from "../utils/ApiResponse.js";
import { asyncHandler } from "../utils/asyncHandler.js";

export const createRoute = asyncHandler(async (req, res) => {
  const { id, code, name, active = true, stops = [] } = req.body || {};

  if (!id || !code || !name) {
    throw new ApiError(400, "id, code and name are required");
  }

  const exists = await Route.findOne({ $or: [{ id }, { code }] });
  if (exists) {
    throw new ApiError(409, "Route with same id or code already exists");
  }

  // Basic validation for stops array shape if provided
  if (stops && !Array.isArray(stops)) {
    throw new ApiError(400, "stops must be an array if provided");
  }

  const route = await Route.create({ id, code, name, active, stops });

  return res.status(201).json(new ApiResponse(201, route, "Route created"));
});

export const getRouteForStop = asyncHandler(async (req, res) => {
  const { stopId } = req.params || {};

  if (!stopId) {
    throw new ApiError(400, "stopId is required");
  }

  const route = await Route.findOne({ 'stops.id': stopId }).lean();
  if (!route) {
    throw new ApiError(404, "Route not found for given stopId");
  }

  return res
    .status(200)
    .json(new ApiResponse(200, { stop_id: stopId, route_id: route.id }, "Route for stop"));
});

export const listAllStopsWithRoute = asyncHandler(async (_req, res) => {
  const routes = await Route.find({}, { id: 1, stops: 1, _id: 0 }).lean();
  const items = [];
  for (const r of routes) {
    const routeId = r.id;
    const stops = Array.isArray(r.stops) ? r.stops : [];
    for (const s of stops) {
      items.push({
        stop_id: s.id,
        route_id: routeId,
        name: s.name,
        lat: s.lat,
        lng: s.lng,
        sequence: s.sequence,
      });
    }
  }
  return res.status(200).json(new ApiResponse(200, { stops: items }, "All stops with route ids"));
});



