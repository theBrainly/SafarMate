import { ApiResponse } from "../utils/ApiResponse.js";
import { ApiError } from "../utils/ApiError.js";
import { asyncHandler } from "../utils/asyncHandler.js";
import { uploadOnCloudinary } from "../utils/cloudinary.js";
import { User } from "../models/user.model.js";

const gernateAccessAndRefreshTokens = async (userId) => {
  try {
    const user = await User.findById(userId);

    const accessToken = user.generateAccessToken();
    const refreshToken = user.generateRefreshToken();

    user.refreshToken = refreshToken;
    await user.save({ validateBeforeSave: false });

    return { accessToken, refreshToken };
  } catch (error) {
    throw new ApiError(
      500,
      " something went wrong while generaing refresh and access token "
    );
  }
};
const signup = asyncHandler(async (req, res) => {
  const {
    role,
    firstName,
    lastName,
    email,
    phone,
    password,
    gender,
    designation,
  } = req.body;

  if (!email || !password) {
    throw new ApiError(400, "All fields are required");
  }

  const userExists = await User.findOne({
    $or: [{ email }, { phone }],
  });

  if (userExists) {
    throw new ApiError(400, "User already exists");
  }
  const user = await User.create({
    role,
    firstName,
    lastName,
    email,
    phone,
    password,
    gender,
    designation,
  });

  const createdUser = await User.findById(user._id).select(
    "-password -refreshToken"
  );
  console.log("user created succefully");

  if (!createdUser) {
    throw new ApiError(505, "Something went wrong while registering the user");
  }
  const { accessToken, refreshToken } = await gernateAccessAndRefreshTokens(
    user._id
  );

 
  const options = { secure: true, httpOnly: true, sameSite: "none" };

  return res
    .status(201)
    .cookie("accessToken", accessToken, options)
    .cookie("refreshToken", refreshToken, options)
    .json(new ApiResponse(201, createdUser, "user register successdully"));
});

const loginUser = asyncHandler(async (req, res) => {
  const { email, password } = req.body;
  if (!email) {
    throw new ApiError(400, "  email is required ");
  }

  const user = await User.findOne({
    $or: [{ email }],
  });

  if (!user) {
    throw new ApiError(404, " User does not exist ");
  }

  // const isPasswordCorrect = await user.isPasswordcorrect(password);
  const isPasswordCorrect = await user.isPasswordCorrect(password);

  if (!isPasswordCorrect) {
    throw new ApiError(401, " Password does not match with user ");
  }

  const { accessToken, refreshToken } = await gernateAccessAndRefreshTokens(
    user._id
  );

  const loggedInUser = await User.findById(user._id).select(
    "-password -refreshToken"
  );
  console.log("user login succefully");
  const options = { secure: true, httpOnly: true, sameSite: "none" };
  return res
    .status(200)
    .cookie("accessToken", accessToken, options)
    .cookie("refreshToken", refreshToken, options)
    .json(
      new ApiResponse(
        200,
        {
          user: loggedInUser,
        },
        "User Loggin successfully "
      )
    );
});

const logoutUser = asyncHandler(async (req, res) => {
  await User.findByIdAndUpdate(
    req.user._id,
    {
      $unset: {
        refreshToken: 1,
      },
    },
    { new: true }
  );

  const options = { secure: true, httpOnly: true, sameSite: "none" };

  return res
    .status(200)
    .clearCookie("accessToken", options)
    .clearCookie("refreshToken", options)
    .json(new ApiResponse(200, {}, "User Logged Out "));
});

const changeCurrentPassword = asyncHandler(async (req, res) => {
  const { oldPassword, newPassword } = req.body;
  const user = await User.findById(req.user?._id);
  const isPasswordCorrect = await user.isPasswordCorrect(oldPassword);

  if (!isPasswordCorrect) {
    throw new ApiError(400, "Invaild old Password");
  }

  user.password = newPassword;
  await user.save({ validateBeforeSave: false });

  return res
    .status(200)
    .json(new ApiResponse(200, {}, "Password change successfully "));
});
const getCurrentUser = asyncHandler(async (req, res) => {
  return res
    .status(200)
    .json(new ApiResponse(200, req.user, "Current user fetched successfully"));
});

const updateAccountDetails = asyncHandler(async (req, res) => {
  const { firstName, lastName } = req.body;

  if (!firstName || !lastName) {
    throw new ApiError(400, "All fields are required");
  }
  let avatar = "";
  if (!req.file) {
    console.log("file is not coming ");
  }
  if (req.file) {
    console.log("file is coming ");

    try {
      if (!req.file) {
        throw new ApiError(400, "1 Avatar file is missing");
      }

      const avatarLocalPath = req.file?.path;

      if (!avatarLocalPath) {
        throw new ApiError(400, "2 Avatar file is missing");
      }

      avatar = await uploadOnCloudinary(avatarLocalPath);

      if (!avatar.url) {
        throw new ApiError(400, "Error while uploading on avatar");
      }
    } catch (error) {
      console.log("error", error.message);
    }
  }

  const user = await User.findByIdAndUpdate(
    req.user?._id,
    {
      $set: {
        firstName: firstName,
        lastName: lastName,
        avatar: avatar.url,
      },
    },
    { new: true }
  ).select("-password -refreshToken");

  return res
    .status(200)
    .json(new ApiResponse(200, user, "Account details updated successfully"));
});

const updateUserAvatar = asyncHandler(async (req, res) => {
  if (!req.file) {
    throw new ApiError(400, "1 Avatar file is missing");
  }
  console.log(req.file);

  const avatarLocalPath = req.file?.path;

  if (!avatarLocalPath) {
    throw new ApiError(400, "2 Avatar file is missing");
  }

  const avatar = await uploadOnCloudinary(avatarLocalPath);

  if (!avatar.url) {
    throw new ApiError(400, "Error while uploading on avatar");
  }

  const user = await User.findByIdAndUpdate(
    req.user?._id,
    {
      $set: {
        avatar: avatar.url,
      },
    },
    { new: true }
  ).select("-password");

  return res
    .status(200)
    .json(new ApiResponse(200, user, "Avatar image updated successfully "));
});

const refreshAccessToken = asyncHandler(async (req, res) => {
  const incomingRefreshToken =
    req.cookies.refreshToken || req.body.refreshToken;

  if (!incomingRefreshToken) {
    throw new ApiError(401, "unauthorized request ");
  }

  try {
    const decodedToken = jwt.verify(
      incomingRefreshToken,
      process.env.REFRESH_TOKEN_SECRET
    );

    const user = await User.findById(decodedToken?._id);

    if (!user) {
      throw new ApiError(401, "Invalid refresh token");
    }

    if (incomingRefreshToken !== user?.refreshToken) {
      throw new ApiError(401, "Refresh token is expired or used");
    }

    const options = { secure: true, httpOnly: true, sameSite: "none" };

    const { accessToken, newRefreshToken } =
      await gernateAccessAndRefreshTokens(user._id);

    return res
      .status(200)
      .cookie("accessToken", accessToken, options)
      .cookie("refreshToken", newRefreshToken, options)
      .json(
        new ApiResponse(
          200,
          { accessToken, refreshToken: newRefreshToken },
          "Access token refreshed"
        )
      );
  } catch (error) {
    throw new ApiError(401, error?.message || "Invalid refresh token");
  }
});

export {
  signup,
  loginUser,
  logoutUser,
  changeCurrentPassword,
  getCurrentUser,
  updateAccountDetails,
  updateUserAvatar,
};
