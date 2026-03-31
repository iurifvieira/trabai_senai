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

    private int id;

    @FXML
    private void entrarLogin() throws Exception {

        String nomeEntrar = nomelogin.getText();
        String senhaEntrar = senhalogin.getText();

        if (nomeEntrar.isEmpty() || senhaEntrar.isEmpty()) {
            mostrarAlerta("Erro", "Nome e senha são obrigatórios!");
            return;
        }

        try (Connection conn = Conexao.conectar()) {

            String loginSql = """
            SELECT id, nome
            FROM usuarios
            WHERE UPPER(nome) = ? AND senha = ?
        """;

            PreparedStatement stmt = conn.prepareStatement(loginSql);
            stmt.setString(1, nomeEntrar.toUpperCase());
            stmt.setString(2, senhaEntrar);

            ResultSet rs = stmt.executeQuery();

          if (rs.next()) {
                // Login com sucesso
                System.out.println("Login realizado! ID: " + rs.getInt("id"));
                // Proxima tela...
            } else {
                // Falha no login
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Usuário ou senha inválidos.");
                alert.show();
            }

            int id = rs.getInt("id");
            String nome = rs.getString("nome");

            Contato usuario = new Contato(id, nome, nomeEntrar);
            Sessao.setUsuarioLogado(usuario);

            if (nome.equalsIgnoreCase("admin")) {
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
