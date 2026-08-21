package br.com.barbearia.enums;

public enum PerfilUsuario {
    ADMIN("Administrador"),
    ATENDENTE("Atendente");

    private final String descricao;

    PerfilUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
