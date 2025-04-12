# Nihongo IT

This project has been renamed from "Japanese IT Learning" to "Nihongo IT".

## Structure

- `nihongo-it-frontend`: Vue.js frontend application
- `nihongo-it-backend`: Spring Boot backend application

## Important Migration Notes

The project was renamed from "japanese-it-learning" to "nihongo-it", including:

1. Directory structure changes:
   - Main project: `japanese-it-learning` → `nihongo-it`
   - Frontend: `japanese-it-learning-frontend` → `nihongo-it-frontend`
   - Backend: `japanese-it-learning-backend` → `nihongo-it-backend`

2. Backend changes:
   - Package name: `com.example.japanesitlearning` → `com.example.nihongoit`
   - Class names: `JapaneseItLearning*` → `NihongoIt*`
   - Application file: `JapaneseItLearningBackendApplication.kt` → `NihongoItBackendApplication.kt`

3. Frontend changes:
   - Package name in package.json
   - Display name in UI components
   - Page titles

## Setup

### Backend
1. Navigate to the backend directory: `cd nihongo-it-backend`
2. Run the application: `./gradlew bootRun`

### Frontend
1. Navigate to the frontend directory: `cd nihongo-it-frontend`
2. Install dependencies: `npm install`
3. Run the development server: `npm run dev`

## Note for Developers

If you encounter any issues related to the name change, please check:
- Package imports in backend code
- Path references in configuration files
- API endpoint URLs in frontend code 