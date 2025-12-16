# H.E.A.L.T.H.
## Home Exercise and Activity Level Tracker

## Project Overview
**H.E.A.L.T.H.** is a modern Android application designed for creating and using workout routines. It provides a user-friendly platform for everyone, from beginners to seasoned athletes, to manage their fitness journey effectively.  
The app is built using modern Android technologies with a focus on a clean, reactive architecture.

---

## Team Members
- **Kristo Krikmann** – Good Computer Man (Krikmann)
- **Kaur Kivilaan** – Cyber Wolf, Lead Mockup Designer (Kaurcode)
- **Jonathan Astmäe** – Skibidi-Web Master (Asttar)
- **Agnes Kivistik** – Dark Knower (Agnes-Kivistik, 77991133)

---

## Main Features & User Stories
This application is a personal fitness companion designed to help users build and maintain a consistent workout habit. The focus is on creating, tracking, and analyzing personal training data locally on the user's device.

---

## 1. Create and Customize Your Own Workouts
- **User Story:**  
  *As someone who knows what exercises I like, I want to create my own workout plans from scratch so they fit my personal goals and schedule.*
- The app features a powerful routine builder where users can add repetition- or duration-based exercises, specify sets, weight, and rest times, and arrange them in any order.

---

## 2. Follow Your Plan with an Interactive Player
- **User Story:**  
  *As an athlete, I want an app to guide me through my workout, tracking my exercises and rest times, so I can focus on my performance instead of a stopwatch.*
- The interactive workout player guides users through each step of the selected routine, with built-in timers for exercises and rest periods, ensuring a structured and effective training session.

---

## 3. Track Your Progress and Build Habits
- **User Story:**  
  *As someone trying to build a workout habit, I want to see my progress and history over time so I can stay motivated and see how far I've come.*
- The app provides a detailed workout history and a statistics dashboard with charts. Users can view weekly and monthly activity, track progress toward goals, and identify their most frequently completed routines, reinforcing positive habits.
---

## Installation and Build
To build and run this project, you will need **Android Studio** and a configured Android emulator or a physical device.

### 1. Clone the Repository
Clone this project to your local machine using your preferred Git client or the command line:
```
git clone https://github.com/Krikmann/H.E.A.L.T.H.git
cd H.E.A.L.T.H
```
### 2. Add API Key
Add your API key to the `local.properties` file in the following format:

```
properties
RAPIDAPI_KEY="YOUR_API_KEY_HERE"
```
3. Select a run configuration (an emulator or a connected physical device).
4. Click the **Run** button (▶️).

---

## Tools and Frameworks
This project leverages a modern Android tech stack:

- **UI:** Jetpack Compose for building the entire user interface declaratively
- **Architecture:** MVVM (Model-View-ViewModel) with a clear separation of concerns
- **Asynchronicity:** Kotlin Coroutines and Flow for managing background tasks and reactive data streams
- **Database:** Room for local, persistent storage of routines, user profiles, and workout history
- **Networking:** Retrofit and OkHttp for communication with the remote exercise API
- **Navigation:** Navigation for Compose for handling screen transitions
- **Charts:** Vico for displaying beautiful and interactive charts on the Home and Stats screens
- **Language:** 100% Kotlin

---

## Project Structure Explained
The project follows a feature-oriented architecture that separates concerns into distinct layers, making the codebase scalable and easy to maintain.

```
Kotlin
└── app/src/main/java/ee/ut/cs/HEALTH/
    ├── data/                # Data handling layer
    │   ├── local/           # Room Database (DAOs, Entities, DTOs)
    │   ├── remote/          # Retrofit API definitions
    │   └── repository/      # Single source of truth for data
    │
    ├── domain/model/        # Core business logic and data classes
    │   ├── remote/          # Models for API data
    │   └── routine/         # Core models for routines, exercises, etc.
    │
    ├── ui/                  # UI layer (Jetpack Compose)
    │   ├── components/      # Reusable UI widgets
    │   ├── navigation/      # Navigation graph and destinations
    │   ├── screens/         # Composable functions for each app screen
    │   └── theme/           # App theme, colors, and typography
    │
    └── viewmodel/           # ViewModels for each screen
```

---

## Demo video

[Video link](https://drive.google.com/file/d/1eWg6FuNHENTFjedIvQLTrfIT58-FmEFw/view?usp=sharing)
