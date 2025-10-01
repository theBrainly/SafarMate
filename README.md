# SafarMate - Public Transport Companion

SafarMate is a comprehensive public transport companion application that connects passengers with bus services, provides real-time bus tracking, and simplifies the ticketing process. The application consists of a modern Jetpack Compose Android frontend and a Node.js Express backend.

## Project Overview

SafarMate aims to improve the public transport experience by:

- Providing real-time bus location tracking
- Displaying accurate ETAs for buses
- Simplifying the ticket booking and payment process
- Offering route planning capabilities
- Supporting both passenger and conductor experiences

## Frontend (Android)

The Android application is built entirely with modern Android development practices:

### Tech Stack

- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36 (Android 16.0 Upside Down Cake)

### Key Features

- **Material 3 Design**: Complete implementation of Material Design 3 with light/dark mode support
- **Role-Based Access**: Separate experiences for passengers and conductors
- **Real-Time Map Integration**: OpenStreetMap integration for live bus tracking
- **Location Services**: Google Play Services location integration
- **Navigation Component**: Streamlined navigation with NavHost and type-safe routes
- **Responsive UI**: Adapts to different screen sizes and orientations

### App Structure

- **UI Layer**: Compose-based screens in the `screens` package
- **Business Logic**: ViewModels in the `viewmodels` package
- **Data Layer**: Repository pattern with API services in the `api` package
- **Navigation**: Navigation Component with routes defined in `AppNavigation.kt`

### Screens

- **Splash Screen**: App introduction and onboarding
- **Choose Role Screen**: Select between passenger and conductor modes
- **Login/Signup Screens**: User authentication
- **Home Screen**: Main passenger dashboard
- **Route Map Screen**: Interactive map showing bus routes
- **Payment Screen**: Secure payment processing for tickets
- **ChatBot Screen**: AI assistant for user queries
- **Conductor Journey Screen**: Interface for bus conductors

### Dependencies

- **Navigation**: Navigation Compose
- **ViewModel**: Lifecycle ViewModel Compose
- **Networking**: Retrofit with OkHttp and Gson
- **Image Loading**: Coil for Compose
- **Maps**: OpenStreetMap (osmdroid)
- **Location**: Google Play Services Location

## Backend (Node.js)

The backend is built with Node.js and Express, providing RESTful API endpoints for the mobile application.

### Tech Stack

- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: MongoDB (Mongoose ORM)
- **Caching**: Redis
- **Authentication**: JWT (JSON Web Tokens)
- **Cloud Storage**: Cloudinary

### API Endpoints

The backend provides several categories of endpoints:

#### Bus Related

- Get bus location
- Calculate bus ETA to stops
- List buses on specific routes

#### Route Related

- List all bus stops
- Get route information for specific stops
- Calculate ETAs between points

#### User Authentication

- User registration and login
- Profile management
- Role-based access control

#### Communication Channels

- SMS notifications
- WhatsApp integration
- USSD service integration

### Backend Structure

The backend follows a modular architecture:

- **Controllers**: Business logic for different domains (bus, route, user, etc.)
- **Models**: MongoDB schemas (admin, bus, crew, route, ticket, user)
- **Routes**: API route definitions and handlers
- **Middlewares**: Authentication, file upload, etc.
- **Integrations**: External service integrations (caching, cloud storage)
- **Utils**: Helper functions and utility classes

## Development Setup

### Prerequisites

- Android Studio (latest version)
- JDK 11 or newer
- Node.js 16.x or newer
- MongoDB
- Redis (optional, for caching)

### Frontend Setup

1. Clone the repository
2. Open the `myPP` folder in Android Studio
3. Sync Gradle files
4. Run the application on an emulator or physical device

### Backend Setup

1. Navigate to the backend directory
2. Install dependencies:
   ```
   npm install
   ```
3. Set up environment variables (create a .env file based on .env.example)
4. Start the development server:
   ```
   npm run dev
   ```

### Docker Setup

For containerized development and deployment:

1. Build and start services:
   ```
   docker-compose up -d
   ```

2. The services will be available at the following ports:
   - Backend API: http://localhost:8000
   - MongoDB: mongodb://localhost:27017
   - Redis: redis://localhost:6379

## Architecture

SafarMate follows a client-server architecture with a clear separation of concerns:

### Mobile App (Client)

- Responsible for UI rendering and user interactions
- Implements offline-first approach where possible
- Handles local caching of frequently accessed data
- Manages user state and authentication tokens

### API Server (Backend)

- Provides authenticated access to data
- Processes business logic
- Interacts with the database and external services
- Handles data validation and sanitization

## Current Status and Roadmap

### Current Status

The application has a solid foundation with:
- Complete design system implementation
- Core navigation and screens
- API integration architecture
- Mock data implementation for development

### Future Enhancements

1. **Testing**: Add unit tests for ViewModels and UI tests for critical user flows
2. **Performance Monitoring**: Implement performance monitoring for API calls
3. **Accessibility**: Ensure all UI components meet accessibility guidelines
4. **Offline Support**: Enhance caching for better offline experience
5. **Dynamic Theming**: Implement Material You dynamic coloring on supported devices
6. **Localization**: Add support for multiple languages

## License

[MIT License](LICENSE)

## Contributors

- Akash Sharma - Project Lead & Developer

## Acknowledgements

- Material Design 3 Guidelines
- OpenStreetMap for map data
- Icons from Material Design Icons