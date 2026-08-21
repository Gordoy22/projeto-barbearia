package br.com.barbearia.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ServicoDTO {

    private Long id;

    @NotBlank(message = "Informe o nome do serviço.")
    @Size(max = 120)
    private String nome;

    @Size(max = 500)
    private String descricao;

    @NotNull(message = "Informe a duração em minutos.")
    @Min(value = 5, message = "A duração mínima é de 5 minutos.")
    private Integer duracaoMinutos;

    @NotNull(message = "Informe o valor.")
    @Positive(message = "O valor deve ser maior que zero.")
    private BigDecimal valor;

    private boolean ativo = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
