package com.mycompany.disparadordemensagens.controller;


import com.mycompany.disparadordemensagens.models.Contato;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PerfilContatoController {
    @FXML private Label labelNome;
    @FXML private Label labelTelefone;
    @FXML private Label labelEmail;
    @FXML private Label labelId;

    void setContato(Contato contato) {
        labelNome.setText(contato.getNome());
        labelTelefone.setText("Telefone: " + contato.getNumeroTelefone());
        labelEmail.setText("Email: " + contato.getEmail());
        labelId.setText("ID: " + contato.getId());
    }
}
