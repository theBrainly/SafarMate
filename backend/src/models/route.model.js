import mongoose from 'mongoose';

const StopSchema = new mongoose.Schema({
  id: { type: String, required: true }, // e.g., 's1'
  route_id: { type: String, required: true }, // e.g., 'r1'
  name: { type: String, required: true }, // e.g., 'City Center'
  lat: { type: Number, required: true },
  lng: { type: Number, required: true },
  sequence: { type: Number, required: true }
}, { _id: false });

const RouteSchema = new mongoose.Schema({
  id: { type: String, required: true }, // e.g., 'r1'
  code: { type: String, required: true }, // e.g., '1'
  name: { type: String, required: true }, // e.g., 'City Center — Airport'
  active: { type: Boolean, default: true },
  stops: { type: [StopSchema], default: [] }
});

const Route= mongoose.model("Route",RouteSchema)

export default Route;