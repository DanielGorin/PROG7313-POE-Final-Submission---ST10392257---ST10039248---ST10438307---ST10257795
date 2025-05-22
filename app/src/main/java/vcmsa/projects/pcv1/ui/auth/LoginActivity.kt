package vcmsa.projects.pcv1.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.ui.main.MainActivity
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.UserRepository
import vcmsa.projects.pcv1.databinding.ActivityLoginBinding
import vcmsa.projects.pcv1.util.SessionManager

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var repo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = UserRepository(AppDatabase.getInstance(this).userDao())
        session = SessionManager(this)

        binding.btnSubmit.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (username.isEmpty() || password.isEmpty()) return@setOnClickListener

            lifecycleScope.launch {
                repo.login(username, password).onSuccess { user ->
                    session.saveUserId(user.id)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finishAffinity()
                }.onFailure {
                    binding.tvError.text = it.message
                }
            }
        }
    }
}