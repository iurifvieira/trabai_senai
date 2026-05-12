package com.mycompany.disparadordemensagens.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mycompany.disparadordemensagens.App;
import com.mycompany.disparadordemensagens.models.Contato;
import com.mycompany.disparadordemensagens.controller.UsuarioControle;
import com.mycompany.disparadordemensagens.database.Conexao;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class PerfilUsuarioControle {
    @FXML
    private ImageView fotoUsuario;
    @FXML
    private Label NomeUsuario;
    @FXML
    private Label TelefoneUsuario;
    @FXML
    private Label EmailUsuario;

    private static final String PASTA_FOTOS = "fotos_perfil/";

    void setUsuario() {
        NomeUsuario.setText(Sessao.getUsuarioLogado().getNome());
        TelefoneUsuario.setText("Tel: " + Sessao.getUsuarioLogado().getNumeroTelefone());
        EmailUsuario.setText("Email: " + Sessao.getUsuarioLogado().getEmail());

        String fotoPath = getFotoPerfilUsuario(Sessao.getUsuarioLogado().getId());

        if (fotoPath != null && !fotoPath.isEmpty()) {
            File fotoFile = new File(fotoPath);
            if (fotoFile.exists()) {
                Image img = new Image(fotoFile.toURI().toString());
                fotoUsuario.setImage(img);

                // aplicar recorte circular
                double radius = Math.min(fotoUsuario.getFitWidth(), fotoUsuario.getFitHeight()) / 2;
                Circle clip = new Circle(fotoUsuario.getFitWidth() / 2, fotoUsuario.getFitHeight() / 2, radius);
                fotoUsuario.setClip(clip);
            } else {
                carregarAvatarPadrao();
            }
        } else {

            carregarAvatarPadrao();
        }

    }

    private String getFotoPerfilUsuario(int usuarioId) {
        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT foto_perfil FROM usuarios WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String foto = rs.getString("foto_perfil");
                if (foto != null && !foto.isEmpty()) {
                    return foto;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Abre foto em tela cheia para melhor visualização
     * 
     * @author iuri
     */
    @FXML
    private void abrirFotoTelaCheia() {
        if (fotoUsuario.getImage() != null) {
            Stage stage = new Stage();
            stage.setTitle("Foto do usuário");

            ImageView fullImageView = new ImageView(fotoUsuario.getImage());
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
            fotoUsuario.setImage(new Image(url.toString()));
        }
    }

    private void salvarFotoPerfilUsuario(int usuarioId, String fotoPath) {
        try (Connection conn = Conexao.conectar()) {
            String sql = "UPDATE usuarios SET foto_perfil = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, fotoPath);
            stmt.setInt(2, usuarioId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Função para alterar foto de perfil
     * 
     * @since 30/04/2026
     * @author Iuri
     */

    @FXML
    private void Editar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.jfif"));

        Stage stage = (Stage) fotoUsuario.getScene().getWindow();
        File arquivoSelecionado = fileChooser.showOpenDialog(stage);

        if (arquivoSelecionado != null) {
            try {
                // Criar pasta se não existir
                File pasta = new File(PASTA_FOTOS);
                if (!pasta.exists()) {
                    pasta.mkdirs();
                }

                // Verificar se já existe foto anterior e excluir
                String fotoPath = getFotoPerfilUsuario(Sessao.getUsuarioLogado().getId());
                if (fotoPath != null) {
                    File fotoAntiga = new File(fotoPath);
                    if (fotoAntiga.exists()) {
                        fotoAntiga.delete();
                        System.out.println("Foto anterior excluída: " + fotoAntiga.getName());
                    }
                }

                // Copiar arquivo para pasta de fotos
                String extensao = getExtensao(arquivoSelecionado.getName());
                File destino = new File(PASTA_FOTOS + "perfil_" + Sessao.getUsuarioLogado().getId() + extensao);

                try (FileInputStream fis = new FileInputStream(arquivoSelecionado);
                        FileOutputStream fos = new FileOutputStream(destino)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }

                // Atualizar ImageView
                Image image = new Image(destino.toURI().toString());
                fotoUsuario.setImage(image);

                // Salvar caminho completo no banco
                String caminhoCompleto = destino.getAbsolutePath();
                salvarFotoPerfilUsuario(Sessao.getUsuarioLogado().getId(), caminhoCompleto);

                mostrarAlerta("Sucesso", "Foto de perfil alterada com sucesso!");
                aplicarClipCircular(fotoUsuario);

            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Erro", "Falha ao alterar foto de perfil.");
            }
        }
    }

    private String getExtensao(String nomeArquivo) {
        int lastDot = nomeArquivo.lastIndexOf('.');
        return (lastDot > 0) ? nomeArquivo.substring(lastDot) : ".png";
    }
/**
 * deixa a foto circular
 * @param imgView
 */
    private void aplicarClipCircular(ImageView imgView) {
        double radius = Math.min(imgView.getFitWidth(), imgView.getFitHeight()) / 2;
        Circle clip = new Circle(imgView.getFitWidth() / 2, imgView.getFitHeight() / 2, radius);
        imgView.setClip(clip);
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

}
