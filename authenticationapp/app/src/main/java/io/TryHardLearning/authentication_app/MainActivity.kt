package io.TryHardLearning.authentication_app

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.TryHardLearning.authentication_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val REQ_ONE_TAP = 2
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Configuração do One Tap Sign-In
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id)) // Substituir pelo Client ID correto
                    .setFilterByAuthorizedAccounts(false) // Permitir qualquer conta
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        // Login com Email e Senha
        binding.btLogin.setOnClickListener {
            val email = binding.eLoginEmail.text.toString().trim()
            val password = binding.eLoginPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInUserWithEmailAndPassword(email, password)
            } else {
                Toast.makeText(this, "Todos os campos devem ser preenchidos", Toast.LENGTH_SHORT).show()
            }
        }

        // Login com Google One Tap
        binding.btGoogle.setOnClickListener {
            showAuthGoogle()
        }

        // Ir para tela de cadastro
        binding.btCadastro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showAuthGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        REQ_ONE_TAP, null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Erro ao iniciar One Tap: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Falha na autenticação Google: ${e.localizedMessage}")
                Toast.makeText(this, "Erro ao iniciar autenticação Google", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP) {
            try {
                val credential: SignInCredential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken

                if (idToken != null) {
                    Log.d(TAG, "ID Token recebido. Autenticando com Firebase...")
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Usuário autenticado com sucesso pelo One Tap")
                                Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.w(TAG, "Erro ao autenticar no Firebase: ${task.exception?.message}")
                                Toast.makeText(this, "Falha na autenticação: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Log.d(TAG, "Nenhum ID Token encontrado.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao processar resposta do One Tap: ${e.localizedMessage}")
            }
        }
    }

    private fun signInUserWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Login com email e senha bem-sucedido")
                    Toast.makeText(this, "Login realizado com sucesso", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "Erro ao autenticar: ${task.exception?.message}")
                    Toast.makeText(this, "Falha na autenticação: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val TAG = "AuthApp"
    }
}
