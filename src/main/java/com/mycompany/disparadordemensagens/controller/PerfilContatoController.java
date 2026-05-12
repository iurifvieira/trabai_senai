package com.mycompany.disparadordemensagens.controller;

import java.io.File;

import com.mycompany.disparadordemensagens.App;
import com.mycompany.disparadordemensagens.models.Contato;
import com.mycompany.disparadordemensagens.controller.UsuarioControle;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;

public class PerfilContatoController {
    @FXML
    private ImageView fotoContato;
    @FXML
    private Label NomeContato;
    @FXML
    private Label TelefoneContato;
    @FXML
    private Label EmailContato;
    // @FXML private Label labelId;
    /**
     * Recebe os dados do contato em uma tela
     * 
     * @param contato
     */
    void setContato(Contato contato) {
        NomeContato.setText(contato.getNome());
        TelefoneContato.setText("Telefone: " + contato.getNumeroTelefone());
        EmailContato.setText("Email: " + contato.getEmail());

        String fotoPath = contato.getFotoPerfil();
        if (fotoPath != null && !fotoPath.isEmpty()) {
            File fotoFile = new File(fotoPath);
            if (fotoFile.exists()) {
                Image img = new Image(fotoFile.toURI().toString());
                fotoContato.setImage(img);

                // aplicar recorte circular
                double radius = Math.min(fotoContato.getFitWidth(), fotoContato.getFitHeight()) / 2;
                Circle clip = new Circle(fotoContato.getFitWidth() / 2, fotoContato.getFitHeight() / 2, radius);
                fotoContato.setClip(clip);
            } else {
                carregarAvatarPadrao();
            }
        } else {

            carregarAvatarPadrao();
        }

    }

    /**
     * Abre foto em tela cheia para melhor visualização
     * 
     * @author iuri
     */
    @FXML
    private void abrirFotoTelaCheia() {
        if (fotoContato.getImage() != null) {
            Stage stage = new Stage();
            stage.setTitle("Foto do Contato");

            ImageView fullImageView = new ImageView(fotoContato.getImage());
            fullImageView.setPreserveRatio(true);
            fullImageView.setFitWidth(600); // largura da tela cheia
            fullImageView.setFitHeight(600); // altura da tela cheia

            StackPane root = new StackPane(fullImageView);
            root.setStyle("-fx-background-color: black;"); // fundo preto para destacar

            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
        }
    }

    /**
     * Carrega foto em branco (foto padrão), caso usuário não possua foto
     * 
     */
    private void carregarAvatarPadrao() {
        var url = getClass().getResource("/com/mycompany/disparadordemensagens/img/avatar.jpg");
        if (url != null) {
            fotoContato.setImage(new Image(url.toString()));
        }
    }

    @FXML
    private void logout() {
        NomeContato.getScene().getWindow().hide();

    }
}
