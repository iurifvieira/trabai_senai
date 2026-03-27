package com.mycompany.disparadordemensagens.controller;

import com.mycompany.disparadordemensagens.database.Conexao;
import com.mycompany.disparadordemensagens.models.Contato;
import com.mycompany.disparadordemensagens.App;
import com.mycompany.disparadordemensagens.models.Grupo;
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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.ListChangeListener;
import javafx.stage.Popup;

public class UsuarioControle {

    @FXML
    private ListView<Contato> listaContatos; //array contendo a lista de usuarios 
    @FXML
    private TextArea historicoMensagens; // area onde fica o historico;
    @FXML
    private TextField campoAssuntoEnvio;
    @FXML
    private TextArea campoMensagem;
    @FXML
    private ComboBox<String> comboPrioridade;
    @FXML
    private Label labelContagem; //label onde mostra quantos usuarios foram selecionados
    @FXML
    private Label labelStatusSistema;
    @FXML
    private TextField campoPesquisa;
    @FXML
    private ScrollPane scrollHistorico;
    @FXML
    private Label labelSelecionarManual;
    @FXML
    private ComboBox<Grupo> comboGrupos;

    private Map<Contato, List<Mensagem>> historicoMensagensMap = new HashMap<>();
    private Popup sugestoesPopup = new Popup();
    private ListView<Contato> listaSugestoes = new ListView<>();
    private List<Contato> todosUsuarios = new ArrayList<>();

    @FXML
    public void initialize() { // inicialização, onde carrega os usuarios
        listaContatos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //Faz a conexão com o banco para trazer os usuarios
        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT id, nome, numeroTelefone FROM usuarios ";
            PreparedStatement stmt = conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();
            //enquanto "rs" for executavel, ele recebe as informações
            while (rs.next()) {
                int id = rs.getInt("id"); // id do usuário
                String nome = rs.getString("nome"); // nome 
                String telefone = rs.getString("numeroTelefone"); // telefone
                // crio um novo objeto contendo as informações
                Contato contato = new Contato(id, nome, telefone);
                listaContatos.getItems().add(contato); // jogo na lista de contatos
                todosUsuarios.add(contato);
            }
        } catch (Exception e) {  //caso dê erro
            e.printStackTrace();
            labelStatusSistema.setText("Erro ao carregar usuários");
        } //"botão" com o nível de prioridade
        comboPrioridade.getItems().addAll("Alta", "Média", "Baixa");
        comboPrioridade.setValue("Média");
        //label de status 
        labelStatusSistema.setText("● Online - "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        listaContatos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            atualizarVisualizacao();
        });

        // Listener de seleção manual para usuario
        listaContatos.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Contato>) change -> {
            if (isCtrlPressed() && listaContatos.getSelectionModel().getSelectedItems().size() > 1) {
                labelSelecionarManual.setText("Selecionar manualmente");
            } else {
                labelSelecionarManual.setText("");
            }
        });

        // Listener de teclado
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

        //função para a pesquisa, atualiza a cada letra digitada
        campoPesquisa.textProperty().addListener((obs, oldValue, newValue) -> { //listener é um "ouvinte", reage a eventos específicos em uma aplicação
            try {
                FuncPesquisar(); // chama o método a cada alteração
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //Metodo de selecionar  contato manualmente
    private boolean isCtrlPressed() {
        return listaContatos.getScene() != null
                && listaContatos.getScene().getAccelerators() != null; // 
    }

    @FXML  // metodo de enviar mensagem
    private void enviarMensagem() throws Exception {
        List<Contato> contatosSelecionados = listaContatos.getSelectionModel().getSelectedItems();
        String assunto = campoAssuntoEnvio.getText(); //assunto recebe campo de assunto
        String conteudo = campoMensagem.getText(); // recebe campo mensagem
        String prioridade = comboPrioridade.getValue(); // recebe combo prioridade

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
        // tenta conectar com o banco para enviar as informações para os selecionados
        try (Connection conn = Conexao.conectar()) {
            String sqlEnviar = "INSERT INTO mensagens ( destinatario_id, assunto, mensagem, prioridade, datahora) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sqlEnviar);
            //para contatos selecionados recebem
            for (Contato c : contatosSelecionados) {
                Mensagem msg = new Mensagem(c.getId(), assunto, conteudo, prioridade);
                stmt.setInt(1, msg.getDestinatarioId());
                stmt.setString(2, msg.getAssunto());
                stmt.setString(3, msg.getMensagem());
                stmt.setString(4, msg.getPrioridade());
                stmt.setTimestamp(5, Timestamp.valueOf(msg.getDataHora()));
                stmt.executeUpdate();

                // monta envelope para exibir na tela
                StringBuilder envelope = new StringBuilder();
                envelope.append("\n  PARA:    ").append(c.getNome()).append("\n");
                envelope.append("  ASSUNTO: ").append(assunto.toUpperCase()).append("\n");
                envelope.append("  DATA:    ").append(msg.getDataHora()).append("\n");
                envelope.append("  PRIORIDADE: ").append(prioridade.toUpperCase()).append("\n");
                envelope.append("_________________________________________________________________\n\n");
                envelope.append("  MENSAGEM:\n\n");
                envelope.append("  ").append(conteudo.replace("\n", "\n  ")).append("\n\n");
                envelope.append("_________________________________________________________________\n\n");

                // adiciona no TextArea de histórico
                historicoMensagens.appendText(envelope.toString());
                // Atualiza cache local
                historicoMensagensMap.computeIfAbsent(c, k -> new java.util.ArrayList<>()).add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Falha ao enviar mensagem.");
        }

        limparCampos();
        atualizarVisualizacao();
    }

    private void atualizarVisualizacao() { // atualiza depois de enviar mensagem 
//        var selecionados = listaContatos.getSelectionModel().getSelectedItems();;
        Contato selecionado = listaContatos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            return;
        }
        // tenta fazer a conexão trazendo as informações do banco atualizado
        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT  assunto, mensagem, prioridade, datahora FROM mensagens WHERE destinatario_id = ? ORDER BY datahora ASC ";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selecionado.getId());
            //resultado do historico, forma o "envelope" com as informações
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();  //string builder serve para manipular sequencias de caracteres 
            sb.append("HISTÓRICO COM: ").append(selecionado.getNome()).append("");
            while (rs.next()) {
                // monta envelope para exibir na tela
                StringBuilder envelope = new StringBuilder();
                envelope.append("\n  PARA:    ").append(selecionado.getNome()).append("-").append("(").append(selecionado.getNumeroTelefone()).append(")").append("\n");        //append serve para acrescentar algum valor a uma sequência atual
                envelope.append("  ASSUNTO: ").append(rs.getString("assunto").toUpperCase()).append("\n");
                envelope.append("  DATA:    ").append(rs.getTimestamp("datahora")).append("\n");
                envelope.append("  PRIORIDADE: ").append(rs.getString("prioridade").toUpperCase()).append("\n");
                envelope.append("_________________________________________________________________\n\n");
                envelope.append("  MENSAGEM:\n\n");
                envelope.append("  ").append(rs.getString("mensagem").replace("\n", "\n  ")).append("\n\n");
                envelope.append("_________________________________________________________________\n\n");
                //transforma em string para visualizar
                sb.append(envelope.toString());
            }
            // move o cursor para o fim
            historicoMensagens.positionCaret(historicoMensagens.getText().length());
            historicoMensagens.setScrollTop(Double.MAX_VALUE);
            historicoMensagens.selectEnd();
            historicoMensagens.deselect();
            // historico de mensagens recebe todo o texto em string
            historicoMensagens.setText(sb.toString());
        } catch (Exception e) { //caso de erro
            e.printStackTrace();
            historicoMensagens.setText("Erro ao carregar histórico.");
        }
        labelContagem.setText(listaContatos.getSelectionModel().getSelectedItems().size() + " usuários selecionados");;;

    }

//    @FXML
//    private void criarGrupo() {
//        // abre um diálogo para digitar o nome do grupo
//        TextInputDialog dialog = new TextInputDialog();
//        dialog.setTitle("Novo Grupo");
//        dialog.setHeaderText("Criar novo grupo");
//        dialog.setContentText("Nome do grupo:");
//
//        Optional<String> result = dialog.showAndWait();
//        result.ifPresent(nomeGrupo -> {
//            try (Connection conn = Conexao.conectar()) {
//                // insere grupo
//                String sqlGrupo = "INSERT INTO grupos (nome) VALUES (?)";
//                PreparedStatement stmtGrupo = conn.prepareStatement(sqlGrupo, Statement.RETURN_GENERATED_KEYS);
//                stmtGrupo.setString(1, nomeGrupo);
//                stmtGrupo.executeUpdate();
//
//                ResultSet rs = stmtGrupo.getGeneratedKeys();
//                int grupoId = 0;
//                if (rs.next()) {
//                    grupoId = rs.getInt(1);
//                }
//
//                // associa usuários selecionados
//                for (Contato contato : listaContatos.getSelectionModel().getSelectedItems()) {
//                    String sqlAssoc = "INSERT INTO grupo_usuarios (grupo_id, usuario_id) VALUES (?, ?)";
//                    PreparedStatement stmtAssoc = conn.prepareStatement(sqlAssoc);
//                    stmtAssoc.setInt(1, grupoId);
//                    stmtAssoc.setInt(2, contato.getId());
//                    stmtAssoc.executeUpdate();
//                }
//
//                // atualiza ComboBox
//                comboGrupos.getItems().add(new Grupo(grupoId, nomeGrupo));
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }
    @FXML // metodo de pesquisar nome do usuário
    private void FuncPesquisar() throws Exception {
        String nome = campoPesquisa.getText().trim(); // string nome pega o que passa no campo pesquisa
        try (Connection conn = Conexao.conectar()) {

            String sqlPesquisar = "SELECT * FROM usuarios  WHERE nome LIKE ? "; // sqlpesquisar busca no banco o nome
            PreparedStatement stmt = conn.prepareStatement(sqlPesquisar);
            stmt.setString(1, "%" + nome + "%");
            listaContatos.getItems().clear(); //lista pega o item que foi passado e limpa a tabela

            try (ResultSet rsPesq = stmt.executeQuery()) {
                while (rsPesq.next()) { //enquanto rspesquisa continuar 
                    int id = rsPesq.getInt("id");
                    String nomeUsuario = rsPesq.getString("nome");
                    String telefone = rsPesq.getString("numeroTelefone");

                    Contato c = new Contato(id, nomeUsuario, telefone);
                    c.setNome(rsPesq.getString("nome"));
                    listaContatos.getItems().add(c); //faz toda essa função
                }
                if (rsPesq == null) {
                    listaContatos.refresh();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Atenção", "Erro ao pesquisar nome do usuário");
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
