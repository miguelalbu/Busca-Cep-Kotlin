package com.example.miniprojetomobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private val REQUEST_PERMISSION_CODE = 1001

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

        val buttonExport = Button(this).apply {
            text = "Exportar e Compartilhar CSV"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }

        layout.addView(editTextCep)
        layout.addView(buttonSearch)
        layout.addView(textViewResult)
        layout.addView(progressBar)
        layout.addView(buttonExport)

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

        buttonExport.setOnClickListener {
            // Verifica se a permissão é necessária ou se o dispositivo usa Android 10 ou superior (Scoped Storage)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q || isPermissionGranted()) {
                exportToCsvAndShare() // Exporta o CSV e compartilha
            } else {
                requestStoragePermission() // Solicita permissão de armazenamento para versões mais antigas
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

    private fun isPermissionGranted(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Scoped Storage não exige permissão
            true
        } else {
            // Verifica permissão para versões anteriores ao Android 10
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Toast.makeText(this, "Permissão não é necessária no Android 10 ou superior.", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_CODE
            )
        }
    }


    private fun exportToCsvAndShare() {
        val savedData = databaseHelper.getAllJsonData()

        try {
            // Cria o arquivo CSV no diretório de arquivos externos privados do app
            val file = File(getExternalFilesDir(null), "cep_data.csv")
            val writer = FileWriter(file)

            writer.append("Logradouro,Bairro,Cidade,Estado\n")

            savedData.forEach { json ->
                val jsonResponse = JSONObject(json)
                val logradouro = jsonResponse.optString("logradouro", "Não encontrado")
                val bairro = jsonResponse.optString("bairro", "Não encontrado")
                val cidade = jsonResponse.optString("localidade", "Não encontrado")
                val uf = jsonResponse.optString("uf", "Não encontrado")

                writer.append("$logradouro,$bairro,$cidade,$uf\n")
            }

            writer.flush()
            writer.close()

            // Após salvar o arquivo, chamar a função de compartilhamento
            shareCsv(file)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao exportar dados: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun shareCsv(file: File) {
        try {
            // Obtém a URI para o arquivo usando o FileProvider
            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            // Cria um intent para compartilhar o arquivo CSV
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri) // Anexa o arquivo
                putExtra(Intent.EXTRA_SUBJECT, "Arquivo CSV")
                putExtra(Intent.EXTRA_TEXT, "Segue em anexo o arquivo CSV.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Concede permissão para ler o arquivo
            }

            // Inicia a atividade para compartilhar o arquivo
            startActivity(Intent.createChooser(intent, "Compartilhar CSV via:"))
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao compartilhar o arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show()
                exportToCsvAndShare()
            } else {
                Toast.makeText(this, "Permissão de armazenamento negada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
