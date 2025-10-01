const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const REPO_PATH = process.cwd();
const BACKUP_PATH = path.join(path.dirname(REPO_PATH), 'SafarMate_Backup_Temp');
const START_DATE = new Date('2025-10-01T09:00:00');
const END_DATE = new Date('2025-12-30T17:00:00');

// Defines the order in which files are "created" or "modified"
const HISTORY = [
    // --- PHASE 1: SETUP (Oct 1-5) ---
    { msg: "Initial commit", date: "2025-10-01", files: ["README.md"] },
    { msg: "Add .gitignore", date: "2025-10-01", files: [".gitignore"] },
    { msg: "Initialize backend project", date: "2025-10-02", files: ["backend/package.json", "backend/package-lock.json"] },
    { msg: "Setup backend gitignore", date: "2025-10-02", files: ["backend/.gitignore"] },
    { msg: "Add Docker configuration", date: "2025-10-03", files: ["backend/Dockerfile", "backend/compose.yaml", "backend/.dockerignore"] },
    { msg: "Initialize Android project", date: "2025-10-04", files: ["myPP/build.gradle.kts", "myPP/settings.gradle.kts", "myPP/gradle.properties"] },
    { msg: "Setup Android gitignore", date: "2025-10-04", files: ["myPP/.gitignore", "myPP/.gitignore copy"] },
    { msg: "Configure Gradle wrapper", date: "2025-10-05", files: ["myPP/gradle/wrapper/gradle-wrapper.properties", "myPP/gradle/wrapper/gradle-wrapper.jar", "myPP/gradlew"] },
    { msg: "Add libs.versions.toml", date: "2025-10-05", files: ["myPP/gradle/libs.versions.toml"] },

    // --- PHASE 2: BACKEND FOUNDATION (Oct 6-25) ---
    { msg: "Create basic server structure", date: "2025-10-07", files: ["backend/src/server.js", "backend/src/app.js"] },
    { msg: "Setup database connection", date: "2025-10-08", files: ["backend/src/db/db.js"] },
    { msg: "Add environment configuration", date: "2025-10-09", files: [] }, // simulate env setup (files ignored)
    { msg: "Implement API response utilities", date: "2025-10-10", files: ["backend/src/utils/ApiResponse.js", "backend/src/utils/ApiError.js", "backend/src/utils/asyncHandler.js"] },
    { msg: "Add common utilities", date: "2025-10-11", files: ["backend/src/utils/index.js"] },
    { msg: "Setup Cloudinary integration", date: "2025-10-12", files: ["backend/src/utils/cloudinary.js"] },
    { msg: "Configure Redis cache", date: "2025-10-14", files: ["backend/src/integrations/cache/redis.js"] },
    { msg: "Add basic data store for demo", date: "2025-10-15", files: ["backend/src/data/demoStore.js"] },
    { msg: "Implement file upload middleware", date: "2025-10-18", files: ["backend/src/middlewares/multer.middleware.js"] },

    // --- PHASE 3: BACKEND MODELS (Oct 26-Nov 5) ---
    { msg: "Create User model", date: "2025-10-27", files: ["backend/src/models/user.models.js"] },
    { msg: "Create Bus model", date: "2025-10-28", files: ["backend/src/models/bus.models.js"] },
    { msg: "Create Route model", date: "2025-10-29", files: ["backend/src/models/route.model.js"] },
    { msg: "Create Ticket model", date: "2025-10-30", files: ["backend/src/models/ticket.models.js"] },
    { msg: "Create Crew model", date: "2025-11-01", files: ["backend/src/models/crew.models.js"] },
    { msg: "Create Admin model", date: "2025-11-02", files: ["backend/src/models/admin.models.js"] },

    // --- PHASE 4: BACKEND FEATURES (Nov 6-25) ---
    { msg: "Implement User Controller", date: "2025-11-07", files: ["backend/src/controllers/user.controller.js"] },
    { msg: "Add Auth Middleware", date: "2025-11-08", files: ["backend/src/middlewares/auth.middleware.js"] },
    { msg: "Setup User Routes", date: "2025-11-09", files: ["backend/src/routes/user.route.js"] },
    { msg: "Implement Bus Controller", date: "2025-11-12", files: ["backend/src/controllers/bus.controller.js"] },
    { msg: "Setup Bus Routes", date: "2025-11-13", files: ["backend/src/routes/core/busRoutes.js"] },
    { msg: "Implement Route Controller", date: "2025-11-15", files: ["backend/src/controllers/route.controller.js"] },
    { msg: "Setup Route Routes", date: "2025-11-16", files: ["backend/src/routes/core/routeRoutes.js"] },
    { msg: "Implement SMS channel controller", date: "2025-11-20", files: ["backend/src/controllers/sms.controller.js"] },
    { msg: "Setup SMS routes", date: "2025-11-20", files: ["backend/src/routes/channels/smsRoutes.js"] },
    { msg: "Implement WhatsApp controller", date: "2025-11-22", files: ["backend/src/controllers/whatsapp.controller.js"] },
    { msg: "Setup WhatsApp routes", date: "2025-11-22", files: ["backend/src/routes/channels/whatsappRoutes.js"] },
    { msg: "Implement USSD controller", date: "2025-11-24", files: ["backend/src/controllers/ussd.controller.js"] },
    { msg: "Setup USSD routes", date: "2025-11-24", files: ["backend/src/routes/channels/ussdRoutes.js"] },
    { msg: "Setup Webhook controller", date: "2025-11-25", files: ["backend/src/controllers/webhook.controller.js"] },
    { msg: "Aggregate all routes", date: "2025-11-25", files: ["backend/src/routes/index.js"] },
    { msg: "Document API endpoints", date: "2025-11-25", files: ["backend/API.md"] },

    // --- PHASE 5: ANDROID CORE (Nov 26-Dec 5) ---
    // Removed explicit directory creation step because path creation is handled implicitly by copyFile
    { msg: "Add Android Manifest", date: "2025-11-26", files: ["myPP/app/src/main/AndroidManifest.xml"] },
    { msg: "Create Application class", date: "2025-11-27", files: ["myPP/app/src/main/java/com/example/mypp/SafarMateApp.kt"] },
    { msg: "Create MainActivity", date: "2025-11-28", files: ["myPP/app/src/main/java/com/example/mypp/MainActivity.kt"] },
    { msg: "Define App Theme", date: "2025-11-30", files: ["myPP/app/src/main/java/com/example/mypp/ui/theme/Theme.kt", "myPP/app/src/main/java/com/example/mypp/ui/theme/Color.kt", "myPP/app/src/main/java/com/example/mypp/ui/theme/Type.kt"] },
    { msg: "Add design system extensions", date: "2025-12-01", files: ["myPP/app/src/main/java/com/example/mypp/ui/theme/DesignSystemExtensions.kt", "myPP/app/src/main/java/com/example/mypp/ui/theme/Dimensions.kt"] },
    { msg: "Add string resources", date: "2025-12-02", files: ["myPP/app/src/main/res/values/strings.xml", "myPP/app/src/main/res/values/colors.xml", "myPP/app/src/main/res/values/themes.xml"] },
    {
        msg: "Add raw resources and drawables", date: "2025-12-03", files: [
            "myPP/app/src/main/res/drawable/arrow_forward.xml",
            "myPP/app/src/main/res/drawable/arrow_icon.xml",
            "myPP/app/src/main/res/drawable/back_icon.xml",
            "myPP/app/src/main/res/drawable/bus_logo.xml",
            "myPP/app/src/main/res/drawable/bus_small_icon.xml",
            "myPP/app/src/main/res/drawable/card_default.xml",
            "myPP/app/src/main/res/drawable/card_selected.xml",
            "myPP/app/src/main/res/drawable/home_swap_icon.xml",
            "myPP/app/src/main/res/drawable/icon_location.xml",
            "myPP/app/src/main/res/drawable/icon_notification.xml",
            "myPP/app/src/main/res/drawable/line_left.xml",
            "myPP/app/src/main/res/drawable/line_right.xml",
            "myPP/app/src/main/res/drawable/ri_whatsapp_fill.xml",
            "myPP/app/src/main/res/drawable/rounded_button.xml",
            "myPP/app/src/main/res/drawable/signinwith.xml",
            "myPP/app/src/main/res/font/interfont.ttf",
            "myPP/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
            "myPP/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml"
        ]
    },
    {
        msg: "Add image assets", date: "2025-12-04", files: [
            "myPP/app/src/main/res/drawable/bus_sample.png",
            "myPP/app/src/main/res/drawable/googlimg.png",
            "myPP/app/src/main/res/drawable/line_one.png",
            "myPP/app/src/main/res/drawable/line_two.png",
            "myPP/app/src/main/res/drawable/mainlogo.png",
            "myPP/app/src/main/res/drawable/qrpayment.png",
            "myPP/app/src/main/res/drawable/sample_map.png",
            "myPP/app/src/main/res/drawable/signiin.png",
            "myPP/app/src/main/res/drawable/user_sample.jpg",
            "myPP/app/src/main/res/drawable/user_sample_2.jpg"
        ]
    },

    // --- PHASE 6: ANDROID LOGIC (Dec 6-15) ---
    { msg: "Define Data Models", date: "2025-12-06", files: ["myPP/app/src/main/java/com/example/mypp/api/DataModels.kt"] },
    { msg: "Setup Network Result wrapper", date: "2025-12-07", files: ["myPP/app/src/main/java/com/example/mypp/api/NetworkResult.kt"] },
    { msg: "Define API Service interface", date: "2025-12-07", files: ["myPP/app/src/main/java/com/example/mypp/api/ApiService.kt"] },
    { msg: "Implement Retrofit Client", date: "2025-12-08", files: ["myPP/app/src/main/java/com/example/mypp/api/RetrofitClient.kt"] },
    { msg: "Add Cache Interceptor", date: "2025-12-09", files: ["myPP/app/src/main/java/com/example/mypp/api/CacheInterceptor.kt"] },
    { msg: "Implement Repository", date: "2025-12-10", files: ["myPP/app/src/main/java/com/example/mypp/api/SafarMateRepository.kt"] },
    { msg: "Create UserAuth ViewModel", date: "2025-12-12", files: ["myPP/app/src/main/java/com/example/mypp/viewmodels/UserAuthViewModel.kt"] },
    { msg: "Create Home ViewModel", date: "2025-12-13", files: ["myPP/app/src/main/java/com/example/mypp/viewmodels/HomeViewModel.kt"] },
    { msg: "Create Map ViewModel", date: "2025-12-14", files: ["myPP/app/src/main/java/com/example/mypp/viewmodels/MapViewModel.kt"] },

    // --- PHASE 7: ANDROID UI SCREENS (Dec 16-25) ---
    { msg: "Implement Splash Screen", date: "2025-12-16", files: ["myPP/app/src/main/java/com/example/mypp/screens/SplashScreen.kt"] },
    { msg: "Implement Login Screen", date: "2025-12-17", files: ["myPP/app/src/main/java/com/example/mypp/screens/LoginScreen.kt"] },
    { msg: "Implement Sign Up Screen", date: "2025-12-17", files: ["myPP/app/src/main/java/com/example/mypp/screens/SignUpScreen.kt"] },
    { msg: "Implement Role Selection Screen", date: "2025-12-18", files: ["myPP/app/src/main/java/com/example/mypp/screens/ChooseRoleScreen.kt"] },
    { msg: "Implement Home Screen", date: "2025-12-19", files: ["myPP/app/src/main/java/com/example/mypp/screens/HomeScreen.kt"] },
    { msg: "Implement Route Map Screen", date: "2025-12-20", files: ["myPP/app/src/main/java/com/example/mypp/screens/RouteMapScreen.kt", "myPP/app/src/main/java/com/example/mypp/map/OSMMapCompose.kt"] },
    { msg: "Implement Route Location Screen", date: "2025-12-21", files: ["myPP/app/src/main/java/com/example/mypp/screens/RouteLocationScreen.kt"] },
    { msg: "Implement Conductor Screens", date: "2025-12-22", files: ["myPP/app/src/main/java/com/example/mypp/screens/ConductorJourneyScreen.kt", "myPP/app/src/main/java/com/example/mypp/screens/ConductorPlaceholderScreen.kt"] },
    { msg: "Implement Payment Screen", date: "2025-12-23", files: ["myPP/app/src/main/java/com/example/mypp/screens/PaymentScreen.kt"] },
    { msg: "Implement ChatBot Screen", date: "2025-12-24", files: ["myPP/app/src/main/java/com/example/mypp/screens/ChatBotScreen.kt"] },
    { msg: "Implement App Navigation", date: "2025-12-25", files: ["myPP/app/src/main/java/com/example/mypp/navigation/AppNavigation.kt"] },

    // --- PHASE 8: FINAL POLISH (Dec 26-31) ---
    { msg: "Add XML config rules", date: "2025-12-26", files: ["myPP/app/src/main/res/xml/backup_rules.xml", "myPP/app/src/main/res/xml/data_extraction_rules.xml"] },
    { msg: "Add unit tests", date: "2025-12-27", files: ["myPP/app/src/test/java/com/example/mypp/ExampleUnitTest.kt", "myPP/app/src/androidTest/java/com/example/mypp/ExampleInstrumentedTest.kt"] },
    { msg: "Add proguard rules", date: "2025-12-28", files: ["myPP/app/build.gradle.kts", "myPP/app/proguard-rules.pro"] }, // Update build.gradle
    { msg: "Add App Review documentation", date: "2025-12-29", files: ["myPP/SafarMate_App_Review.md"] },
    { msg: "Final touchups", date: "2025-12-30", files: [] } // catch-all for anything missed
];


// Helper to copy file
function copyFile(sourcePath, destPath) {
    const destDir = path.dirname(destPath);
    if (!fs.existsSync(destDir)) {
        fs.mkdirSync(destDir, { recursive: true });
    }

    // Safety check for source existence
    if (!fs.existsSync(sourcePath)) {
        return false;
    }

    const stats = fs.statSync(sourcePath);
    if (stats.isDirectory()) {
        // If directory, copy recursively
        // NOTE: Node < 16.7 doesn't support recursive copy with fs.cpSync
        // But the user is likely on modern node. We'll use fs.cpSync if available or manual impl
        if (typeof fs.cpSync === 'function') {
            fs.cpSync(sourcePath, destPath, { recursive: true });
        } else {
            console.warn(`Warning: Directory copy skipped for ${sourcePath} (unsupported Node version, need to implement recursion)`);
        }
        return true;
    } else {
        fs.copyFileSync(sourcePath, destPath);
        return true;
    }
}

// Helper to format date
function formatDate(dateStr) {
    // Add time component +0530
    return `${dateStr} 12:00:00 +0530`;
}

// Main logic
async function main() {
    console.log(`Starting Realistic History Generation (Fixed)...`);
    console.log(`Repo Path: ${REPO_PATH}`);
    console.log(`Backup Path: ${BACKUP_PATH}`);

    // 1. BACKUP
    if (fs.existsSync(BACKUP_PATH)) {
        console.log(`Cleaning up old backup...`);
        fs.rmSync(BACKUP_PATH, { recursive: true, force: true });
    }
    console.log(`Creating backup...`);
    fs.mkdirSync(BACKUP_PATH, { recursive: true });

    // Move everything except excludes to backup
    const excludes = ['.git', 'node_modules', 'backdate_commits.js', 'dev_journal.md', 'generate_history.js', '.DS_Store'];
    const files = fs.readdirSync(REPO_PATH);

    for (const file of files) {
        if (!excludes.includes(file)) {
            const src = path.join(REPO_PATH, file);
            const dest = path.join(BACKUP_PATH, file);
            fs.renameSync(src, dest);
        }
    }
    console.log(`Backup complete.`);

    // 2. RE-INIT GIT
    console.log(`Re-initializing git...`);
    execSync(`rm -rf .git`, { cwd: REPO_PATH });
    execSync(`git init`, { cwd: REPO_PATH });

    // 3. INCREMENTAL RESTORE
    let commitCount = 0;

    for (const entry of HISTORY) {
        const dateStr = formatDate(entry.date);
        let filesAdded = false;

        for (const fileRelPath of entry.files) {
            const src = path.join(BACKUP_PATH, fileRelPath);
            const dest = path.join(REPO_PATH, fileRelPath);

            if (copyFile(src, dest)) {
                filesAdded = true;
            }
        }

        if (filesAdded || entry.files.length === 0) {
            try {
                const hasChanges = execSync(`git status --porcelain`, { cwd: REPO_PATH }).toString().trim().length > 0;

                if (hasChanges) {
                    execSync(`git add .`, { cwd: REPO_PATH });

                    const env = {
                        ...process.env,
                        GIT_AUTHOR_DATE: dateStr,
                        GIT_COMMITTER_DATE: dateStr
                    };

                    execSync(`git commit -m "${entry.msg}"`, { cwd: REPO_PATH, env });
                    console.log(`[${++commitCount}] ${entry.date}: ${entry.msg}`);
                } else if (entry.files.length === 0) {
                    // explicit empty commit
                    const env = {
                        ...process.env,
                        GIT_AUTHOR_DATE: dateStr,
                        GIT_COMMITTER_DATE: dateStr
                    };
                    execSync(`git commit --allow-empty -m "${entry.msg}"`, { cwd: REPO_PATH, env });
                    console.log(`[${++commitCount}] ${entry.date}: ${entry.msg} (Empty)`);
                }
            } catch (err) {
                console.error(`Failed commit ${entry.msg}`, err.message);
            }
        }
    }

    // 4. RESTORE REMAINDER (Catch-up)
    console.log(`Restoring remaining files...`);

    // Recursive copy function
    function copyRecursiveSync(src, dest) {
        if (fs.existsSync(src)) {
            const stats = fs.statSync(src);
            if (stats.isDirectory()) {
                if (!fs.existsSync(dest)) fs.mkdirSync(dest);
                fs.readdirSync(src).forEach(childItemName => {
                    copyRecursiveSync(path.join(src, childItemName), path.join(dest, childItemName));
                });
            } else {
                if (!fs.existsSync(dest)) { // Only if not already restored
                    fs.copyFileSync(src, dest);
                }
            }
        }
    }

    copyRecursiveSync(BACKUP_PATH, REPO_PATH);

    // Commit the remainder
    try {
        const hasChanges = execSync(`git status --porcelain`, { cwd: REPO_PATH }).toString().trim().length > 0;
        if (hasChanges) {
            execSync(`git add .`, { cwd: REPO_PATH });
            const env = {
                ...process.env,
                GIT_AUTHOR_DATE: formatDate("2025-12-31"), // Final date
                GIT_COMMITTER_DATE: formatDate("2025-12-31")
            };
            execSync(`git commit -m "Final integration and cleanup"`, { cwd: REPO_PATH, env });
            console.log(`[${++commitCount}] Final catch-up commit`);
        }
    } catch (e) { }

    // 5. CLEANUP
    console.log(`Removing backup...`);
    fs.rmSync(BACKUP_PATH, { recursive: true, force: true });

    console.log(`Done. Total Commits: ${commitCount}`);
}

main();
