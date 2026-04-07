package com.mycompany.disparadordemensagens.controller;

import com.mycompany.disparadordemensagens.models.Contato;

// Classe para identificar o usuario logado
public class Sessao {

    private static Contato usuarioLogado;

    public static void setUsuarioLogado(Contato usuario) {
        usuarioLogado = usuario;
    }

    public static Contato getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void limpar() {
        usuarioLogado = null;
    }
}