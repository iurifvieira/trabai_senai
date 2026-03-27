package com.mycompany.disparadordemensagens.controller;

import com.mycompany.disparadordemensagens.database.Conexao;
import com.mycompany.disparadordemensagens.App;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.scene.control.Alert;

public class CadastroControle {

    // Campos de texto e botões ligados ao FXML
    @FXML
    private TextField nomeField;       // Campo para digitar o nome
    @FXML
    private TextField telefoneField;   // Campo para digitar o telefone
    @FXML
    private PasswordField senhaField;  // Campo para digitar a senha
    @FXML
    private Button voltar;             // Botão "Voltar" 

    // Método chamado ao clicar em "Cadastrar"
    @FXML
    private void cadastrar() throws Exception {
        // Pega os valores digitados nos campos
        String nome = nomeField.getText();
        String telefone = telefoneField.getText();
        String senha = senhaField.getText();
      
        // Valida se os campos estão preenchidos
        if (nome.isEmpty()) {
            mostrarAlerta("Erro", "O campo nome passou vazio!");
            return;
        }
        // Valida se o nome contém apenas letras e espaços
        if (!nome.matches("[A-Za-z ]+")) {
            mostrarAlerta("Erro", "Nome deve conter apenas letras!");
            return;
        }
        if(telefone.isEmpty()){
            mostrarAlerta("Erro", "O campo de telefone passou vazio");
        }
        if (senha.isEmpty()) {
            mostrarAlerta("Erro", "O campo de senha passou vazio!");
            return;
        }

        try (Connection conn = Conexao.conectar()) {
            // Verifica se já existe usuário com mesmo telefone
            String checkTelefoneSql = "SELECT * FROM usuarios WHERE numeroTelefone = ?";
            PreparedStatement checkTelefoneStmt = conn.prepareStatement(checkTelefoneSql);
            checkTelefoneStmt.setString(1, telefone);
            ResultSet rsTelefone = checkTelefoneStmt.executeQuery();

            if (rsTelefone.next()) {
                mostrarAlerta("Erro", "Telefone já cadastrado!");
                return;
            }
        }
        try (Connection conn = Conexao.conectar()) {
            //verifica  se ja existe admin
            String checkAdminSql = "SELECT  * FROM usuarios WHERE LOWER(nome) = 'admin' ";
            PreparedStatement checkAdminStmt = conn.prepareStatement(checkAdminSql);
            ResultSet rsAdmin = checkAdminStmt.executeQuery();

            if (rsAdmin.next() && nome.equalsIgnoreCase("admin")) {
                mostrarAlerta("Erro", "Já existe um admin cadastrado");
                return;
            }
        }
        // Se passou nas validações, tenta salvar no banco
        try (Connection conn = Conexao.conectar()) {
            String sql = "INSERT INTO usuarios (nome, numeroTelefone, senha) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nome);
            stmt.setString(2, telefone);
            stmt.setString(3, senha);
            stmt.executeUpdate();

            mostrarAlerta("Sucesso", "Usuário cadastrado com sucesso!");

            if (nome.equalsIgnoreCase("admin")) {
                App.setRoot("Admin");
            } else {
                App.setRoot("Usuario");
            }
        } catch (Exception e) {
            mostrarAlerta("Erro", "Falha ao cadastrar no banco: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Método chamado ao clicar em "Limpar"
    @FXML
    private void limpar() {
        // Limpa os campos de texto
        nomeField.clear();
        telefoneField.clear();
        senhaField.clear();
    }

    // Método chamado ao clicar em "Voltar para login"
    @FXML
    private void voltarLogin() {
        try {
            App.setRoot("tela"); // Volta para a tela de login (tela.fxml)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//eqwqeqe
    // Método auxiliar para mostrar mensagens de erro em pop-up

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR); // Cria alerta do tipo erro
        alert.setTitle(titulo);                         // Define título da janela
        alert.setHeaderText(null);                      // Remove cabeçalho
        alert.setContentText(mensagem);                 // Define mensagem
        alert.showAndWait();                            // Exibe e espera o usuário fechar
    }
}
