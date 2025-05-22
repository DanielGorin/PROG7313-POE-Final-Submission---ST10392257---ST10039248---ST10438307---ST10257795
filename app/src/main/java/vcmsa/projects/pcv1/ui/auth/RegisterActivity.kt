package vcmsa.projects.pcv1.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.ui.main.MainActivity
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.UserRepository
import vcmsa.projects.pcv1.databinding.ActivityRegisterBinding
import vcmsa.projects.pcv1.util.SessionManager

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = UserRepository(AppDatabase.getInstance(this).userDao())
        session = SessionManager(this)

        binding.btnSubmit.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (!validate(username, password)) return@setOnClickListener

            lifecycleScope.launch {
                repo.register(username, password).onSuccess { user ->
                    session.saveUserId(user.id)
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finishAffinity()
                }.onFailure {
                    binding.tvError.text = it.message
                }
            }
        }
    }

    private fun validate(username: String, password: String): Boolean {
        if (username.isEmpty()) { binding.etUsername.error = "Required"; return false }
        if (password.length < 8 || !password.any { it.isDigit() } || !password.any { it.isLetter() }) {
            binding.etPassword.error = "Password must be 8+ chars, include letters and digits"
            return false
        }
        return true
    }
}
