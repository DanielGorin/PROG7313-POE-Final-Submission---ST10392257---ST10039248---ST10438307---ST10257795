package vcmsa.projects.pcv1.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import vcmsa.projects.pcv1.databinding.FragmentProfileBinding
import vcmsa.projects.pcv1.ui.auth.LandingActivity
import vcmsa.projects.pcv1.util.SessionManager
import vcmsa.projects.pcv1.ui.auth.LoginActivity // Change this if your login activity is named differently
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

        binding.btnLogout.setOnClickListener {
            session.clear()
            val intent = Intent(requireContext(), LandingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        // Set switch to current theme
        binding.switchDarkMode.isChecked = DarkModeManager.isDarkModeEnabled(requireContext())

        // Toggle dark mode on change
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            DarkModeManager.setDarkMode(requireContext(), isChecked)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
