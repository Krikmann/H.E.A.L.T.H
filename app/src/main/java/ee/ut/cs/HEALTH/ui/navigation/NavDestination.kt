package ee.ut.cs.HEALTH.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavDestination(
    val label: String? = null,
    val icon: ImageVector? = null,
    val contentDescription: String? = null,
    val route: String
) {
    HOME("Home", Icons.Default.Home, "Home", "home"),
    SEARCH("Search", Icons.Default.Search, "Search", "search"),
    ADD("Add", Icons.Default.Add, "Add", "add"),
    STATS("Stats", Icons.Default.Done, "Stats", "stats"),
    PROFILE("Profile", Icons.Default.AccountCircle, "Profile", "profile"),
    EDITPROFILE("EditProfile", Icons.Default.AccountCircle, "Edit Profile", "editprofile"),

    EXERCISE_DETAIL(route="exercise_detail/{exerciseName}")

}
