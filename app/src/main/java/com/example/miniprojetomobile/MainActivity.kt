package com.example.miniprojetomobile

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseHelper = DatabaseHelper(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val editTextCep = EditText(this).apply {
            hint = "Digite o CEP"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }

        val buttonSearch = Button(this).apply {
            text = "Buscar"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }

        val textViewResult = TextView(this).apply {
            text = "Resultado:"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.START
            setPadding(16)
        }

        val progressBar = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            visibility = android.view.View.GONE
        }

        layout.addView(editTextCep)
        layout.addView(buttonSearch)
        layout.addView(textViewResult)
        layout.addView(progressBar)

        setContentView(layout)

        buttonSearch.setOnClickListener {
            val cep = editTextCep.text.toString()

            if (cep.isNotEmpty()) {
                progressBar.visibility = android.view.View.VISIBLE
                textViewResult.text = "Buscando dados..."
                fetchCepData(cep, textViewResult, progressBar)
            } else {
                textViewResult.text = "Por favor, digite um CEP."
            }
        }
    }

    private fun fetchCepData(cep: String, textViewResult: TextView, progressBar: ProgressBar) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://viacep.com.br/ws/$cep/json/")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = InputStreamReader(connection.inputStream).readText()
                    val jsonResponse = JSONObject(response)

                    if (jsonResponse.has("erro")) {
                        launch(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "CEP inválido", Toast.LENGTH_SHORT).show()
                            textViewResult.text = "CEP não encontrado."
                        }
                    } else {
                        val logradouro = jsonResponse.optString("logradouro", "Não encontrado")
                        val bairro = jsonResponse.optString("bairro", "Não encontrado")
                        val cidade = jsonResponse.optString("localidade", "Não encontrado")
                        val uf = jsonResponse.optString("uf", "Não encontrado")


                        val jsonData = jsonResponse.toString()
                        databaseHelper.insertJsonData(jsonData)

                        launch(Dispatchers.Main) {
                            textViewResult.text = """
                                Logradouro: $logradouro
                                Bairro: $bairro
                                Cidade: $cidade
                                Estado: $uf
                            """.trimIndent()
                        }
                    }
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Erro na resposta do servidor", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erro ao buscar dados: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                launch(Dispatchers.Main) {
                    progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }


    private fun loadAllSavedData() {
        val savedData = databaseHelper.getAllJsonData()
        savedData.forEach { json ->
            println(json)
        }
    }
}
