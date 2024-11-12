# **Busca CEP - Projeto Android**

## 🚀 Descrição

Este projeto consiste em um **aplicativo Android** que permite ao usuário realizar a busca de informações de um CEP (Código de Endereçamento Postal) diretamente no serviço **ViaCEP**. As informações obtidas são exibidas ao usuário e, em seguida, salvas em um banco de dados **SQLite**. Além disso, os dados podem ser exportados para um arquivo **CSV** para facilitar a visualização e o compartilhamento das informações.

---

## 🎯 Funcionalidades

- **Busca de CEP**: O usuário insere um CEP e o aplicativo faz uma consulta no serviço **ViaCEP** para obter dados como logradouro, bairro, cidade e estado.
- **Armazenamento local**: As informações obtidas são salvas no banco de dados **SQLite** para futuras consultas.
- **Exportação de dados**: Os dados salvos no banco podem ser exportados para um arquivo **CSV**, facilitando a visualização e permitindo que os dados sejam compartilhados com outros aplicativos.
- **Interface simples**: Interface de usuário fácil de usar, com um campo de texto para entrada do CEP, um botão de busca e uma área para exibição do resultado.

---

## 🛠️ Tecnologias Utilizadas

- **Android Studio**: IDE para desenvolvimento do aplicativo.
- **Kotlin**: Linguagem de programação principal.
- **SQLite**: Banco de dados local para armazenamento das informações.
- **ViaCEP API**: API gratuita para busca de dados de endereços a partir de um CEP.
- **CSV**: Formato de arquivo utilizado para exportar dados de maneira simples.

---

## 📦 Como Rodar o Projeto

### 1. **Clone o Repositório**

Clone o repositório para a sua máquina local:

```bash
git clone https://github.com/seu-usuario/busca-cep.git
