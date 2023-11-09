package uk.ac.abertay.plannorfunctions.nav

import androidx.annotation.StringRes
import uk.ac.abertay.plannorfunctions.R

// Best practice to store the route URL in a sealed class as its private nad can only be mod with-in this class
sealed class AppRoutes(val route: String, @StringRes val resourceId: Int) {
    object Home : AppRoutes("home", R.string.title_activity_contractor_screen)
    object Contractor : AppRoutes("contract",  R.string.title_activity_home_screen )
}