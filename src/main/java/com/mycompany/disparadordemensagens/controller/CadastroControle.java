package com.mycompany.disparadordemensagens.controller;

import com.mycompany.disparadordemensagens.database.Conexao;
import com.mycompany.disparadordemensagens.App;
import com.mycompany.disparadordemensagens.models.Contato;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement; 
import javafx.scene.control.Alert;

public class CadastroControle {

    @FXML
    private TextField nomeField;
    @FXML
    private TextField telefoneField;
    @FXML
    private PasswordField senhaField;
    @FXML
    private Button voltar;

    @FXML
    private void cadastrar() throws Exception {
        String nome = nomeField.getText();
        String telefone = telefoneField.getText();
        String senha = senhaField.getText();

        if (nome.isEmpty()) {
            mostrarAlerta("Erro", "O campo nome passou vazio!");
            return;
        }
        if (!nome.matches("[\\p{L} ]+")) {
            mostrarAlerta("Erro", "Nome deve conter apenas letras!");
            return;
        }
        if (telefone.isEmpty()) {
            mostrarAlerta("Erro", "O campo de telefone passou vazio");
            return;
        }
        if (!telefone.matches("\\d{11}")) {
            mostrarAlerta("Erro", "Apenas 11 numeros de telefone");
            return;
        }
        if (senha.isEmpty()) {
            mostrarAlerta("Erro", "O campo de senha passou vazio!");
            return;
        }
        if (senha.length() < 8) {
            mostrarAlerta("Erro", "A senha deve ter no minimo 8 caracteres!");
            return;
        }

        // Verifica telefone duplicado
        try (Connection conn = Conexao.conectar()) {
            String checkTelefoneSql = "SELECT * FROM usuarios WHERE numeroTelefone = ?";
            PreparedStatement checkTelefoneStmt = conn.prepareStatement(checkTelefoneSql);
            checkTelefoneStmt.setString(1, telefone);
            ResultSet rsTelefone = checkTelefoneStmt.executeQuery();
            if (rsTelefone.next()) {
                mostrarAlerta("Erro", "Telefone já cadastrado!");
                return;
            }
        }

        // Insere e recupera o id gerado pelo banco
        try (Connection conn = Conexao.conectar()) {
            String sql = "INSERT INTO usuarios (nome, numeroTelefone, senha) VALUES (?, ?, ?)";

            // 
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, nome);
            stmt.setString(2, telefone);
            stmt.setString(3, senha);
            stmt.executeUpdate();

            // ✅ Lê o id gerado e cria a sessão com o id correto
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);
                Contato novoUsuario = new Contato(idGerado, nome, telefone);
                Sessao.setUsuarioLogado(novoUsuario);
            }

            mostrarAlerta("Sucesso", "Usuário cadastrado com sucesso!");
            try {
                App.setRoot("Usuario");
            } catch (Exception e) {
                mostrarAlerta("Erro", "Ao mudar para tela de Usuário: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void limpar() {
        nomeField.clear();
        telefoneField.clear();
        senhaField.clear();
    }

    @FXML
    private void voltarLogin() {
        try {
            App.setRoot("tela");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}