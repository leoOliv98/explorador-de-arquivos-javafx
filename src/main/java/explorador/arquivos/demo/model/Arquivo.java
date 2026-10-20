package explorador.arquivos.demo.model;

public class Arquivo extends SistemaArquivos {
    private String tipo; // ex: "txt", "pdf", "png"
    private double tamanho; // em KB ou MB

    public Arquivo(String nome, String tipo, double tamanho) {
        super(nome);
        this.tipo = tipo;
        this.tamanho = tamanho;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    @Override
    public double getTamanho() {
        return tamanho;
    }

    public void setTamanho(double tamanho) {
        this.tamanho = tamanho;
    }
}