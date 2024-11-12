package com.example.miniprojetomobile

data class CepData(
    val logradouro: String,
    val bairro: String,
    val cidade: String,
    val uf: String
) {
    fun toJson(): String {
        return """{
            "logradouro": "$logradouro",
            "bairro": "$bairro",
            "cidade": "$cidade",
            "uf": "$uf"
        }""".trimIndent()
    }
}
