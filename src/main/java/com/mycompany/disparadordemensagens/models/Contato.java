package com.mycompany.disparadordemensagens.models;

import com.mycompany.disparadordemensagens.models.Contato;
import com.mycompany.disparadordemensagens.controller.UsuarioControle;
import javafx.scene.paint.Color;
public class Contato {

    private int id;
    private String nome;
    private String numeroTelefone;
//    private String senha;
    

    public Contato(int id, String nome, String numeroTelefone) {;
        this.id = id;
        this.nome = nome;
        this.numeroTelefone = numeroTelefone;
//        this.senha = senha;
    }
    
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNumeroTelefone() {
        return numeroTelefone;
    }

//    public String getSenha() {
//        return senha;
//    }
    //setters  
    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setNumeroTelefone(String numeroTelefone) {
        this.numeroTelefone = numeroTelefone;
    }

//    public void setSenha(String Senha) {
//        this.senha = Senha;
//    }
    
//     public void setUsuarioLogado(Contato usuario) {
//        this.usuarioLogado = usuario;
//    }
    @Override
    public String toString() {
        return nome + " (" + numeroTelefone + ")";
    }

    public void setFill(Color DARKCYAN) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
