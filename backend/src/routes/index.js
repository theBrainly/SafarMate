import express from "express";

// Core (channel-agnostic) routes
import busRoutes from "./core/busRoutes.js";
import routeRoutes from "./core/routeRoutes.js";

// Channel-specific routes (adapters)
import ussdRoutes from "./channels/ussdRoutes.js";
import smsRoutes from "./channels/smsRoutes.js";
import whatsappRoutes from "./channels/whatsappRoutes.js";

const router = express.Router();

/**
 * Core APIs (used by app/web frontend, and also channel adapters)
 * Example: GET /api/buses/:busId/location
 */

router.use("/buses", busRoutes);
router.use("/routes", routeRoutes);

/**
 * Channel adapters
 * Example: POST /api/ussd
 */

router.use("/ussd", ussdRoutes);
router.use("/sms", smsRoutes);
router.use("/whatsapp", whatsappRoutes);

export default router;
