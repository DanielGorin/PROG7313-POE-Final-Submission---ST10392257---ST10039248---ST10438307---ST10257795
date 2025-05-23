package vcmsa.projects.pcv1.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.UserRepository
import vcmsa.projects.pcv1.databinding.FragmentProfileBinding
import vcmsa.projects.pcv1.ui.auth.LandingActivity
import vcmsa.projects.pcv1.util.DarkModeManager
import vcmsa.projects.pcv1.util.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager
    private lateinit var repo: UserRepository
    private var currentUserId: Int = -1

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                // Request persistent URI permission
                requireContext().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // ignore if permission not needed or already granted
            }

            binding.imageProfile.setImageURI(it)
            binding.textProfileInitial.visibility = View.GONE

            lifecycleScope.launch {
                repo.updateProfileImage(currentUserId, it.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        currentUserId = session.getUserId()
        repo = UserRepository(AppDatabase.getInstance(requireContext()).userDao())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = session.getUsername() ?: "User"
        binding.textUsername.text = username

        lifecycleScope.launch {
            val uriString = repo.getProfileImage(currentUserId)
            if (!uriString.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(uriString)
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    inputStream?.close() // Validate URI access
                    binding.imageProfile.setImageURI(uri)
                    binding.textProfileInitial.visibility = View.GONE
                } catch (e: Exception) {
                    setFallbackImage(username)
                }
            } else {
                setFallbackImage(username)
            }
        }

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

        binding.switchDarkMode.isChecked = DarkModeManager.isDarkModeEnabled(requireContext())
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            DarkModeManager.setDarkMode(requireContext(), isChecked)
        }
    }

    private fun setFallbackImage(username: String) {
        binding.imageProfile.setImageResource(R.drawable.circle_background)
        binding.textProfileInitial.text = username.firstOrNull()?.uppercase() ?: "?"
        binding.textProfileInitial.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
