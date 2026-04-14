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
import java.util.regex.Pattern;
import javafx.scene.control.Label;

public class CadastroControle {

    @FXML
    private TextField nomeField;
    @FXML
    private TextField telefoneField;
    @FXML
    private PasswordField senhaField;
    @FXML
    private TextField emailField;
    @FXML
    private Button voltar;
    @FXML
    private Label labelTelefoneCount;

    @FXML
    private void cadastrar() throws Exception {
        String nome = nomeField.getText();
        String telefone = telefoneField.getText();
        String senha = senhaField.getText();
        String email = emailField.getText();

        if (email.isEmpty()) {
            mostrarAlerta("Erro", "O campo de e-mail está vazio!");
            return;
        }
        //valida se o email vai passar com o '@gmail.com
        if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@"
                + "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            mostrarAlerta("Erro", "Formato de e-mail inválido!");
            return;
        }

        //validações dos campos
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

        try (Connection conn = Conexao.conectar()) {
            String checkEmailSql = "SELECT * FROM usuarios WHERE email = ?";
            PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailSql);
            checkEmailStmt.setString(1, email);
            ResultSet rsEmail = checkEmailStmt.executeQuery();

            if (rsEmail.next()) {
                mostrarAlerta("Erro", "Este  Email já está cadastrado!");
                return;
            }
        }

        // Insere e recupera o id gerado pelo banco
        try (Connection conn = Conexao.conectar()) {
            String sql = "INSERT INTO usuarios (nome, numeroTelefone, senha, email) VALUES (?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, nome);
            stmt.setString(2, telefone);
            stmt.setString(3, senha);
            stmt.setString(4, email);
            stmt.executeUpdate();

            // o id gerado e cria a sessão com o id correto
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);
                Contato novoUsuario = new Contato(idGerado, nome, telefone, email);
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

    @FXML   //função para perca de foco dos campos
    public void initialize() {
        // Ao pressionar Enter no campo nome → vai para telefone
        emailField.setOnAction(e -> nomeField.requestFocus());

        // Ao pressionar Enter no campo telefone → vai para email
        nomeField.setOnAction(e -> telefoneField.requestFocus());

        // Ao pressionar Enter no campo email → vai para senha
        telefoneField.setOnAction(e -> senhaField.requestFocus());
    }

    @FXML //conta quantos digitos foram feitos no campo de telefone
    public void contagemTel() {
        telefoneField.textProperty().addListener((obs, oldValue, newValue) -> {
            // remove caracteres não numéricos
            String digits = newValue.replaceAll("[^\\d]", "");

            // atualiza contador
            labelTelefoneCount.setText(digits.length() + "/11 dígitos");

            // limita a 11 dígitos
            if (digits.length() > 11) {
                digits = digits.substring(0, 11);
            }

            // atualiza campo sem caracteres inválidos
            if (!newValue.equals(digits)) {
                telefoneField.setText(digits);
                telefoneField.positionCaret(digits.length());
            }
        });
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
