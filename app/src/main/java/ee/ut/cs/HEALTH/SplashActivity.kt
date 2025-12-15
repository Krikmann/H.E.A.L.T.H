package ee.ut.cs.HEALTH

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

/**
 * An activity that displays a splash screen upon application startup.
 *
 * This screen is shown for a fixed duration before automatically redirecting
 * the user to the main part of the application, defined in [MainActivity].
 */
class SplashActivity : Activity() {

    /**
     * Called when the activity is first created.
     * This function sets up a delayed transition to the [MainActivity].
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down, this Bundle contains the
     *                           data it most recently supplied in [onSaveInstanceState].
     *                           Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }
}
