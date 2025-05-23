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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)
        DarkModeManager.setDarkMode(applicationContext, DarkModeManager.isDarkModeEnabled(applicationContext))


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
