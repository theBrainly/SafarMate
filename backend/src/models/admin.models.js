const administratorProfileSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
      unique: true, // one admin profile per user
    },
    adminLevel: {
      type: String,
      enum: ["SuperAdmin", "Manager", "RouteAdmin"],
      default: "Manager",
    },
    departments: [{ type: String }], 
    permissions: {
      type: [String],
      default: ["manageUsers", "manageRoutes", "viewReports"], // custom roles
    },
  },
  { timestamps: true }
);

export const AdministratorProfile = mongoose.model(
  "AdministratorProfile",
  administratorProfileSchema
);
