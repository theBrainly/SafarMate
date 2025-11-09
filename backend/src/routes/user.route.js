import Router from "express";
// import upload from "../middlewares/molter.middleware.js";
import { upload } from "../middlewares/multer.middleware.js";
import { verifyJWT } from "../middlewares/auth.middleware.js";
import {
  signup,
  loginUser,
  logoutUser,
  changeCurrentPassword,
  getCurrentUser,
  updateAccountDetails,
  updateUserAvatar,
  
} from "../controllers/user.controller.js";
const router = Router();

router.route("/signup").post(signup);
router.route("/login").post(loginUser);
router.route("/logout").post(verifyJWT, logoutUser);
router.route("/change/password").patch(verifyJWT, changeCurrentPassword);
router.route("/current-user").get(verifyJWT, getCurrentUser);
router
  .route("/update-userdetails")
  .post(verifyJWT, upload.single("avatar"), updateAccountDetails);
router
  .route("/change/avatarImage")
  .post(verifyJWT, upload.single("avatar"), updateUserAvatar);

export default router;
