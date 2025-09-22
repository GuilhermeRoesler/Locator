package com.example.locator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: ProgressBar

    // URL da sua API de login
    private val apiUrl = "https://souls.pythonanywhere.com/api/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializa os componentes da UI
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)

        // Define o listener de clique para o botão de login
        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Mostra a ProgressBar e desabilita o botão enquanto a requisição é feita
                setLoading(true)
                // Inicia a requisição à API em uma coroutine
                performLogin(username, password)
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        // Usa o lifecycleScope para garantir que a coroutine seja cancelada se a activity for destruída
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Cria o cliente OkHttp
                val client = OkHttpClient()

                // Cria o corpo da requisição (JSON)
                val jsonObject = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }
                val requestBody = jsonObject.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())

                // Cria a requisição POST
                val request = Request.Builder()
                    .url(apiUrl)
                    .post(requestBody)
                    .build()

                // Executa a requisição e obtém a resposta
                val response = client.newCall(request).execute()

                // Muda para a thread principal para manipular a resposta
                withContext(Dispatchers.Main) {
                    setLoading(false) // Esconde a ProgressBar
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            // Extrai o 'id' do JSON de resposta
                            val responseJson = JSONObject(responseBody)
                            val userId = responseJson.optInt("user_id", -1) // -1 como valor padrão caso não encontre

                            if (userId != -1) {
                                // Se o login for bem-sucedido, inicia a MainActivity
                                Toast.makeText(applicationContext, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                                navigateToMain(userId)
                            } else {
                                // Caso a resposta não contenha o 'id' esperado
                                Toast.makeText(applicationContext, "Erro: ID de usuário não encontrado.", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        // Trata respostas de erro (ex: 401, 404)
                        Toast.makeText(applicationContext, "Erro: Usuário ou senha inválidos.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                // Trata exceções de rede ou outras
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(applicationContext, "Erro de conexão: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Navega para a MainActivity, passando o ID do usuário.
     */
    private fun navigateToMain(userId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_ID", userId)
        }
        startActivity(intent)
        finish() // Finaliza a LoginActivity para que o usuário não possa voltar a ela pressionando "back"
    }

    /**
     * Controla a visibilidade da ProgressBar e o estado do botão de login.
     */
    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            buttonLogin.isEnabled = false
            buttonLogin.alpha = 0.5f
        } else {
            progressBar.visibility = View.GONE
            buttonLogin.isEnabled = true
            buttonLogin.alpha = 1.0f
        }
    }
}
