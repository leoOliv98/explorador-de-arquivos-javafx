package explorador.arquivos.demo.model;

public class ListaDuplamenteEncadeada<T> {
    private No<T> inicio;
    private No<T> fim;
    private int quantidade;

    public ListaDuplamenteEncadeada() {
        this.inicio = null;
        this.fim = null;
        this.quantidade = 0;
    }

    public void adicionar(T elemento) {
        No<T> novo = new No<>(elemento);
        if (inicio == null) {
            inicio = novo;
            fim = novo;
        } else {
            fim.setProximo(novo);
            novo.setAnterior(fim);
            fim = novo;
        }
        quantidade++;
    }

    public boolean remover(T elemento) {
        No<T> atual = inicio;

        while (atual != null) {
            if (atual.getDado().equals(elemento)) {
                if (atual == inicio) {
                    inicio = atual.getProximo();
                    if (inicio != null) {
                        inicio.setAnterior(null);
                    } else {
                        fim = null; // A lista ficou vazia
                    }
                } else if (atual == fim) {
                    fim = atual.getAnterior();
                    fim.setProximo(null);
                } else {
                    atual.getAnterior().setProximo(atual.getProximo());
                    atual.getProximo().setAnterior(atual.getAnterior());
                }
                quantidade--;
                return true;
            }
            atual = atual.getProximo();
        }
        return false;
    }

    public No<T> getInicio() { return inicio; }
    public No<T> getFim() { return fim; }
    public int getQuantidade() { return quantidade; }
}