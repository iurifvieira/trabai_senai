# Disparador de Mensagens - SENAI

Projeto de um sistema disparador de mensagens desenvolvido em Java com interface gráfica utilizando JavaFX para as atividades do SENAI.

---

##  Tecnologias e Ferramentas Utilizadas
- **Linguagem:** Java
- **Interface Gráfica:** JavaFX (telas em formato `.fxml`)
- **Gerenciador de Dependências:** Maven (`pom.xml`)
- **Ambiente de Desenvolvimento:** NetBeans / VS Code

## 📁 Estrutura Básica do Projeto
- `src/main/java`: Contém o código-fonte principal, incluindo a classe inicial `App.java`.
- `src/main/resources`: Guarda os arquivos visuais das telas (como o `Tela.fxml`).
- `pom.xml`: Configurações de dependências do projeto e plugins do JavaFX.

##  Como executar o projeto localmente

1. **Pré-requisitos:** Certifique-se de ter o **JDK (Java Development Kit)** instalado na sua máquina.
2. **Clonar o repositório:**
   ```bash
   git clone https://github.com
   ```
3. **Executar a aplicação:** Abra o terminal na raiz do projeto e execute o comando abaixo para compilar e abrir a tela do sistema:
   ```bash
   mvn exec:java -Dexec.mainClass="com.mycompany.disparadordemensagens.App"
   ```

##  Autores
- [Iuri Vieira](https://github.com)

---
Desenvolvido como critério de avaliação no SENAI - 2025/2026.
