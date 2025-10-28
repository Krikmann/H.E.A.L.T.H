package ee.ut.cs.HEALTH.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository
import ee.ut.cs.HEALTH.domain.model.remote.ExerciseApi // <-- ADD THIS: Import your API interface
import ee.ut.cs.HEALTH.domain.model.routine.EnrichedRoutineItem
import ee.ut.cs.HEALTH.domain.model.routine.RestDurationBetweenExercisesId
import ee.ut.cs.HEALTH.domain.model.routine.RoutineId
import ee.ut.cs.HEALTH.domain.model.routine.SavedExercise
import ee.ut.cs.HEALTH.domain.model.routine.SavedRestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine
import ee.ut.cs.HEALTH.domain.model.routine.summary.RoutineSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async // <-- ADD THIS: For running API calls in parallel
import kotlinx.coroutines.awaitAll // <-- ADD THIS: For waiting for all calls to finish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull // <-- ADD THIS: To get a single value from a flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID // <-- ADD THIS: For creating unique IDs for rest items

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val repository: RoutineRepository,
    private val exerciseApi: ExerciseApi // <-- INJECT API: Add your API service here
) : ViewModel() {

    /**
     * Holds the user's search input string.
     */
    val query = MutableStateFlow("")

    /**
     * A flow of search results that automatically updates when the `query` flow changes.
     * `flatMapLatest` cancels the previous database query and starts a new one
     * as the user types, which is highly efficient.
     */
    val summaries: StateFlow<List<RoutineSummary>> = query
        .flatMapLatest { searchQuery ->
            repository.searchRoutineSummaries(searchQuery.trim())
        }
        .stateIn(
            scope = viewModelScope,
            // `WhileSubscribed` makes the flow active only when the UI is visible,
            // saving resources. The 5000ms timeout keeps it active during brief
            // configuration changes (like screen rotation).
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // The initial state before the first query completes.
        )

    /**
     * State for the currently selected routine.
     * `selectedId` tracks which routine is being viewed.
     * `selectedRoutine` holds the raw data for that routine from the database.
     * `enrichedRoutineItems` holds the final, combined list of steps with API data for the UI.
     */
    val selectedId = MutableStateFlow<Long?>(null)
    val selectedRoutine = MutableStateFlow<SavedRoutine?>(null)
    private val _enrichedRoutineItems = MutableStateFlow<List<EnrichedRoutineItem>>(emptyList())
    val enrichedRoutineItems: StateFlow<List<EnrichedRoutineItem>> = _enrichedRoutineItems


    // --- UI Events ---
    // These functions are called by the UI to signal user actions.

    /**
     * Updates the search query. Called every time the user types in the search field.
     */
    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    /**
     * Fetches the full details of a routine when the user selects it from the list.
     * This function now orchestrates a multi-step process:
     * 1. Fetches the routine structure from the local database.
     * 2. Extracts all unique exercise names from the routine.
     * 3. Fetches detailed exercise data (including image URLs) from the remote API for each name.
     * 4. Combines the database structure with the API data into a final "enriched" list for the UI.
     *
     * @param id The ID of the selected routine.
     */
    fun onRoutineSelect(id: Long) {
        selectedId.value = id
        viewModelScope.launch {
            // Step 1: Fetch the routine structure from the local database.
            // .firstOrNull() gets a single emission from the flow.
            println("--- onRoutineSelect started for id: $id ---")
            val routine = repository.getRoutine(RoutineId(id)).firstOrNull()
            selectedRoutine.value = routine

            if (routine == null) {
                // If the routine is not found, clear everything.
                println("!!! Routine with id $id not found in repository. Stopping.")
                _enrichedRoutineItems.value = emptyList()
                return@launch
            }
            println(">>> Found routine: ${routine.name}. Contains ${routine.routineItems.size} items.")
            // Step 2: Extract all unique exercise names to avoid duplicate API calls.
            val uniqueExerciseNames = routine.routineItems
                .filterIsInstance<SavedExercise>()
                .map { it.exerciseDefinition.name }
                .distinct()
            // Step 3: Fetch details for each unique exercise from the API in parallel.
            // 'async' starts each API call on a background thread.
            // 'associateWith' will create a map like: "Push-up" -> ExerciseDetailDto

            println(">>> Found ${uniqueExerciseNames.size} unique exercises to fetch: $uniqueExerciseNames")
            val detailsMap = uniqueExerciseNames.map { name ->
                async {

                    try {println(">>> Sending GET request for exercise name: '$name'")
                        val response = exerciseApi.searchExercisesByName(name)

                        if (response.isSuccessful) {
                            // response.body() on nüüd ApiResponse?
                            // Võtame sealt seest välja harjutuste nimekirja, mis on .exercises
                            val exerciseList = response.body()?.exercises

                            // Võtame sellest nimekirjast esimese harjutuse
                            val firstExercise = exerciseList?.firstOrNull()

                            if (firstExercise != null) {
                                println("<<< SUCCESS (${response.code()}): Found exercise '${firstExercise.name}' for query '$name'")
                                name to firstExercise
                            } else {
                                println("<<< SUCCESS but not found (${response.code()}): No exercise found in 'data' array for '$name'")
                                name to null
                            }
                        } else {
                            println("<<< ERROR (${response.code()}): Error body for '$name': ${response.errorBody()?.string()}")
                            name to null
                        }
                    }catch (e: Exception) {
                        // In case of a network error for one item, we map it to null.
                        System.err.println("!!! CATCH BLOCK: Error fetching '$name'. Exception Type: ${e.javaClass.simpleName}, Message: ${e.message}")

                        name to null
                    }
                }
            }.awaitAll().toMap() // 'awaitAll' waits for all async calls to complete.

            // Step 4: Combine the routine structure with the fetched API details.
            // This "unrolls" the sets and creates the final list for the UI.
            val enrichedItems = mutableListOf<EnrichedRoutineItem>()
            for (item in routine.routineItems) {
                when (item) {
                    is SavedExercise -> {
                        // Find the pre-fetched details for this exercise.
                        val details = detailsMap[item.exerciseDefinition.name]
                        for (i in 1..item.amountOfSets) {
                            // Add the exercise step with its API details.
                            enrichedItems.add(EnrichedRoutineItem(item, details))
                            // Add a rest period after the set, but not after the last one.
                            if (i < item.amountOfSets) {
                                val restItem = SavedRestDurationBetweenExercises(
                                    id = RestDurationBetweenExercisesId(System.currentTimeMillis() + enrichedItems.size),
                                    restDuration = item.recommendedRestDurationBetweenSets
                                )
                                enrichedItems.add(EnrichedRoutineItem(restItem, null)) // Rest has no details.
                            }
                        }
                    }
                    is SavedRestDurationBetweenExercises -> {
                        // Add standalone rest periods directly.
                        enrichedItems.add(EnrichedRoutineItem(item, null))
                    }
                }
            }
            _enrichedRoutineItems.value = enrichedItems
        }
    }

    /**
     * Clears the current selection, returning the user to the search list view.
     * Resets all related state variables.
     */
    fun onClearSelection() {
        selectedId.value = null
        selectedRoutine.value = null
        _enrichedRoutineItems.value = emptyList() // Also clear the enriched items.
    }
}

/**
 * Factory for creating a `SearchViewModel` with its dependencies.
 * This is necessary because the ViewModel now has multiple constructor parameters.
 *
 * @param repository The [RoutineRepository] for local database access.
 * @param exerciseApi The [ExerciseApi] for remote network access.
 */
class SearchViewModelFactory(
    private val repository: RoutineRepository,
    private val exerciseApi: ExerciseApi // <-- ADD API to factory
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository, exerciseApi) as T // <-- PASS API to constructor
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
