package explorador.arquivos.demo.model;

import java.time.LocalDateTime;

public abstract class SistemaArquivos {
    private String nome;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataModificacao;

    public SistemaArquivos(String nome) {
        this.nome = nome;
        this.dataCriacao = LocalDateTime.now();
        this.dataModificacao = LocalDateTime.now();
    }

    public String getNome() { return nome; }
    public void setNome(String nome) {
        this.nome = nome;
        this.dataModificacao = LocalDateTime.now();
    }

    // Método abstrato: cada classe calcula seu tamanho de forma específica
    public abstract double getTamanho();
}