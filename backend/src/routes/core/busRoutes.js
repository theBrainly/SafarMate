import express from "express";
import { getBusLocation, updateBusLocation, createBus, getBusesOnRoute, getBusETA } from "../../controllers/bus.controller.js";

const router = express.Router();

// Admin routes
router.post("/", createBus)
router.post("/:busId/location",updateBusLocation)

// User routes
router.get("/:busId/location", getBusLocation);
router.get("/:busId/:stopId", getBusETA);
router.get("/:stopId", getBusesOnRoute);


export default router;