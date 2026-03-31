package com.mycompany.disparadordemensagens.models;

import java.time.LocalDateTime;

public class Mensagem {

    private int destinatarioId;
    private String assunto;
    private String mensagem;
    private String prioridade;
    private LocalDateTime dataHora;
    private int remetendeId;
//construtor
    public Mensagem(int destinatarioId, String assunto, String mensagem, String prioridade, int remetendeId) {
        this.destinatarioId = destinatarioId;
        this.assunto = assunto;
        this.mensagem = mensagem;
        this.prioridade = prioridade;
        this.dataHora = LocalDateTime.now();
        this.remetendeId =  remetendeId;
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
    public int rementeteiId(){
        return remetendeId;
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
        
    public void setRemetenteId(int remetenteId){
        this.remetendeId = remetenteId;
    }


}
