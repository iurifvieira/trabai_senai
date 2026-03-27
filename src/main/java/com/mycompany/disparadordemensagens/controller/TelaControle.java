package com.mycompany.disparadordemensagens.controller;

import com.mycompany.disparadordemensagens.database.Conexao;
import com.mycompany.disparadordemensagens.App;
import com.mycompany.disparadordemensagens.models.Contato;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TelaControle {

    @FXML
    private TextField nomelogin;
    @FXML
    private PasswordField senhalogin;
    @FXML
    private Button salvar;
    @FXML
    private Button limpar;

    @FXML
    private void entrarLogin() throws Exception {
        String nomeEntrar = nomelogin.getText();
        String senhaEntrar = senhalogin.getText();
        if (nomeEntrar.isEmpty() || senhaEntrar.isEmpty()) {
            mostrarAlerta("Erro:", "Nome e senha são obrigatórios!");
            return;
        }

        try (Connection conn = Conexao.conectar()) {
            String loginSql = "SELECT  id,  nome FROM usuarios WHERE UPPER(nome) = ? AND senha = ?";
            PreparedStatement loginStmt = conn.prepareStatement(loginSql);
            loginStmt.setString(1, nomeEntrar.toUpperCase());
            loginStmt.setString(2, senhaEntrar);
            ResultSet rslogin = loginStmt.executeQuery();

            if (!rslogin.next()) {
                mostrarAlerta("Erro", "Nome ou senha inválidos");
                return;
            }
          
           
            String nomeUsuario = rslogin.getString("nome");
            if (nomeUsuario.equalsIgnoreCase("admin")) {
                App.setRoot("Admin");
            } else {
                App.setRoot("Usuario");
           
            }
        }
    }

    @FXML
    private void limparLogin() {
        nomelogin.clear();
        senhalogin.clear();
    }

    @FXML
    private void cadastrarNovoUsuario() {
        try {
            App.setRoot("cadastro");
        } catch (Exception e) {
            mostrarAlerta("Erro", " ao mudar para tela de cadastro: " + e.getMessage());
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
