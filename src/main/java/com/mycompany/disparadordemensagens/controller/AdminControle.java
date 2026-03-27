package com.mycompany.disparadordemensagens.controller;

import com.mycompany.disparadordemensagens.database.Conexao;
import com.mycompany.disparadordemensagens.models.Contato;
import com.mycompany.disparadordemensagens.App;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminControle {

    @FXML
    private ListView<Contato> listaUsuarios;   // Lista de usuários cadastrados 
    @FXML
    private TextField campoAssuntoAdmin;      // campo onde digita o assunto
    @FXML
    private TextArea campoMensagemAdmin;      // campo onde escreve a mensagem
    @FXML
    private TextArea logDeEnvios;             // historico de mensagens
    @FXML
    private ComboBox<String> comboPrioridade; // campo onde informa a prioridade do assunto
    @FXML
    private Label labelContagem;              // label que mostra quantos selecionados
    @FXML
    private Label labelStatusSistema;         //informação do sistema online

    // Mapeia "nome - telefone" → id do usuário
    private Map<Contato, Integer> usuariosMap = new HashMap<>();

    private String Contato;

    @FXML
    public void initialize() {
        listaUsuarios.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT id, nome, numeroTelefone FROM usuarios";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                String telefone = rs.getString("numeroTelefone");

                Contato contato = new Contato(id,nome, telefone);
                listaUsuarios.getItems().add(contato);

            }
        } catch (Exception e) {
            e.printStackTrace();
            labelStatusSistema.setText("Erro ao carregar usuários");
        }

        comboPrioridade.getItems().addAll("Alta", "Média", "Baixa");
        comboPrioridade.setValue("Média");

        labelStatusSistema.setText("● Sistema Online - "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        listaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            atualizarVisualizacaoHistorico();
        });
    }

    private void atualizarVisualizacaoHistorico() {
        var selecionados = listaUsuarios.getSelectionModel().getSelectedItems();

        if (selecionados.size() == 1) {
            com.mycompany.disparadordemensagens.models.Contato usuario = selecionados.get(0);
            int usuarioId = usuariosMap.get(usuario);

            String sql = "SELECT m.assunto, m.mensagem, m.prioridade, m.datahora, u.nome AS remetente "
                    + "FROM mensagens m "
                    + "JOIN usuarios u ON m.remetente_id = u.id "
                    + "WHERE m.destinatario_id = ? OR m.remetente_id = ? "
                    + "ORDER BY m.datahora ASC";

            try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, usuarioId);
                stmt.setInt(2, usuarioId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.isBeforeFirst()) {
                    logDeEnvios.setText("Nenhuma mensagem enviada para: " + usuario);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" HISTÓRICO: ").append(usuario).append("\n");
                    while (rs.next()) {
                        sb.append("De: ").append(rs.getString("remetente")).append("\n");
                        sb.append("Assunto: ").append(rs.getString("assunto")).append("\n");
                        sb.append("Data: ").append(rs.getString("datahora")).append("\n");
                        sb.append("Prioridade: ").append(rs.getString("prioridade")).append("\n");
                        sb.append("Mensagem: ").append(rs.getString("mensagem")).append("\n");
                        sb.append("--------------------------------------------------\n");
                    }
                    logDeEnvios.setText(sb.toString());
                    logDeEnvios.setScrollTop(Double.MAX_VALUE);
                    logDeEnvios.selectEnd();
                    logDeEnvios.deselect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (selecionados.isEmpty()) {
            logDeEnvios.clear();
            logDeEnvios.setPromptText("Selecione um usuário para ver o histórico individual.");
        } else {
            logDeEnvios.setText("Modo Disparo em Lote: " + selecionados.size() + " usuários selecionados.");
        }

        labelContagem.setText(selecionados.size() + " usuários selecionados");
    }

    @FXML
    private void executarDisparoLote() {
        List<Contato> selecionadosNoMomento = new ArrayList<>(listaUsuarios.getSelectionModel().getSelectedItems());

        String assunto = campoAssuntoAdmin.getText();
        String mensagem = campoMensagemAdmin.getText();
        String prioridade = comboPrioridade.getValue();
        LocalDateTime agora = LocalDateTime.now();

        if (selecionadosNoMomento.isEmpty() || mensagem.isEmpty() || assunto.isEmpty()) {
            mostrarAlerta("Erro", "Preencha todos os campos!");
            return;
        }

        for (com.mycompany.disparadordemensagens.models.Contato usuario : selecionadosNoMomento) {
            int destinatarioId = usuariosMap.get(usuario);

            String sql = "INSERT INTO mensagens (remetente_id, destinatario_id, assunto, mensagem, prioridade, datahora) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(2, destinatarioId);
                stmt.setString(3, assunto);
                stmt.setString(4, mensagem);
                stmt.setString(5, prioridade);
                stmt.setTimestamp(6, Timestamp.valueOf(agora));
                stmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        campoAssuntoAdmin.clear();
        campoMensagemAdmin.clear();
        atualizarVisualizacaoHistorico();

        mostrarAlerta("Atenção", "Disparo realizado com sucesso.");
    }

    @FXML
    private void limparAdmin() {
        campoAssuntoAdmin.clear();
        campoMensagemAdmin.clear();
    }

    @FXML
    private void logout() {
        try {
            App.setRoot("tela");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void selecionarTodos() {
        listaUsuarios.getSelectionModel().selectAll();
    }

    @FXML
    private void desmarcarTodos() {
        listaUsuarios.getSelectionModel().clearSelection();
        logDeEnvios.clear();
    }

 private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
