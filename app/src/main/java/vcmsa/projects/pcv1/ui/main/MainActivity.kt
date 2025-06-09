// Takudzwa Murwira – ST10392257, Jason Daniel Isaacs – ST10039248, Daniel Gorin – ST10438307 and Moegammad-Yaseen Salie – ST10257795
//PROG7313

//References:
//            https://medium.com/@SeanAT19/how-to-use-mpandroidchart-in-android-studio-c01a8150720f
//            https://chatgpt.com/
//            https://www.youtube.com/playlist?list=PLWz5rJ2EKKc8SmtMNw34wvYkqj45rV1d3
//            https://www.youtube.com/playlist?list=PLSrm9z4zp4mEPOfZNV9O-crOhoMa0G2-o
package vcmsa.projects.pcv1.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import vcmsa.projects.pcv1.R.id.mobile_navigation
import vcmsa.projects.pcv1.R.id.nav_host_fragment_activity_main
import vcmsa.projects.pcv1.databinding.ActivityMainBinding
import vcmsa.projects.pcv1.util.DarkModeManager
import vcmsa.projects.pcv1.util.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout and initialize session manager
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)
        // Apply dark mode based on saved preference
        DarkModeManager.setDarkMode(applicationContext, DarkModeManager.isDarkModeEnabled(applicationContext))

        // Check if user is logged in; finish activity if not
        if (!session.isLoggedIn()) {
            finish() // or redirect to login
            return
        }

        // Setup BottomNavigationView
        val navController = supportFragmentManager.findFragmentById(nav_host_fragment_activity_main)
            ?.findNavController()
        binding.navView.setupWithNavController(navController!!)

    }
}
