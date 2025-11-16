import express from "express";
import { createRoute, getRouteForStop, listAllStopsWithRoute } from "../../controllers/route.controller.js";
import { getETAByPoints } from "../../controllers/bus.controller.js";

const router = express.Router();

// Admin routes
router.post("/", createRoute);

// User Routes
router.get("/stops/:stopId", getRouteForStop);
router.get("/stops", listAllStopsWithRoute);
router.get("/between", getETAByPoints);

export default router;
