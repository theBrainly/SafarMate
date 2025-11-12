import { busLocations } from "../data/demoStore.js";
import {client} from "../integrations/cache/redis.js"
import Bus from "../models/bus.models.js";
import Route from "../models/route.model.js";
import { ApiError } from "../utils/ApiError.js";
import { ApiResponse } from "../utils/ApiResponse.js";
import { asyncHandler } from "../utils/asyncHandler.js";
import axios from "axios";


export const getBusLocation = asyncHandler(async(req,res)=>{
    const {busId}=req.params;

    if(!busId){
        throw new ApiError(400, "busId is required");
    }

    const existed = await client.exists(busId);
    if(!existed){
        throw new ApiError(404, "Bus location not found");
    }

    const data=await client.hgetall(busId);

    return res.status(200).json(new ApiResponse(200, data, "Location fetched"));
})

export const updateBusLocation=async(req,res)=>{
    const {busId}=req.params;
    const {lat,longitude,s,heading}=req.query;

    const existed = await client.exists(busId);

    const fieldsAdded = await client.hset(
        busId,
        {
            busId: busId,
            lat: lat,
            lng: longitude,
            speed: s,
            heading: heading,
            updated_at: new Date().toISOString()
        }
    );

    console.log({ busId, fieldsAdded, existed: Boolean(existed) });
    return res.json({ status: existed ? 'updated' : 'created' });
}

export const createBus = asyncHandler(async (req, res) => {
    const { id, route_id, plate, seat_count = 40, status = 'active' } = req.body || {};

    if (!id || !route_id || !plate) {
        throw new ApiError(400, "id, route_id and plate are required");
    }

    const route = await Route.findOne({ id: route_id });
    if (!route) {
        throw new ApiError(404, "Route not found");
    }

    const existing = await Bus.findOne({ $or: [{ id }, { plate }] });
    if (existing) {
        throw new ApiError(409, "Bus with same id or plate already exists");
    }

    const bus = await Bus.create({ id, route_id, plate, seat_count, status });

    return res.status(201).json(new ApiResponse(201, bus, "Bus created"));
});

export const getBusETA = asyncHandler(async (req, res) => {
    const { busId,stopId } = req.params || {};
    
    const orig_lat = await client.hget(busId, 'lat');
    const orig_lng = await client.hget(busId, 'lng');
    
    // Get the lat and lng for the stopId
    if (orig_lat === null || orig_lng === null) {
        throw new ApiError(404, "Bus location not found");
    }

    const routeDoc = await Route.findOne({ 'stops.id': stopId });
    if (!routeDoc) {
        throw new ApiError(404, "Stop not found");
    }
    const stop = (routeDoc.stops || []).find(s => s.id === stopId);
    if (!stop) {
        throw new ApiError(404, "Stop not found");
    }
    const dest_lat = stop.lat;
    const dest_lng = stop.lng;
    
    const lat1 = Number(orig_lat);
    const lng1 = Number(orig_lng);
    const lat2 = Number(dest_lat);
    const lng2 = Number(dest_lng);
    
    if ([lat1, lng1, lat2, lng2].some((n) => Number.isNaN(n))) {
        throw new ApiError(400, "All coordinates must be numbers");
    }
    
    // OSRM expects lon,lat order
    const url = `http://router.project-osrm.org/route/v1/driving/${lng1},${lat1};${lng2},${lat2}?overview=false`;
    
    try {
        const response = await axios.get(url, { timeout: 8000, headers: { Accept: 'application/json' } });
        const data = response.data;
        if (data && Array.isArray(data.routes) && data.routes.length > 0) {
            const route = data.routes[0];
            const result = {
                distance_meters: route.distance ?? null,
                duration_seconds: route.duration ?? null,
                provider_status: data.code || 'OK',
            };
            return res.status(200).json(new ApiResponse(200, result, "ETA computed"));
        }
        throw new ApiError(502, "Failed to compute ETA");
    } catch (err) {
        const provider = err?.response?.data;
        const status = err?.response?.status || 502;
        const message = provider?.message || err.message || "Request failed";
        throw new ApiError(status, message, provider || []);
    }
});

export const getETAByPoints = asyncHandler(async (req, res) => {
    const { orig_lat, orig_lng, dest_lat, dest_lng } = req.query || {};
  
    if (
      orig_lat === undefined ||
      orig_lng === undefined ||
      dest_lat === undefined ||
      dest_lng === undefined
    ) {
      throw new ApiError(400, "orig_lat, orig_lng, dest_lat, dest_lng are required");
    }
  
    const lat1 = Number(orig_lat);
    const lng1 = Number(orig_lng);
    const lat2 = Number(dest_lat);
    const lng2 = Number(dest_lng);
  
    if ([lat1, lng1, lat2, lng2].some((n) => Number.isNaN(n))) {
      throw new ApiError(400, "All coordinates must be numbers");
    }
  
    const url = `http://router.project-osrm.org/route/v1/driving/${lng1},${lat1};${lng2},${lat2}?overview=false`;
  
    try {
      const response = await axios.get(url, { timeout: 8000, headers: { Accept: "application/json" } });
      const data = response.data;
      if (data && Array.isArray(data.routes) && data.routes.length > 0) {
        const route = data.routes[0];
        const result = {
          distance_meters: route.distance ?? null,
          duration_seconds: route.duration ?? null,
          provider_status: data.code || "OK",
        };
        return res.status(200).json(new ApiResponse(200, result, "ETA computed"));
      }
      throw new ApiError(502, "Failed to compute ETA");
    } catch (err) {
      const provider = err?.response?.data;
      const status = err?.response?.status || 502;
      const message = provider?.message || err.message || "Request failed";
      throw new ApiError(status, message, provider || []);
    }
  });

export const getBusesOnRoute = asyncHandler(async(req,res)=>{
    const {stopId}=req.params;
    // Find the route that has this stopId
    const routeDoc = await Route.findOne({ 'stops.id': stopId }).lean();
    if (!routeDoc) {
        throw new ApiError(404, "Route not found for provided stopId");
    }

    const routeSuffix = typeof routeDoc.id === 'string' && routeDoc.id.length > 1
        ? routeDoc.id.slice(1)
        : routeDoc.id;

    // Match buses whose id (without first letter) equals the route suffix
    const idPattern = new RegExp(`^.${routeSuffix}$`); // e.g., r21 -> /^.21$/ matches b21
    const buses = await Bus.find({ id: idPattern }).select({ id: 1, _id: 0 }).lean();

    const busIds = buses.map(b => b.id);
    return res.status(200).json(new ApiResponse(200, { route_id: routeDoc.id, buses: busIds }, "Buses on route"));
})
