import mongoose from "mongoose";

const TicketSchema = new mongoose.Schema({
  ticketId: { type: String, required: true, unique: true },
  busId: String,
  routeId: String,
  fromStopIndex: Number, // index in route stops
  toStopIndex: Number,
  passengerPhone: String,
  created_at: new Date().toISOString(),
  paid: { type: Boolean, default: false },
  issuedChannel: { 
    type: String,
    enum: ["con","sms","ussd","wp"]
   },
});

const Ticket=mongoose.model("Ticket",TicketSchema)

export default Ticket
