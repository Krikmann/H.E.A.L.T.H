package ee.ut.cs.HEALTH.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Defines all possible navigation destinations in the application.
 *
 * This enum class centralizes route management, providing a type-safe way to handle
 * navigation. Each destination holds metadata required for both navigation graphs
 * and UI components like the bottom navigation bar.
 *
 * @property label The text to be displayed in UI elements, such as a navigation bar item. Nullable if not used.
 * @property icon The [ImageVector] to be displayed for this destination in the navigation bar. Nullable if not used.
 * @property contentDescription A description for accessibility services for the icon. Nullable if not used.
 * @property route The unique string path used by the NavController to navigate to this screen.
 *                 It can include arguments in the format `path/{argument}`.
 */
enum class NavDestination(
    val label: String? = null,
    val icon: ImageVector? = null,
    val contentDescription: String? = null,
    val route: String
) {
    /** The main dashboard screen. */
    HOME("Home", Icons.Filled.Home, "Home", "home"),

    /** The screen for searching, previewing, and starting routines. Includes an optional routineId to open directly. */
    SEARCH("Search",
        Icons.Filled.Search, "Search", "search?routineId={routineId}"),

    /** The screen for creating a new routine. */
    ADD("Add", Icons.Filled.Add, "Add", "add"),

    /** The screen that displays the user's workout history. */
    STATS("Stats", Icons.Filled.Done, "Stats", "stats"),

    /** The screen that displays the user's profile information. */
    PROFILE("Profile",
        Icons.Filled.AccountCircle, "Profile", "profile"),

    /** The screen for editing the user's profile. Not shown in the main navigation bar. */
    EDITPROFILE(label = "EditProfile", icon = Icons.Filled.AccountCircle, contentDescription = "Edit Profile", route = "editprofile"),

    /** The screen for displaying the details of a single exercise. Not shown in the main navigation bar. */
    EXERCISE_DETAIL(route="exercise_detail/{exerciseId}")
}
