import express from "express";
import { handleUSSD } from "../../controllers/ussd.controller.js";

const router=express.Router();

router.post("/", handleUSSD);

export default router;