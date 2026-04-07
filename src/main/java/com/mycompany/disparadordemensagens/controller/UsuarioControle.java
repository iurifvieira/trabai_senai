package com.mycompany.disparadordemensagens.controller;

import com.mycompany.disparadordemensagens.database.Conexao;
import com.mycompany.disparadordemensagens.models.Contato;
import com.mycompany.disparadordemensagens.App;
import com.mycompany.disparadordemensagens.models.Mensagem;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ListChangeListener;

public class UsuarioControle {

    @FXML
    private ListView<Contato> listaContatos;
    @FXML
    private TextArea historicoMensagens;
    @FXML
    private TextField campoAssuntoEnvio;
    @FXML
    private TextArea campoMensagem;
    @FXML
    private ComboBox<String> comboPrioridade;
    @FXML
    private Label labelContagem;
    @FXML
    private Label labelStatusSistema;
    @FXML
    private TextField campoPesquisa;
    @FXML
    private ScrollPane scrollHistorico;
    @FXML
    private Label labelSelecionarManual;

    private Map<Contato, List<Mensagem>> historicoMensagensMap = new HashMap<>();
    private List<Contato> todosUsuarios = new ArrayList<>();

    @FXML
    public void initialize() {
        listaContatos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // A filtragem do usuário logado é feita corretamente dentro do while abaixo
        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT id, nome, numeroTelefone FROM usuarios";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                String telefone = rs.getString("numeroTelefone");

                // Filtra o usuário logado corretamente aqui
                if (Sessao.getUsuarioLogado() == null || id != Sessao.getUsuarioLogado().getId()) {
                    Contato contato = new Contato(id, nome, telefone);
                    listaContatos.getItems().add(contato);
                    todosUsuarios.add(contato);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            labelStatusSistema.setText("Erro ao carregar usuários");
        }

        comboPrioridade.getItems().addAll("Alta", "Média", "Baixa");
        comboPrioridade.setValue("Média");
        labelStatusSistema.setText("● Online - "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        listaContatos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            atualizarVisualizacao();
        });

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

        campoPesquisa.textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                FuncPesquisar();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean isCtrlPressed() {
        return listaContatos.getScene() != null
                && listaContatos.getScene().getAccelerators() != null;
    }

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
                Mensagem msg = new Mensagem(c.getId(), assunto, conteudo, prioridade, Sessao.getUsuarioLogado().getId());
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
            sb.append("HISTÓRICO COM: ").append(selecionado.getNome()).append("\n");

            boolean temMensagens = false;
            while (rs.next()) {
                temMensagens = true;

                int remetenteId = rs.getInt("remetente_id");
                int destinatarioId = rs.getInt("destinatario_id");

                String remetenteNome = (remetenteId == Sessao.getUsuarioLogado().getId())
                        ? "Você"
                        : selecionado.getNome();

                String destinatarioNome = (destinatarioId == Sessao.getUsuarioLogado().getId())
                        ? "Você"
                        : selecionado.getNome();

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

        labelContagem.setText(listaContatos.getSelectionModel().getSelectedItems().size() + " usuários selecionados");
    }

    @FXML
    private void FuncPesquisar() throws Exception {
        String nome = campoPesquisa.getText().trim();
        try (Connection conn = Conexao.conectar()) {
            String sqlPesquisar = "SELECT * FROM usuarios WHERE nome LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sqlPesquisar);
            stmt.setString(1, "%" + nome + "%");
            listaContatos.getItems().clear();

            try (ResultSet rsPesq = stmt.executeQuery()) {
                Contato usuarioLogado = Sessao.getUsuarioLogado();
                while (rsPesq.next()) {
                    int id = rsPesq.getInt("id");
                    String nomeUsuario = rsPesq.getString("nome");
                    String telefone = rsPesq.getString("numeroTelefone");

                    // Filtra o usuário logado também na pesquisa
                    if (usuarioLogado == null || id != usuarioLogado.getId()) {
                        Contato c = new Contato(id, nomeUsuario, telefone);
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

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
