package vcmsa.projects.pcv1.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.databinding.FragmentProfileBinding
import vcmsa.projects.pcv1.ui.auth.LandingActivity
import vcmsa.projects.pcv1.util.SessionManager
import vcmsa.projects.pcv1.util.DarkModeManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            binding.imageProfile.setImageURI(uri)
            binding.textProfileInitial.visibility = View.GONE
            saveProfileImageUri(requireContext(), uri)
        }
    }


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
            try {
                val inputStream = requireContext().contentResolver.openInputStream(profileImageUri)
                inputStream?.close() // Test if the file is accessible

                binding.imageProfile.setImageURI(profileImageUri)
                binding.textProfileInitial.visibility = View.GONE
            } catch (e: Exception) {
                // URI invalid or file removed
                binding.imageProfile.setImageResource(R.drawable.circle_background)
                binding.textProfileInitial.text = username.firstOrNull()?.uppercase() ?: "?"
                binding.textProfileInitial.visibility = View.VISIBLE
            }
        } else {
            binding.imageProfile.setImageResource(R.drawable.circle_background)
            binding.textProfileInitial.text = username.firstOrNull()?.uppercase() ?: "?"
            binding.textProfileInitial.visibility = View.VISIBLE
        }

        // Profile image click (future image picker)
        binding.imageProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
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

    fun clearProfileImageUri(context: Context) {
        context.getSharedPreferences("profile", Context.MODE_PRIVATE)
            .edit().remove("profile_image_uri").apply()
    }

    fun saveProfileImageUri(context: Context, uri: android.net.Uri) {
        context.getSharedPreferences("profile", Context.MODE_PRIVATE)
            .edit().putString("profile_image_uri", uri.toString()).apply()
    }
    // Stub function (replace with actual saved image logic)
    private fun getProfileImageUri(context: android.content.Context): Uri? {
        // Return a saved URI if you persist it (e.g., SharedPreferences)
        return null
    }
}
