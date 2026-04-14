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
//classe para login de usuario 

public class TelaControle {

    @FXML
    private PasswordField senhalogin;
    @FXML
    private TextField emaillogin;
    @FXML
    private Button salvar;
    @FXML
    private Button limpar;

    private int id;

    @FXML
    private void entrarLogin() throws Exception {
        String emailEntrar = emaillogin.getText().trim();
        String senhaEntrar = senhalogin.getText().trim();

        if (emailEntrar.isEmpty() || senhaEntrar.isEmpty()) {
            mostrarAlerta("Erro", "E-mail e senha são obrigatórios!");
            return;
        }

        try (Connection conn = Conexao.conectar()) {
            String loginSql = "SELECT id, nome, numeroTelefone, email, senha FROM usuarios WHERE email = ? AND senha = ?";
            PreparedStatement stmt = conn.prepareStatement(loginSql);
            stmt.setString(1, emailEntrar);
            stmt.setString(2, senhaEntrar);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                String telefone = rs.getString("numeroTelefone");
                String email = rs.getString("email");

                Contato usuario = new Contato(id, nome, telefone, email);
                Sessao.setUsuarioLogado(usuario);

                try {
                    App.setRoot("Usuario");
                } catch (Exception e) {
                    mostrarAlerta("Erro", "Falha ao abrir tela de Usuário: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                mostrarAlerta("Erro", "E-mail ou senha incorretos!");
            }
        }
    }

    @FXML
    private void limparLogin() {
        emaillogin.clear();
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

    @FXML
    private void abrirTelaRedefinirSenha() {
        try {
            App.setRoot("EsqueciSenha"); // nome do arquivo FXML da tela de redefinição
        } catch (Exception e) {
            mostrarAlerta("Erro", "Não foi possível abrir a tela de redefinição.");
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
