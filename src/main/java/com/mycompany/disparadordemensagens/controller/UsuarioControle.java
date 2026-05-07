package com.mycompany.disparadordemensagens.controller;

import com.mycompany.disparadordemensagens.database.Conexao;
import com.mycompany.disparadordemensagens.models.Contato;
import com.mycompany.disparadordemensagens.App;
import com.mycompany.disparadordemensagens.models.Mensagem;
import com.mycompany.disparadordemensagens.controller.PerfilContatoController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.Bindings;

import javafx.collections.ListChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javafx.scene.shape.Circle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Controlador da interface de usuário responsável por gerenciar contatos,
 * mensagens e perfil do usuário logado.
 *
 * @since 30/04/2026
 * @author Iuri
 */
public class UsuarioControle {

    @FXML // carrega lista de contatos
    private ListView<Contato> listaContatos;
    @FXML
    private TextArea historicoMensagens;
    @FXML
    private TextField campoAssuntoEnvio;
    @FXML
    private TextArea campoMensagem;
    @FXML // campo onde seleciona a prioridade da mensage: alta/baixa/media
    private ComboBox<String> comboPrioridade;
    @FXML // Quantos usuarios selecionados para o disparo de mensagens
    private Label labelContagem;
    @FXML
    private Label labelStatusSistema;
    @FXML
    private TextField campoPesquisa;
    @FXML
    private ScrollPane scrollHistorico;
    @FXML
    private Label labelSelecionarManual;
    @FXML
    private ImageView imagemPerfil;
    @FXML
    private Label labelNomeUsuario;
    @FXML
    private Label labelTelefoneUsuario;
    @FXML
    private Label labelEmailUsuario;
    // Label que vai mostrar o relógio
    @FXML
    private Label labelRelogio;

    // fotos de perfis
    private static final String PASTA_FOTOS = "fotos_perfil/";
    // Foto inicial quando usuario faz cadastro
    private static final String FOTO_DEFAULT = "perfil-default.png";

    private Map<Contato, List<Mensagem>> historicoMensagensMap = new HashMap<>();
    private List<Contato> todosUsuarios = new ArrayList<>();

    /**
     * Inicializa a interface do usuário, carregando contatos, configurando relógio,
     * filtros de pesquisa e elementos visuais.
     *
     * @since 30/04/2026
     * @author Iuri
     */
    @FXML
    public void initialize() {
        listaContatos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // inicia o relogio digital
        iniciarRelogio();
        // função para quando não for encontrado usuario na pesquisa aparecer esta
        // mensagem
        Label placeholder = new Label("Nenhum contato encontrado");
        placeholder.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");

        // A filtragem do usuário logado é feita dentro do while abaixo
        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT id, nome, numeroTelefone, email, foto_perfil FROM usuarios";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                String telefone = rs.getString("numeroTelefone");
                String email = rs.getString("email");
                String fotoPerfil = rs.getString("foto_perfil");
                // Filtra o usuário logado aqui
                if (Sessao.getUsuarioLogado() == null || id != Sessao.getUsuarioLogado().getId()) {
                    Contato contato = new Contato(id, nome, telefone, email);
                    contato.setFotoPerfil(fotoPerfil);
                    listaContatos.getItems().add(contato);
                    todosUsuarios.add(contato);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            labelStatusSistema.setText("Erro ao carregar usuários");
        }

        labelContagem.textProperty().bind(
                Bindings.size(listaContatos.getSelectionModel().getSelectedItems())
                        .asString()
                        .concat(" usuários selecionados"));

        comboPrioridade.getItems().addAll("Alta", "Média", "Baixa");
        comboPrioridade.setValue("Média");

        listaContatos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            atualizarVisualizacao();
        });
        // Faz seleção de usuario manualmente, surge uma mensagem "selecionar
        // manualmente"
        listaContatos.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Contato>) change -> {
            if (isCtrlPressed() && listaContatos.getSelectionModel().getSelectedItems().size() > 1) {
                labelSelecionarManual.setText("Selecionar manualmente");
            } else {
                labelSelecionarManual.setText("");
            }
        });

        listaContatos.setOnKeyPressed(event -> {
            if (event.isControlDown() && listaContatos.getSelectionModel().getSelectedItems().size() > 1) {
                labelSelecionarManual.setText("Selecionar manualmente");
            }
        });
        listaContatos.setOnKeyReleased(event -> {
            if (!event.isControlDown()) {
                labelSelecionarManual.setText("");
            }
        });
        // pega a string digitada da sua pesquisa e entra no metodo
        campoPesquisa.textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                FuncPesquisar();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Carregar foto de perfil
        carregarFotoPerfil();

        // Exibir nome/email/telefone do usuário logado
        if (Sessao.getUsuarioLogado() != null) {
            labelNomeUsuario.setText(Sessao.getUsuarioLogado().getNome());
            labelTelefoneUsuario.setText("Tel: " + Sessao.getUsuarioLogado().getNumeroTelefone());
            labelEmailUsuario.setText("Email: " + Sessao.getUsuarioLogado().getEmail());
        }
        // define como o fundo da lista
        listaContatos.setPlaceholder(placeholder);
        // Configurar célula personalizada para a lista de contatos
        configurarCelulaContato();

        // Garante que o scroll acompanhe a última linha
        Platform.runLater(() -> {
            // Define a posição do cursor no final do texto total
            historicoMensagens.setScrollTop(Double.MAX_VALUE);
            historicoMensagens.selectPositionCaret(historicoMensagens.getLength());
            historicoMensagens.deselect();
        });

    }

    /**
     * Configura a célula da lista de contatos, exibindo imagem, nome e email.
     * Também adiciona evento de duplo clique para abrir o perfil.
     *
     * @since 30/04/2026
     * @author Iuri
     */
    private void configurarCelulaContato() {
        // Carrega imagem do usuario, nome e email
        listaContatos.setCellFactory(listView -> new ListCell<Contato>() {
            private final HBox hbox = new HBox(10);
            private final ImageView imageView = new ImageView();
            private final VBox vbox = new VBox(2);
            private final Label nomeLabel = new Label();
            private final Label emailLabel = new Label();

            {

                // Configurar ImageView
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);

                // Configurar labels
                nomeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                nomeLabel.setStyle("-fx-text-fill: #a8a0a0;");
                emailLabel.setFont(Font.font("System", 11));
                emailLabel.setStyle("-fx-text-fill: #7f8c8d;");

                vbox.getChildren().addAll(nomeLabel, emailLabel);
                VBox.setMargin(nomeLabel, new Insets(0, 0, 0, 0));
                VBox.setMargin(emailLabel, new Insets(2, 0, 0, 0));

                hbox.getChildren().addAll(imageView, vbox);
                HBox.setMargin(imageView, new Insets(5, 10, 5, 5));
            }

            @Override
            protected void updateItem(Contato contato, boolean empty) {
                super.updateItem(contato, empty);

                if (empty || contato == null) {
                    setGraphic(null);
                    return;
                }
                // Carrega nome e email do contato
                nomeLabel.setText(contato.getNome());
                emailLabel.setText(contato.getEmail());

                // Carregar foto do contato
                String fotoPath = contato.getFotoPerfil();
                if (fotoPath != null && !fotoPath.isEmpty()) {
                    File fotoFile = new File(fotoPath);
                    if (fotoFile.exists()) {
                        try {
                            Image img = new Image(fotoFile.toURI().toString());
                            imageView.setImage(img);
                            aplicarClipCircular(imageView);
                        } catch (Exception e) {
                            carregarImagemDefault(imageView);
                        }
                    } else {
                        carregarImagemDefault(imageView);
                    }
                } else {
                    carregarImagemDefault(imageView);
                }
                // Torna a célula clicável
                imageView.setStyle("-fx-cursor: hand;");
                imageView.setOnMouseClicked(event -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/com/mycompany/disparadordemensagens/PerfilContato.fxml"));
                        Parent root = loader.load();

                        PerfilContatoController controller = loader.getController();
                        controller.setContato(contato); // passa o contato clicado

                        Stage stage = new Stage();
                        stage.setTitle("Perfil do contato ");
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                setGraphic(hbox);
            }

            /**
             * Funcão para carregar imagem padrão caso usuário não haja foto
             * 
             * @param imgView
             */
            private void carregarImagemDefault(ImageView imgView) {
                try {
                    var url = getClass().getResource("/com/mycompany/disparadordemensagens/img/avatar.jpg");
                    if (url != null) {
                        Image defaultImage = new Image(url.toURI().toString());
                        imgView.setImage(defaultImage);
                        aplicarClipCircular(imgView);
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    /**
     * Função para carregar foto do usuário logado. Caso não exista, usa imagem
     * padrão.
     * 
     * @since 30/04/2026
     * @author Iuri
     */
    private void carregarFotoPerfil() {
        try {
            // Criar pasta de fotos se não existir
            File pasta = new File(PASTA_FOTOS);
            if (!pasta.exists()) {
                pasta.mkdirs();
            }

            // Verificar se usuário tem foto salva no banco
            String fotoPath = null;
            if (Sessao.getUsuarioLogado() != null) {
                fotoPath = getFotoPerfilUsuario(Sessao.getUsuarioLogado().getId());
                System.out.println("Foto do banco: " + fotoPath);
            }

            File fotoFile = (fotoPath != null) ? new File(fotoPath) : null;
            if (fotoFile != null && fotoFile.exists()) {
                System.out.println("Carregando foto do arquivo: " + fotoFile.getAbsolutePath());
                Image image = new Image(fotoFile.toURI().toString());
                imagemPerfil.setImage(image);
                // aplica metodo da imagem
                aplicarClipCircular(imagemPerfil);
            } else {
                // Usar imagem padrão
                System.out.println("Usando imagem padrão...");
                try {
                    var url = getClass().getResource("/com/mycompany/disparadordemensagens/img/avatar.jpg");
                    if (url != null) {
                        Image defaultImage = new Image(url.toURI().toString());
                        if (!defaultImage.isError()) {
                            imagemPerfil.setImage(defaultImage);
                            System.out.println("Imagem padrão carregada com sucesso!");
                        }
                    } else {
                        System.out.println("URL da imagem padrão é null");
                    }
                } catch (Exception e) {
                    System.out.println("Erro ao carregar imagem padrão: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtém o caminho da foto de perfil do usuário no banco de dados.
     * 
     * @author Iuri
     * @since 30/04/2026
     * @param usuarioId ID do usuário logado
     * @return Caminho da foto ou null se não existir
     */
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
            // aplica metodo da imagem
            aplicarClipCircular(imagemPerfil);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Salva o caminho da foto de perfil do usuário no banco de dados.
     *
     * @author Iuri
     * @since 30/04/2026
     * @param usuarioId ID do usuário logado
     * @param fotoPath  Caminho da foto a ser salva
     */
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
     * Abre a janela de configurações do perfil, permitindo alterar a foto.
     * 
     * @since 30/04/2026
     * @author Iuri
     */
    @FXML
    private void abrirConfiguracoesPerfil() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Configurações de Perfil");
        alert.setHeaderText("Alterar Foto de Perfil");
        alert.setContentText("Deseja alterar sua foto de perfil?");

        ButtonType buttonSim = new ButtonType("Alterar Foto");
        ButtonType buttonCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonSim, buttonCancelar);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonSim) {
                alterarFotoPerfil();
            }
        });
    }

    /**
     * Função para alterar foto de perfil
     * 
     * @since 30/04/2026
     * @author Iuri
     */

    private void alterarFotoPerfil() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", ".*jfif"));

        Stage stage = (Stage) imagemPerfil.getScene().getWindow();
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

                // Copiar arquivo para pasta de fotos (sem timestamp para substituir)
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
                imagemPerfil.setImage(image);

                // Salvar caminho completo no banco de dados
                String caminhoCompleto = destino.getAbsolutePath();
                salvarFotoPerfilUsuario(Sessao.getUsuarioLogado().getId(), caminhoCompleto);

                mostrarAlerta("Sucesso", "Foto de perfil alterada com sucesso!");
                // aplica metodo da imagem
                aplicarClipCircular(imagemPerfil);

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

    private boolean isCtrlPressed() {
        return listaContatos.getScene() != null
                && listaContatos.getScene().getAccelerators() != null;
    }

    /**
     * Envia uma mensagem para os contatos selecionados, salvando no banco de dados
     * e atualizando o histórico.
     *
     * @throws Exception Caso ocorra erro de conexão ou inserção no banco
     * @since 30/04/2026
     * @author Iuri
     */
    @FXML
    private void enviarMensagem() throws Exception {
        List<Contato> contatosSelecionados = listaContatos.getSelectionModel().getSelectedItems();
        String assunto = campoAssuntoEnvio.getText();
        String conteudo = campoMensagem.getText();
        String prioridade = comboPrioridade.getValue();

        if (contatosSelecionados.isEmpty()) {
            mostrarAlerta("Erro", "Selecione pelo menos um contato.");
            return;
        }
        if (assunto.isEmpty()) {
            mostrarAlerta("Atenção", "O campo de assunto está vazio");
            return;
        }
        if (conteudo.isEmpty()) {
            mostrarAlerta("Atenção", "O campo de mensagens está vazio");
            return;
        }

        try (Connection conn = Conexao.conectar()) {
            String sqlEnviar = "INSERT INTO mensagens (destinatario_id, assunto, mensagem, prioridade, datahora, remetente_id) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sqlEnviar);

            for (Contato c : contatosSelecionados) {
                Mensagem msg = new Mensagem(c.getId(), assunto, conteudo, prioridade,
                        Sessao.getUsuarioLogado().getId());
                stmt.setInt(1, msg.getDestinatarioId());
                stmt.setString(2, msg.getAssunto());
                stmt.setString(3, msg.getMensagem());
                stmt.setString(4, msg.getPrioridade());
                stmt.setTimestamp(5, Timestamp.valueOf(msg.getDataHora()));
                stmt.setInt(6, Sessao.getUsuarioLogado().getId());
                stmt.executeUpdate();

                historicoMensagensMap.computeIfAbsent(c, k -> new ArrayList<>()).add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Falha ao enviar mensagem.");
        }

        limparCampos();
        atualizarVisualizacao();
    }

    /**
     * Atualiza a visualização e historico com o contato
     * 
     * @since 30/04/2026
     * @author Iuri
     * @return Formata as mensagens enviadas
     */

    private void atualizarVisualizacao() {
        Contato selecionado = listaContatos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            return;
        }

        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT assunto, mensagem, prioridade, datahora, remetente_id, destinatario_id "
                    + "FROM mensagens "
                    + "WHERE (destinatario_id = ? AND remetente_id = ?) "
                    + "   OR (destinatario_id = ? AND remetente_id = ?) "
                    + "ORDER BY datahora ASC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selecionado.getId());
            stmt.setInt(2, Sessao.getUsuarioLogado().getId());
            stmt.setInt(3, Sessao.getUsuarioLogado().getId());
            stmt.setInt(4, selecionado.getId());

            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            sb.append("HISTÓRICO COM: ").append(selecionado.getNome()).append(" ").append("<")
                    .append(selecionado.getEmail()).append(">").append("\n");

            boolean temMensagens = false;
            while (rs.next()) {
                temMensagens = true;

                int remetenteId = rs.getInt("remetente_id");
                int destinatarioId = rs.getInt("destinatario_id");

                String remetenteNome = (remetenteId == Sessao.getUsuarioLogado().getId())
                        ? "Você <" + Sessao.getUsuarioLogado().getEmail() + ">"
                        : selecionado.getNome() + " <" + selecionado.getEmail() + ">";

                String destinatarioNome = (destinatarioId == Sessao.getUsuarioLogado().getId())
                        ? "Você <" + Sessao.getUsuarioLogado().getEmail() + ">"
                        : selecionado.getNome() + " <" + selecionado.getEmail() + ">";

                sb.append("\nDE: ").append(remetenteNome).append("\n");
                sb.append("PARA: ").append(destinatarioNome).append("\n");
                sb.append("ASSUNTO: ").append(rs.getString("assunto").toUpperCase()).append("\n");
                sb.append("DATA: ").append(rs.getTimestamp("datahora")).append("\n");
                sb.append("PRIORIDADE: ").append(rs.getString("prioridade").toUpperCase()).append("\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                sb.append("MENSAGEM:\n\n").append(rs.getString("mensagem")).append("\n\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            }

            if (!temMensagens) {
                sb.append("\nNenhuma mensagem trocada ainda.\n");
            }

            historicoMensagens.setText(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            historicoMensagens.setText("Erro ao carregar histórico.");
        }
        // Após definir o texto no historicoMensagens
        historicoMensagens.positionCaret(historicoMensagens.getText().length());
        // Scrollpane
        scrollHistorico.setVvalue(1.0);
    }

    /**
     * Pesquisa contatos pelo nome digitado no campo de pesquisa.
     *
     * @throws Exception Caso ocorra erro de conexão ou consulta no banco
     */
    @FXML
    private void FuncPesquisar() throws Exception {
        String nome = campoPesquisa.getText().trim();
        try (Connection conn = Conexao.conectar()) {
            String sqlPesquisar = "SELECT id, nome, numeroTelefone, email, foto_perfil FROM usuarios WHERE nome LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sqlPesquisar);
            stmt.setString(1, "%" + nome + "%");

            listaContatos.getItems().clear();

            try (ResultSet rsPesq = stmt.executeQuery()) {
                Contato usuarioLogado = Sessao.getUsuarioLogado();
                while (rsPesq.next()) {
                    int id = rsPesq.getInt("id");
                    String nomeUsuario = rsPesq.getString("nome");
                    String telefone = rsPesq.getString("numeroTelefone");
                    String email = rsPesq.getString("email");
                    String fotoPerfil = rsPesq.getString("foto_perfil");
                    // Filtra o usuário logado também na pesquisa
                    if (usuarioLogado == null || id != usuarioLogado.getId()) {
                        Contato c = new Contato(id, nomeUsuario, telefone, email);
                        c.setFotoPerfil(fotoPerfil);
                        listaContatos.getItems().add(c);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Atenção", "Erro ao pesquisar nome do usuário");
            }
        }
    }

    @FXML
    private void limparCampos() {
        campoAssuntoEnvio.clear();
        campoMensagem.clear();
    }

    @FXML
    private void selecionarTodos() {
        listaContatos.getSelectionModel().selectAll();
    }

    @FXML
    private void desmarcarTodos() {
        listaContatos.getSelectionModel().clearSelection();
        historicoMensagens.clear();
        labelContagem.setText("Nenhum contato selecionado");
    }

    @FXML
    private void logout() {
        try {
            App.setRoot("tela");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Aplica um recorte circular na imagem de perfil exibida.
     *
     * @param imgView ImageView onde será aplicado o recorte
     */
    // Metodo de deixar a foto de perfil circular
    private void aplicarClipCircular(ImageView imgView) {
        double radius = Math.min(imgView.getFitWidth(), imgView.getFitHeight()) / 2;
        Circle clip = new Circle(imgView.getFitWidth() / 2, imgView.getFitHeight() / 2, radius);
        imgView.setClip(clip);
    }

    /**
     * Inicia o relógio da interface, atualizando o label a cada segundo.
     */
    private void iniciarRelogio() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    LocalDateTime agora = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    labelRelogio.setText(agora.format(formatter));
                }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

}
