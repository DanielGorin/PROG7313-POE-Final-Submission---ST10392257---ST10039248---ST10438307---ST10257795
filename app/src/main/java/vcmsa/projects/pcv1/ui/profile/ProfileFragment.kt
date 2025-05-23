package vcmsa.projects.pcv1.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import vcmsa.projects.pcv1.databinding.FragmentProfileBinding
import vcmsa.projects.pcv1.ui.auth.LandingActivity
import vcmsa.projects.pcv1.util.SessionManager
import vcmsa.projects.pcv1.util.DarkModeManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch username from session or placeholder
        val username = session.getUsername() ?: "User"
        binding.textUsername.text = username

        // Load image or show initial
        val profileImageUri = getProfileImageUri(requireContext()) // Replace with actual implementation
        if (profileImageUri != null) {
            binding.imageProfile.setImageURI(profileImageUri)
            binding.imageProfile.visibility = View.VISIBLE
            binding.textProfileInitial.visibility = View.GONE
        } else {
            binding.imageProfile.visibility = View.GONE
            binding.textProfileInitial.text = username.firstOrNull()?.uppercase() ?: "?"
            binding.textProfileInitial.visibility = View.VISIBLE
        }

        // Profile image click (future image picker)
        binding.imageProfile.setOnClickListener {
            // TODO: Launch image picker here
        }

        binding.btnLogout.setOnClickListener {
            session.clear()
            val intent = Intent(requireContext(), LandingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        // Dark mode toggle
        binding.switchDarkMode.isChecked = DarkModeManager.isDarkModeEnabled(requireContext())
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            DarkModeManager.setDarkMode(requireContext(), isChecked)
        }

        // TODO: Add handlers for btn_edit_profile, btn_change_password, etc.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Stub function (replace with actual saved image logic)
    private fun getProfileImageUri(context: android.content.Context): Uri? {
        // Return a saved URI if you persist it (e.g., SharedPreferences)
        return null
    }
}
