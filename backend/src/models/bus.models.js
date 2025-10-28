import mongoose from "mongoose";

const BusSchema = new mongoose.Schema({
  id: { type: String, required: true, unique: true },
  route_id: { type: String, required: true },
  plate: { type: String, required: true },
  seat_count: { type: Number, default: 40 },
  status: { type: String, enum: ['active', 'inactive'], default: 'active' },
});

const Bus=mongoose.model("Bus",BusSchema)

export default Bus;
