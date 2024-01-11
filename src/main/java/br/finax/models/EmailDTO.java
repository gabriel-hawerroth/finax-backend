package br.finax.models;

import lombok.Data;

@Data
public class EmailDTO {

    private String destinatario;
    private String assunto;
    private String conteudo;
}
