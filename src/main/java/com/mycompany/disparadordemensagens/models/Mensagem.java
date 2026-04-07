package com.mycompany.disparadordemensagens.models;

import java.time.LocalDateTime;

public class Mensagem {

    private int destinatarioId;
    private String assunto;
    private String mensagem;
    private String prioridade;
    private LocalDateTime dataHora;
    private int remetenteId;

// Construtor usado ao enviar
    public Mensagem(int destinatarioId, String assunto, String mensagem, String prioridade, int remetenteId) {
        this.destinatarioId = destinatarioId;
        this.assunto = assunto;
        this.mensagem = mensagem;
        this.prioridade = prioridade;
        this.dataHora = LocalDateTime.now();
        this.remetenteId = remetenteId;
        
    }

    // Construtor usado ao carregar do banco
    public Mensagem(int destinatarioId, String assunto, String mensagem, String prioridade, int remetenteId, LocalDateTime dataHora) {
        this.destinatarioId = destinatarioId;
        this.assunto = assunto;
        this.mensagem = mensagem;
        this.prioridade = prioridade;
        this.dataHora = dataHora;
        this.remetenteId = remetenteId;
    }

    //getters
    public int getDestinatarioId() {
        return destinatarioId;
    }

    public String getAssunto() {
        return assunto;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public int getRemetenteId() {
        return remetenteId;
    }

    //setters
    public void setDestinatarioId(int destinatarioId) {
        this.destinatarioId = destinatarioId;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public void setRemetenteId(int remetenteId) {
        this.remetenteId = remetenteId;
    }

    public String formatarEnvelope(Contato remetente, Contato destinatario) {
       
        StringBuilder sb = new StringBuilder();
        sb.append("\nDE: ").append(remetente.getNome()).append("\n");
        sb.append("PARA: ").append(destinatario.getNome()).append("\n");
        sb.append("ASSUNTO: ").append(assunto.toUpperCase()).append("\n");
        sb.append("DATA: ").append(dataHora).append("\n");
        sb.append("PRIORIDADE: ").append(prioridade.toUpperCase()).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("MENSAGEM:\n\n").append(mensagem).append("\n\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        return sb.toString();
    }

}
