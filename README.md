# Nihongo-IT

A application for learning IT Japanese terms.

## Features

- CRUD support for vocabulary, topics, and categories
- Audio playback for vocabulary terms
- Authentication and user roles
- Category and topic grouping for vocabulary
- Search and filter functionality
- JLPT level tagging
- Flashcard system for vocabulary practice
- Translation tools for sentences
- User progress tracking 
- Speech analysis and pronunciation practice (New!)

## Speech Analysis Feature

The application now includes Japanese speech analysis functionality:

### Features
- Pronunciation practice for vocabulary terms
- Real-time speech analysis and feedback
- Word-level accuracy highlighting
- Detailed scoring for intonation, clarity, and word accuracy
- Personalized feedback in Vietnamese

### Technical Implementation
- Frontend Vue component for recording and visualization
- Spring Boot backend services for audio processing
- Python FastAPI service for advanced speech analysis
- LLM integration for semantic analysis of pronunciation
- Visualization of speech patterns and pronunciation accuracy

## Project Structure

The project consists of:

- `nihongo-it-frontend`: Vue.js frontend application
- `nihongo-it-backend`: Spring Boot backend application
- `nihongo-it-python`: Python FastAPI service for speech analysis

## Getting Started

Follow the instructions in each project directory to set up the development environment.

### Prerequisites

- Node.js 16+ (for frontend)
- Java 17+ (for backend)
- Python 3.8+ (for speech analysis)
- PostgreSQL database
- OpenAI API key (for speech analysis)

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