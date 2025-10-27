import mongoose from "mongoose";
import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";

const userSchema = new mongoose.Schema(
  {
    role: { type: Number, enum: [0, 1, 2], default: 0, required: true }, // 0=user,1=crew,2=admin
    firstName: { type: String, required: true, trim: true },
    lastName: { type: String, trim: true },
    email: {
      type: String,
      required: true,
      unique: true,
    },
    phone: { type: String, unique: true, sparse: true }, // sparse allows null duplicates
    password: { type: String, required: true, minlength: 6 },

    gender: {
      type: String,
      enum: ["Male", "Female", "Other"],
      default: "Male",
    },
    dob: { type: Date },
    avatar: { type: String },
    refreshToken: { type: String },

    designation: { type: String },

    // no manual createdAt/updatedAt here; timestamps handles it
  },
  { timestamps: true }
);

// Hash on create/save
userSchema.pre("save", async function (next) {
  if (!this.isModified("password")) return next();
  this.password = await bcrypt.hash(this.password, 10);
  next();
});

// Hash on findOneAndUpdate if password is changed
userSchema.pre("findOneAndUpdate", async function (next) {
  const update = this.getUpdate();
  if (update?.password) {
    update.password = await bcrypt.hash(update.password, 10);
    this.setUpdate(update);
  }
  next();
});

userSchema.methods.isPasswordCorrect = async function (password) {
  return bcrypt.compare(password, this.password);
};

userSchema.methods.generateAccessToken = function () {
  return jwt.sign(
    {
      _id: this._id,
      email: this.email,
      name: `${this.firstName} ${this.lastName}`,
      role: this.role,
    },
    process.env.ACCESS_TOKEN_SECRET,
    { expiresIn: process.env.ACCESS_TOKEN_EXPIRY || "15m" }
  );
};

userSchema.methods.generateRefreshToken = function () {
  return jwt.sign({ _id: this._id }, process.env.REFRESH_TOKEN_SECRET, {
    expiresIn: process.env.REFRESH_TOKEN_EXPIRY || "7d",
  });
};

export const User = mongoose.model("User", userSchema);
