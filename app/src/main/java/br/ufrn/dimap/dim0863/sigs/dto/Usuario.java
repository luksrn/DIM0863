package br.ufrn.dimap.dim0863.sigs.dto;

import com.squareup.moshi.Json;

public class Usuario {

    @Json(name="nome-pessoa")
    private String nomePessoa;

    @Json(name="login")
    private String login;

    @Json(name="chave-foto")
    private String chaveFoto;

    public String getNomePessoa() {
        return nomePessoa;
    }

    public void setNomePessoa(String nomePessoa) {
        this.nomePessoa = nomePessoa;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getChaveFoto() {
        return chaveFoto;
    }

    public void setChaveFoto(String chaveFoto) {
        this.chaveFoto = chaveFoto;
    }
}
