package io.TryHardLearning.authentication_app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.TryHardLearning.authentication_app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btNewUser.setOnClickListener {
            val email = binding.eEmail.text.toString().trim()
            val password1 = binding.ePassword1.text.toString().trim()
            val password2 = binding.ePassword2.text.toString().trim()

            if (email.isNotEmpty() && password1.isNotEmpty() && password2.isNotEmpty()) {
                if (password1 == password2) {
                    createUserWithEmailAndPassword(email, password1)
                } else {
                    Toast.makeText(this, "As senhas devem ser iguais", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Todos os campos devem ser preenchidos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUserWithEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "User created successfully")
                Toast.makeText(this, "Usuário cadastrado com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Log.w(TAG, "Error creating user: ${task.exception?.message}")
                Toast.makeText(this, "Erro ao criar usuário: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "EmailAndPassword"
    }

}
