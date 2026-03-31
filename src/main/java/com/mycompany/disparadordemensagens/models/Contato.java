package com.mycompany.disparadordemensagens.models;

import com.mycompany.disparadordemensagens.models.Contato;
import com.mycompany.disparadordemensagens.controller.UsuarioControle;
import java.util.Objects;
import javafx.scene.paint.Color;
public class Contato {

    private int id;
    private String nome;
    private String numeroTelefone;
//    private String senha;
    

    public Contato(int id, String nome, String numeroTelefone) {
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

  @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contato)) return false;
        Contato contato = (Contato) o;
        return id == contato.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}