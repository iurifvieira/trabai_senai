package com.mycompany.disparadordemensagens.models;

import com.mycompany.disparadordemensagens.models.Contato;
import com.mycompany.disparadordemensagens.controller.UsuarioControle;
import java.util.Objects;
import javafx.scene.paint.Color;

public class Contato {

    private int id;
    private String nome;
    private String numeroTelefone;
//  private String senha;
    private String email;

    public Contato(int id, String nome, String numeroTelefone, String email) {
        this.id = id;
        this.nome = nome;
        this.numeroTelefone = numeroTelefone;
//        this.senha = senha;
        this.email = email;
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
    public String getEmail() {
        return email;
    }

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
//        this.senha = senha;
//    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return nome + " (" + email + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
