import mongoose from "mongoose";
import { User } from "./user.model.js";

// Crew Profile Schema
const crewProfileSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
      unique: true, // one crew profile per user
    },
    employeeId: { type: String, unique: true, required: true }, // govt/bus assigned ID
    position: { type: String, enum: ["driver", "conductor"] },
    busAssigned: { type: String }, // bus number or route code
    shiftTiming: { type: String }, // e.g. "9AM - 5PM"
    onDuty: { type: Boolean, default: false },
    assignedRoutes: [{ type: mongoose.Schema.Types.ObjectId, ref: "Route" }],
    assignedBus: { type: mongoose.Schema.Types.ObjectId, ref: "Bus" },
    licenseNumber: { type: String },
    emergencyContact: { type: String },
    hireDate: { type: Date, default: Date.now },
    status: {
      type: String,
      enum: ["Active", "Inactive", "Leave", "Suspended"],
      default: "Active",
    },
    longitude: { type: Number },
    latitude: { type: Number },
    last_updated_at: { type: Date },

    experienceYears: { type: Number, default: 0 },
  },
  { timestamps: true }
);

export const CrewProfile = mongoose.model("CrewProfile", crewProfileSchema);
