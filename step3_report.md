# H.E.A.L.T.H. App: API Integration Report

This report details the integration of an external API into the H.E.A.L.T.H. Android application. The goal of this integration is to allow users to search for and add new exercises to their workout routines, enriching the app's functionality.

## 1. Which API was chosen and why?

**API Chosen:** **ExerciseDB** (via RapidAPI platform)

We chose the ExerciseDB API for several key reasons:

*   **Rich and Relevant Data:** The API provides access to a database of over 1,300 exercises. 
*   **Detailed Information:** For each exercise, the API returns not just the name, but also a lot of information that we can later use
*   **Free and Accessible Tier:** The API has a free basic plan with 3000 requests a month on the RapidAPI platform, which is ideal for development, testing, and small-scale applications.
*   **Well-Defined Structure:** The API returns data in a predictable JSON format, which makes it easy to parse and map to Kotlin data classes.

## 2. Example API endpoint used

To search for exercises by their name, I used the following API endpoint provided by ExerciseDB:

GET https://exercisedb-api1.p.rapidapi.com/api/v1/exercises?name=plank

## 3. Error handling strategy

A robust error handling strategy was implemented to ensure the application remains stable and provides  feedback to the user if there is on network. The strategy is built on two key components within the `AddRoutineViewModel`:

1.  **A `SearchResult` Sealed Class:** we created a `sealed class` named `SearchResult` to represent all possible outcomes of an API call. This class has three states:
    *   `Success(val exercises: List<...>)`: Represents a successful API call. The list may be empty if no results were found, but the call itself was successful.
    *   `NoInternet`: Represents a state where the API call failed due to a network connection issue (e.g., an `IOException`).
    *   `ApiError(val code: Int, val message: String)`: Represents a state where the server responded with an error code (e.g., 404 Not Found, 500 Internal Server Error).

2.  **A `try-catch` Block with Response Checking:** The function that makes the API call is wrapped in a `try-catch` block.
    *   The `catch (e: IOException)` block handles network failures and maps the result to the `SearchResult.NoInternet` state.
    *   Inside the `try` block, after getting a response, the code checks `response.isSuccessful`. If it's false, the error code and message are read, and the result is mapped to the `SearchResult.ApiError` state.

