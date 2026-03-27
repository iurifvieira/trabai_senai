/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.disparadordemensagens.models;

/**
 *
 * @author aprendiz.ti
 */
public class Grupo {

    private int id;
    private String nome;

    public Grupo(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }
//getters
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
//setters 
    private void setId(int id){
        this.id =  id;
    }
     private void setNome(String nome){
         this.nome = nome;
     }
    
     @Override
    public String toString() {
        return nome; // mostra o nome no ComboBox
    }
}

