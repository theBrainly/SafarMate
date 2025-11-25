// parse incoming messages from Twilio/Gupshup, simple command parser
export const handleWhatsApp = async (req, res) => {
  const from = req.body.From; // phone number
  const text = req.body.Body?.trim().toUpperCase();

  if (!text) return res.sendStatus(400);

  // simple parsing
  if (text.startsWith("ETA")) {
    // ETA BUS101
    const busId = text.split(" ")[1];
    const loc = await Bus.findOne({ busId });
    const eta = computeEta(busId);
    return twilioReply(res, `Bus ${busId} ETA: ${eta} mins`);
  }

  if (text.startsWith("SEAT")) {
    // SEAT BUS101 STOP3
    const parts = text.split(" ");
    const busId = parts[1];
    const stopIndex = parseInt(parts[2].replace("STOP", ""), 10);
    const seats = await seatEngine.predict(busId, stopIndex);
    return twilioReply(
      res,
      `Approx ${seats} seats available at Stop ${stopIndex}`
    );
  }

  if (text.startsWith("BOOK")) {
    // BOOK BUS101 STOP2 STOP4
    const parts = text.split(" ");
    const busId = parts[1];
    const fromI = parseInt(parts[2].replace("STOP", ""), 10);
    const toI = parseInt(parts[3].replace("STOP", ""), 10);
    const ticket = await Ticket.create({
      ticketId: genId(),
      busId,
      fromStopIndex: fromI,
      toStopIndex: toI,
      passengerPhone: from,
    });
    return twilioReply(
      res,
      `Ticket booked: ${ticket.ticketId}. Pay via UPI at ...`
    );
  }

  return twilioReply(
    res,
    `Sorry, command not recognized. Try ETA / SEAT / BOOK`
  );
};
